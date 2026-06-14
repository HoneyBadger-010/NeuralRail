"""
Baseline controllers to measure the RL agent against.

Every controller (baselines AND the RL policy) implements the same interface:

    controller.act(sim) -> action_dict   # {"signals": {...}, "holds": {...}}
    controller.reset()                    # optional, per-episode

so the evaluation harness drives the SAME simulator with each, computing energy
identically — an apples-to-apples comparison.

Baselines may read simulator internals (routes, occupancy); that mirrors what a
real central controller sees and is fair, since they run server-side.
"""

from __future__ import annotations

import random
from typing import Optional

from sim.models import TrainStatus

_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)


class NoControlAgent:
    """All signals green, never hold. The 'do nothing' lower bound."""

    name = "no_control"

    def reset(self):
        pass

    def act(self, sim) -> dict:
        return {
            "signals": {s: "green" for s in sim.segment_ids()},
            "holds": {t: False for t in sim.train_ids()},
        }


class RandomAgent:
    """Random signals and random holds — a chaotic reference."""

    name = "random"

    def __init__(self, seed: int = 0):
        self._rng = random.Random(seed)

    def reset(self):
        pass

    def act(self, sim) -> dict:
        obs = sim.observation
        signals = {s: self._rng.choice(["red", "yellow", "green"]) for s in sim.segment_ids()}
        holds = {}
        for tid in sim.train_ids():
            t = obs.trains.get(tid)
            if t is not None and t.status not in _TERMINAL:
                holds[tid] = bool(self._rng.random() < 0.3)
            else:
                holds[tid] = False
        return {"signals": signals, "holds": holds}


class GreedyPriorityAgent:
    """Priority-respecting greedy heuristic (ported from the gym-env's control
    suggestions / NeuralRail's conflict resolver intuition):

      * all signals green by default,
      * for every segment that two or more non-terminal trains want to enter
        next, hold all but the highest-priority contender,
      * release everyone else.

    A sensible, safety-respecting baseline that the RL agent must beat on the
    JOINT objective (delay + collisions + energy), especially energy.
    """

    name = "greedy_priority"

    def reset(self):
        pass

    def act(self, sim) -> dict:
        trains = sim._trains
        routes = sim._train_routes
        segments = sim._track_segments

        signals = {s: "green" for s in sim.segment_ids()}
        holds = {tid: False for tid, t in trains.items() if t.status not in _TERMINAL}

        # Map each desired next-segment -> contending (train_id, priority).
        contenders: dict[str, list[tuple[str, int]]] = {}
        for tid, t in trains.items():
            if t.status in _TERMINAL:
                continue
            route = routes.get(tid, [])
            if t.current_segment not in route:
                continue
            idx = route.index(t.current_segment)
            if idx >= len(route) - 1:
                continue
            nxt = route[idx + 1]
            contenders.setdefault(nxt, []).append((tid, t.priority))

        for nxt, group in contenders.items():
            occupied = segments[nxt].occupied_by is not None if nxt in segments else False
            # Contention if >1 wants it, or it's already occupied by someone else.
            if len(group) > 1 or occupied:
                # Highest priority (then earliest id) wins; hold the rest.
                group.sort(key=lambda x: (-x[1], x[0]))
                for tid, _ in group[1:]:
                    holds[tid] = True

        return {"signals": signals, "holds": holds}


def make_baseline(name: str, seed: int = 0):
    if name == "no_control":
        return NoControlAgent()
    if name == "random":
        return RandomAgent(seed=seed)
    if name == "greedy_priority":
        return GreedyPriorityAgent()
    raise ValueError(f"Unknown baseline: {name}")


BASELINE_NAMES = ["no_control", "random", "greedy_priority"]
