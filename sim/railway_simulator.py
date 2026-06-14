"""
RailwaySimulator — the pure, in-process railway traffic simulation.

This is the gym-env's simulation core lifted out of the FastMCP / openenv-core
MCPEnvironment wrapper so it can be stepped millions of times per second locally
for RL training and driven directly by a live server.

Dynamics, network layouts, block-signaling rules, collision detection, the base
reward and the four task scenarios are ported VERBATIM (behaviour-preserving)
from railway_controller_gym_env/server/railway_environment.py. The only changes:

  * No FastMCP tools / MCPEnvironment / async — `step()` takes a plain action
    dict that applies the controller's FULL decision (all signals + all holds)
    for the step, then advances the world. (The original applied one MCP tool
    per step; applying the whole control vector per step is the natural RL
    framing and is a strict superset of the original capability.)
  * `step()` returns a (observation, base_reward, done) tuple. The energy term
    is added on top by the Gymnasium wrapper, not here, so this sim stays usable
    standalone and the energy contribution is cleanly ablatable.

Action dict format:
    {
        "signals": {segment_id: "red" | "yellow" | "green", ...},
        "holds":   {train_id: bool, ...},   # True = hold, False = release/run
    }
"""

from __future__ import annotations

import random
import uuid
from typing import Any, Dict, List, Optional, Set, Tuple

from .models import (
    RailwayObservation,
    SignalState,
    TaskResult,
    TrackSegment,
    TrainState,
    TrainStatus,
)

# Imported lazily-friendly: tasks config lives alongside the project.
from configs.tasks import TASK_CONFIGS  # noqa: E402


class RailwaySimulator:
    """Pure railway traffic simulator (one central controller, full network view)."""

    TASK_CONFIGS = TASK_CONFIGS

    def __init__(self, task_name: str = "basic_control", verbose: bool = False):
        self._task_name = task_name
        self._config = self.TASK_CONFIGS.get(task_name, self.TASK_CONFIGS["basic_control"])
        self.verbose = verbose

        self.episode_id: str = str(uuid.uuid4())

        # World state
        self._trains: Dict[str, TrainState] = {}
        self._track_segments: Dict[str, TrackSegment] = {}
        self._train_routes: Dict[str, List[str]] = {}
        self._held_trains: Set[str] = set()
        self._collisions: int = 0
        self._collisions_this_step: int = 0
        self._arrived_trains: Set[str] = set()
        self._step_count: int = 0
        self._max_steps: int = self._config["max_steps"]
        self._weather_active: bool = False
        self._weather_speed_modifier: float = 1.0
        self._disabled_segments: Set[str] = set()

        self._initialize_network()

    # ------------------------------------------------------------------ #
    # Public API
    # ------------------------------------------------------------------ #
    @property
    def task_name(self) -> str:
        return self._task_name

    @property
    def config(self) -> dict:
        return self._config

    @property
    def num_trains(self) -> int:
        return len(self._trains)

    @property
    def num_segments(self) -> int:
        return len(self._track_segments)

    @property
    def step_count(self) -> int:
        return self._step_count

    @property
    def max_steps(self) -> int:
        return self._max_steps

    @property
    def collisions(self) -> int:
        return self._collisions

    @property
    def collisions_this_step(self) -> int:
        return self._collisions_this_step

    @property
    def held_trains(self) -> Set[str]:
        return set(self._held_trains)

    def segment_ids(self) -> List[str]:
        """Stable, sorted list of segment IDs (layout is fixed per task)."""
        return sorted(self._track_segments.keys())

    def train_ids(self) -> List[str]:
        """Stable, sorted list of train IDs (fixed per task)."""
        return sorted(self._trains.keys())

    @property
    def observation(self) -> RailwayObservation:
        """A deep snapshot of the world (safe to retain across a step)."""
        return RailwayObservation(
            trains={tid: t.model_copy(deep=True) for tid, t in self._trains.items()},
            track_segments={sid: s.model_copy(deep=True) for sid, s in self._track_segments.items()},
            current_step=self._step_count,
            max_steps=self._max_steps,
            collisions=self._collisions,
        )

    def reset(self, seed: Optional[int] = None, task_name: Optional[str] = None) -> RailwayObservation:
        """Reset the world. Optionally switch task. Returns the initial observation."""
        if seed is not None:
            random.seed(seed)
        if task_name and task_name in self.TASK_CONFIGS:
            self._task_name = task_name
            self._config = self.TASK_CONFIGS[task_name]
            self._max_steps = self._config["max_steps"]
        self.episode_id = str(uuid.uuid4())
        self._initialize_network()
        return self.observation

    def step(self, action: Optional[dict] = None) -> Tuple[RailwayObservation, float, bool]:
        """Apply the controller action, advance the world one step.

        Returns (observation, base_reward, done). The base reward has NO energy
        term — the Gymnasium wrapper adds that.
        """
        self._step_count += 1
        self._collisions_this_step = 0

        if action:
            self._apply_action(action)

        self._simulate_trains()
        self._check_collisions()

        reward = self._calculate_base_reward()
        done = self._is_done()
        return self.observation, reward, done

    def base_reward(self) -> float:
        """Re-expose the (energy-free) reward for the current state."""
        return self._calculate_base_reward()

    def is_done(self) -> bool:
        return self._is_done()

    # ------------------------------------------------------------------ #
    # Action application (replaces the MCP tools)
    # ------------------------------------------------------------------ #
    def _apply_action(self, action: dict) -> None:
        signals = action.get("signals", {}) or {}
        for seg_id, state in signals.items():
            seg = self._track_segments.get(seg_id)
            if seg is None:
                continue
            try:
                seg.signal_state = SignalState(str(state).lower())
            except ValueError:
                continue

        holds = action.get("holds", {}) or {}
        for tid, hold in holds.items():
            train = self._trains.get(tid)
            if train is None or train.status in (TrainStatus.ARRIVED, TrainStatus.DELAYED):
                continue
            if hold:
                self._held_trains.add(tid)
                train.status = TrainStatus.WAITING
                train.speed = 0.0
            else:
                self._held_trains.discard(tid)
                # Status/speed are decided by _simulate_trains this step.

    # ------------------------------------------------------------------ #
    # Network construction (ported verbatim)
    # ------------------------------------------------------------------ #
    def _initialize_network(self):
        self._trains.clear()
        self._track_segments.clear()
        self._train_routes.clear()
        self._held_trains.clear()
        self._collisions = 0
        self._collisions_this_step = 0
        self._arrived_trains = set()
        self._step_count = 0
        self._weather_active = False
        self._weather_speed_modifier = 1.0
        self._disabled_segments = set()

        if self._task_name == "basic_control":
            self._create_basic_network()
        elif self._task_name == "junction_management":
            self._create_junction_network()
        elif self._task_name == "rush_hour":
            self._create_complex_network()
        elif self._task_name == "express_priority":
            self._create_express_network()
        else:
            self._create_basic_network()

    def _create_basic_network(self):
        segments = [
            TrackSegment(segment_id="A-J1", length=3, next_segments=["J1-CROSS"], station_name="Station A"),
            TrackSegment(segment_id="J1-B", length=3, next_segments=[], station_name="Station B"),
            TrackSegment(segment_id="D-J1", length=3, next_segments=["J1-CROSS"], station_name="Station D"),
            TrackSegment(segment_id="J1-C", length=3, next_segments=[], station_name="Station C"),
            TrackSegment(segment_id="J1-CROSS", length=2, next_segments=["J1-B", "J1-C"], is_junction=True),
        ]
        for seg in segments:
            self._track_segments[seg.segment_id] = seg

        self._trains["T1"] = TrainState(
            train_id="T1", current_segment="A-J1", destination="J1-B",
            status=TrainStatus.MOVING, speed=1.0, scheduled_arrival=8,
            priority=1, train_type="regular",
        )
        self._train_routes["T1"] = ["A-J1", "J1-CROSS", "J1-B"]
        self._track_segments["A-J1"].occupied_by = "T1"

        self._trains["T2"] = TrainState(
            train_id="T2", current_segment="D-J1", destination="J1-C",
            status=TrainStatus.MOVING, speed=1.0, scheduled_arrival=8,
            priority=1, train_type="regular",
        )
        self._train_routes["T2"] = ["D-J1", "J1-CROSS", "J1-C"]
        self._track_segments["D-J1"].occupied_by = "T2"

    def _create_junction_network(self):
        segments = [
            TrackSegment(segment_id="N1-J1", length=2, next_segments=["J1-CORE"], station_name="Station N1"),
            TrackSegment(segment_id="E1-J1", length=2, next_segments=["J1-CORE"], station_name="Station E1"),
            TrackSegment(segment_id="J1-E1", length=2, next_segments=["E1-E2"]),
            TrackSegment(segment_id="E1-E2", length=2, next_segments=[], station_name="Station E2"),
            TrackSegment(segment_id="W1-J1", length=2, next_segments=["J1-CORE"], station_name="Station W1"),
            TrackSegment(segment_id="J1-W1", length=2, next_segments=["W1-W2"]),
            TrackSegment(segment_id="W1-W2", length=2, next_segments=[], station_name="Station W2"),
            TrackSegment(segment_id="S1-J2", length=2, next_segments=["J2-CORE"], station_name="Station S1"),
            TrackSegment(segment_id="J2-S1", length=2, next_segments=[], station_name="Station S1"),
            TrackSegment(segment_id="J1-CORE", length=1, next_segments=["J1-E1", "J1-W1", "J1-S1"], is_junction=True),
            TrackSegment(segment_id="J2-CORE", length=1, next_segments=["J2-E2", "J2-S1"], is_junction=True),
            TrackSegment(segment_id="J1-S1", length=2, next_segments=["J2-CORE"]),
            TrackSegment(segment_id="J2-E2", length=2, next_segments=["E1-E2"]),
        ]
        for seg in segments:
            self._track_segments[seg.segment_id] = seg

        trains_config = [
            ("T1", "N1-J1", "E1-E2", 12, 1, "regular", ["N1-J1", "J1-CORE", "J1-E1", "E1-E2"]),
            ("T2", "W1-J1", "J2-S1", 14, 2, "express", ["W1-J1", "J1-CORE", "J1-S1", "J2-CORE", "J2-S1"]),
            ("T3", "E1-J1", "W1-W2", 15, 1, "regular", ["E1-J1", "J1-CORE", "J1-W1", "W1-W2"]),
            ("T4", "S1-J2", "E1-E2", 16, 1, "regular", ["S1-J2", "J2-CORE", "J2-E2", "E1-E2"]),
        ]
        for tid, start, dest, arrival, priority, train_type, route in trains_config:
            self._trains[tid] = TrainState(
                train_id=tid, current_segment=start, destination=dest,
                status=TrainStatus.MOVING, speed=1.0, scheduled_arrival=arrival,
                priority=priority, train_type=train_type,
            )
            self._train_routes[tid] = route
            self._track_segments[start].occupied_by = tid

    def _create_complex_network(self):
        segments = [
            TrackSegment(segment_id="A-J1", length=2, next_segments=["J1-CORE"], station_name="Station A"),
            TrackSegment(segment_id="J1-B", length=2, next_segments=[], station_name="Station B"),
            TrackSegment(segment_id="A-J2", length=2, next_segments=["J2-CORE"]),
            TrackSegment(segment_id="J2-C", length=2, next_segments=["C-J3"], station_name="Station C"),
            TrackSegment(segment_id="J2-D", length=2, next_segments=[], station_name="Station D"),
            TrackSegment(segment_id="D-J4", length=2, next_segments=["J4-CORE"], station_name="Station D"),
            TrackSegment(segment_id="B-J3", length=2, next_segments=["J3-CORE"]),
            TrackSegment(segment_id="J3-E", length=2, next_segments=[], station_name="Station E"),
            TrackSegment(segment_id="E-J4", length=2, next_segments=["J4-CORE"], station_name="Station E"),
            TrackSegment(segment_id="J4-F", length=2, next_segments=[], station_name="Station F"),
            TrackSegment(segment_id="J1-CORE", length=1, next_segments=["J1-B", "J1-J2"], is_junction=True),
            TrackSegment(segment_id="J2-CORE", length=1, next_segments=["J2-C", "J2-D", "J4-CORE"], is_junction=True),
            TrackSegment(segment_id="J3-CORE", length=1, next_segments=["J3-E", "J4-CORE"], is_junction=True),
            TrackSegment(segment_id="J4-CORE", length=1, next_segments=["J4-F", "J2-CORE"], is_junction=True),
            TrackSegment(segment_id="J1-J2", length=2, next_segments=["J2-CORE"]),
            TrackSegment(segment_id="C-J3", length=2, next_segments=["J3-CORE"]),
            TrackSegment(segment_id="B-J1", length=2, next_segments=["J1-CORE"], station_name="Station B"),
            TrackSegment(segment_id="F-J4", length=2, next_segments=["J4-CORE"], station_name="Station F"),
        ]
        for seg in segments:
            self._track_segments[seg.segment_id] = seg

        trains_config = [
            ("HS1", "A-J1", "J1-B", 15, 3, "high-speed", ["A-J1", "J1-CORE", "J1-B"]),
            ("HS2", "F-J4", "J2-C", 18, 3, "high-speed", ["F-J4", "J4-CORE", "J2-CORE", "J2-C"]),
            ("EX1", "A-J2", "J3-E", 20, 2, "express", ["A-J2", "J2-CORE", "J2-C", "C-J3", "J3-CORE", "J3-E"]),
            ("EX2", "B-J1", "J4-F", 22, 2, "express", ["B-J1", "J1-CORE", "J1-J2", "J2-CORE", "J4-CORE", "J4-F"]),
            ("R1", "D-J4", "J2-D", 25, 1, "regular", ["D-J4", "J4-CORE", "J2-CORE", "J2-D"]),
            ("R2", "E-J4", "J4-F", 28, 1, "regular", ["E-J4", "J4-CORE", "J4-F"]),
        ]
        for tid, start, dest, arrival, priority, train_type, route in trains_config:
            delay = random.randint(0, 3) if self._config["has_delays"] else 0
            self._trains[tid] = TrainState(
                train_id=tid, current_segment=start, destination=dest,
                status=TrainStatus.MOVING, speed=1.0, scheduled_arrival=arrival,
                priority=priority, train_type=train_type, delay=delay,
            )
            self._train_routes[tid] = route
            self._track_segments[start].occupied_by = tid

        if self._config.get("has_weather", False):
            self._weather_active = True
            self._weather_speed_modifier = 0.75

    def _create_express_network(self):
        segments = [
            TrackSegment(segment_id="A-J1", length=2, next_segments=["J1-CORE"], station_name="Station A"),
            TrackSegment(segment_id="J1-B", length=2, next_segments=[], station_name="Station B"),
            TrackSegment(segment_id="J1-CORE", length=1, next_segments=["J1-B", "J1-J2"], is_junction=True),
            TrackSegment(segment_id="J1-J2", length=2, next_segments=["J2-CORE"]),
            TrackSegment(segment_id="C-J2", length=2, next_segments=["J2-CORE"], station_name="Station C"),
            TrackSegment(segment_id="J2-D", length=2, next_segments=[], station_name="Station D"),
            TrackSegment(segment_id="J2-CORE", length=1, next_segments=["J2-D", "J2-J3"], is_junction=True),
            TrackSegment(segment_id="J2-J3", length=2, next_segments=["J3-CORE"]),
            TrackSegment(segment_id="E-J3", length=2, next_segments=["J3-CORE"], station_name="Station E"),
            TrackSegment(segment_id="J3-F", length=2, next_segments=[], station_name="Station F"),
            TrackSegment(segment_id="J3-CORE", length=1, next_segments=["J3-F"], is_junction=True),
        ]
        for seg in segments:
            self._track_segments[seg.segment_id] = seg

        trains_config = [
            ("HS1", "A-J1", "J1-B", 6, 3, "high-speed", ["A-J1", "J1-CORE", "J1-B"]),
            ("HS2", "C-J2", "J3-F", 12, 3, "high-speed", ["C-J2", "J2-CORE", "J2-J3", "J3-CORE", "J3-F"]),
            ("EX1", "A-J1", "J2-D", 12, 2, "express", ["A-J1", "J1-CORE", "J1-J2", "J2-CORE", "J2-D"]),
            ("R1", "E-J3", "J3-F", 10, 1, "regular", ["E-J3", "J3-CORE", "J3-F"]),
            ("R2", "C-J2", "J2-D", 10, 1, "regular", ["C-J2", "J2-CORE", "J2-D"]),
        ]
        for tid, start, dest, arrival, priority, train_type, route in trains_config:
            self._trains[tid] = TrainState(
                train_id=tid, current_segment=start, destination=dest,
                status=TrainStatus.MOVING, speed=1.0, scheduled_arrival=arrival,
                priority=priority, train_type=train_type,
            )
            self._train_routes[tid] = route
            self._track_segments[start].occupied_by = tid

    def _calculate_route(self, start: str, dest: str) -> List[str]:
        if start not in self._track_segments:
            return [start]
        visited: Set[str] = set()
        queue: List[Tuple[str, List[str]]] = [(start, [start])]
        while queue:
            current, path = queue.pop(0)
            if current == dest:
                return path
            if current in visited:
                continue
            visited.add(current)
            segment = self._track_segments.get(current)
            if segment:
                for next_seg in segment.next_segments:
                    if next_seg not in visited:
                        queue.append((next_seg, path + [next_seg]))
        return [start]

    # ------------------------------------------------------------------ #
    # Dynamics (ported verbatim)
    # ------------------------------------------------------------------ #
    def _simulate_trains(self):
        trains_to_move = []

        for train_id, train in self._trains.items():
            if train.status in (TrainStatus.ARRIVED, TrainStatus.DELAYED):
                continue
            if train_id in self._held_trains:
                continue

            route = self._train_routes.get(train_id, [])
            current_idx = route.index(train.current_segment) if train.current_segment in route else -1

            if current_idx >= 0 and current_idx < len(route) - 1:
                next_segment_id = route[current_idx + 1]
                next_seg = self._track_segments.get(next_segment_id)
                current_seg = self._track_segments.get(train.current_segment)
                if next_seg is None or current_seg is None:
                    continue

                if next_segment_id in self._disabled_segments:
                    train.status = TrainStatus.WAITING
                    continue

                if next_seg.occupied_by is not None:
                    train.status = TrainStatus.WAITING
                    train.speed = 0.0
                    continue

                if next_seg.signal_state == SignalState.RED:
                    train.status = TrainStatus.WAITING
                    train.speed = 0.0
                    continue

                if next_seg.signal_state == SignalState.YELLOW:
                    train.status = TrainStatus.WAITING
                    train.speed = 0.5
                    next_seg.signal_state = SignalState.GREEN
                    continue

                delay_bonus = min(train.delay * 0.1, 0.5)
                effective_priority = train.priority + delay_bonus
                trains_to_move.append(
                    (train_id, train, current_seg, next_seg, next_segment_id, effective_priority)
                )

            elif current_idx == len(route) - 1:
                current_seg = self._track_segments.get(train.current_segment)
                train.speed = 0.0
                if current_seg and current_seg.occupied_by == train_id:
                    current_seg.occupied_by = None
                if self._step_count > train.scheduled_arrival:
                    train.delay = self._step_count - train.scheduled_arrival
                    train.status = TrainStatus.DELAYED
                else:
                    train.status = TrainStatus.ARRIVED

        trains_to_move.sort(key=lambda x: x[5], reverse=True)

        if self._weather_active:
            weather_delayed = []
            for item in trains_to_move:
                if random.random() <= self._weather_speed_modifier:
                    weather_delayed.append(item)
                else:
                    item[1].status = TrainStatus.WAITING
                    item[1].speed = 0.3
            trains_to_move = weather_delayed

        moved_segments: Set[str] = set()
        for train_id, train, current_seg, next_seg, next_segment_id, _ in trains_to_move:
            if next_seg.occupied_by is not None or next_segment_id in moved_segments:
                train.status = TrainStatus.WAITING
                train.speed = 0.0
                continue
            if current_seg.occupied_by == train_id:
                current_seg.occupied_by = None
            train.current_segment = next_segment_id
            next_seg.occupied_by = train_id
            train.status = TrainStatus.MOVING
            train.speed = 1.0
            moved_segments.add(next_segment_id)

    def _check_collisions(self):
        segment_occupancy: Dict[str, List[str]] = {}
        for train_id, train in self._trains.items():
            if train.status not in (TrainStatus.ARRIVED, TrainStatus.DELAYED):
                segment_occupancy.setdefault(train.current_segment, []).append(train_id)

        for seg_id, trains in segment_occupancy.items():
            if len(trains) > 1:
                self._collisions += 1
                self._collisions_this_step += 1
                trains_sorted = sorted(trains, key=lambda t: self._trains[t].priority, reverse=True)
                for i, tid in enumerate(trains_sorted):
                    self._trains[tid].status = TrainStatus.WAITING
                    self._trains[tid].speed = 0.0
                    if i == 0:
                        self._track_segments[seg_id].occupied_by = tid
                    else:
                        self._held_trains.add(tid)
                if self.verbose:
                    print(f"[COLLISION] Trains {trains} collided at segment {seg_id}", flush=True)

    def _calculate_base_reward(self) -> float:
        reward = 0.0
        reward -= self._collisions_this_step * 0.5
        for tid, train in self._trains.items():
            if train.status in (TrainStatus.ARRIVED, TrainStatus.DELAYED) and tid not in self._arrived_trains:
                self._arrived_trains.add(tid)
                if train.delay == 0:
                    reward += 0.2 * train.priority
                else:
                    reward -= 0.05 * min(train.delay, 5)
        waiting = sum(1 for t in self._trains.values() if t.status == TrainStatus.WAITING)
        reward -= 0.01 * waiting
        return max(0.001, min(0.999, reward + 0.5))

    def _is_done(self) -> bool:
        if self._step_count >= self._max_steps:
            return True
        return all(
            t.status in (TrainStatus.ARRIVED, TrainStatus.DELAYED)
            for t in self._trains.values()
        )

    # ------------------------------------------------------------------ #
    # Grading / eval helpers (ported)
    # ------------------------------------------------------------------ #
    def get_final_state(self) -> dict:
        return {
            "trains": {tid: train.model_dump() for tid, train in self._trains.items()},
            "collisions": self._collisions,
            "step": self._step_count,
            "max_steps": self._max_steps,
            "task_name": self._task_name,
            "weather_active": self._weather_active,
        }

    def grade_task(self) -> TaskResult:
        trains_arrived = sum(
            1 for t in self._trains.values()
            if t.status in (TrainStatus.ARRIVED, TrainStatus.DELAYED)
        )
        trains_delayed = sum(1 for t in self._trains.values() if t.delay > 0)
        avg_delay = sum(t.delay for t in self._trains.values()) / max(len(self._trains), 1)
        total_trains = len(self._trains)
        arrival_score = trains_arrived / total_trains if total_trains > 0 else 0
        delay_penalty = min(avg_delay * 0.05, 0.3)
        collision_penalty = min(self._collisions * 0.2, 0.5)
        score = max(0.0, min(1.0, arrival_score - delay_penalty - collision_penalty))
        return TaskResult(
            task_name=self._task_name, score=score,
            trains_arrived=trains_arrived, trains_delayed=trains_delayed,
            collisions=self._collisions, avg_delay=avg_delay,
            message=(f"{trains_arrived}/{total_trains} arrived, "
                     f"{self._collisions} collisions, avg delay {avg_delay:.1f}"),
        )
