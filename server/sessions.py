"""
Live episode sessions: each holds a simulator + controller and produces compact
UI frames per step. Also the agent-vs-baseline `compare` runner.
"""

from __future__ import annotations

import uuid
from collections import OrderedDict
from typing import Optional

from physics.attribute_mapper import PhysicalAttributeMapper
from physics.econ_constants import kwh_to_co2_kg, kwh_to_inr
from physics.energy_model import EnergyModel
from sim.models import TrainStatus
from sim.railway_simulator import RailwaySimulator

from .controllers import build_controller, explain_decision

_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)
MAX_SESSIONS = 64

SCENARIO_META = {
    "basic_control": {
        "name": "Basic Crossing", "difficulty": "easy",
        "description": "Two trains converge on a single shared crossing — the textbook conflict.",
    },
    "express_priority": {
        "name": "Express Priority", "difficulty": "medium-hard",
        "description": "Cascading conflicts across three junctions; high-speed trains on tight schedules.",
    },
    "junction_management": {
        "name": "Junction Management", "difficulty": "medium",
        "description": "Four trains with crossing routes through two junctions.",
    },
    "rush_hour": {
        "name": "Rush Hour", "difficulty": "hard",
        "description": "Six trains, complex network, delays and weather — the congestion stress test.",
    },
}


def _train_view(t) -> dict:
    return {
        "id": t.train_id, "segment": t.current_segment, "destination": t.destination,
        "status": t.status.value, "speed": round(t.speed, 2), "priority": t.priority,
        "type": t.train_type, "delay": t.delay,
    }


class Session:
    def __init__(self, task: str, mode: str, seed: int):
        self.id = str(uuid.uuid4())[:8]
        self.task = task
        self.seed = seed
        self.sim = RailwaySimulator(task)
        self.sim.reset(seed=seed)
        self.controller, self.mode = build_controller(mode, task)
        if hasattr(self.controller, "reset"):
            self.controller.reset()
        self.mapper = PhysicalAttributeMapper()
        self.energy = EnergyModel(self.mapper)
        self.cum_kwh = 0.0
        self.reward_total = 0.0
        self.prev_action: Optional[dict] = None
        self.last_decisions: list[dict] = []
        self.done = False

    def set_mode(self, mode: str):
        self.controller, self.mode = build_controller(mode, self.task)
        if hasattr(self.controller, "reset"):
            self.controller.reset()
        # keep the simulator where it is (hot-swap controller mid-episode)

    def set_override(self, kind: str, target: str, value: Optional[str]):
        if hasattr(self.controller, "set_override"):
            self.controller.set_override(kind, target, value)
            return True
        return False

    def _conflicts(self, obs) -> list[dict]:
        routes = self.sim._train_routes
        wants: dict[str, list[str]] = {}
        for tid, t in obs.trains.items():
            if t.status in _TERMINAL:
                continue
            route = routes.get(tid, [])
            if t.current_segment in route:
                idx = route.index(t.current_segment)
                if idx < len(route) - 1:
                    wants.setdefault(route[idx + 1], []).append(tid)
        out = []
        for seg, ts in wants.items():
            occ = obs.track_segments[seg].occupied_by if seg in obs.track_segments else None
            if len(ts) > 1:
                out.append({"segment": seg, "trains": ts, "type": "junction_contention"})
            elif occ and occ not in ts:
                out.append({"segment": seg, "trains": ts, "type": "block_occupied", "by": occ})
        return out

    def _metrics(self, obs, step_kwh: float) -> dict:
        arrived = sum(1 for t in obs.trains.values() if t.status in _TERMINAL)
        on_time = sum(1 for t in obs.trains.values() if t.status == TrainStatus.ARRIVED)
        total_delay = sum(t.delay for t in obs.trains.values())
        return {
            "step": self.sim.step_count, "max_steps": self.sim.max_steps,
            "step_energy_kwh": round(step_kwh, 2),
            "cum_energy_kwh": round(self.cum_kwh, 1),
            "cum_energy_inr": round(kwh_to_inr(self.cum_kwh)),
            "cum_energy_co2_kg": round(kwh_to_co2_kg(self.cum_kwh), 1),
            "collisions": obs.collisions,
            "reward_total": round(self.reward_total, 2),
            "trains_arrived": arrived, "trains_on_time": on_time,
            "total_trains": len(obs.trains), "total_delay": total_delay,
        }

    def frame(self, decisions=None, step_kwh: float = 0.0) -> dict:
        obs = self.sim.observation
        return {
            "episode_id": self.id, "task": self.task, "mode": self.mode,
            "t": self.sim.step_count, "max_steps": self.sim.max_steps,
            "done": self.done,
            "trains": {tid: _train_view(t) for tid, t in obs.trains.items()},
            "signals": {sid: s.signal_state.value for sid, s in obs.track_segments.items()},
            "occupied": {sid: s.occupied_by for sid, s in obs.track_segments.items()},
            "conflicts": self._conflicts(obs),
            "decisions": decisions if decisions is not None else self.last_decisions,
            "metrics": self._metrics(obs, step_kwh),
        }

    def step_once(self) -> dict:
        if self.done:
            return self.frame(decisions=[])
        prev_obs = self.sim.observation
        action = self.controller.act(self.sim)
        decisions = explain_decision(self.sim, action, self.prev_action)
        _nxt, base, done = self.sim.step(action)
        e = self.energy.step_energy(prev_obs, self.sim.observation)
        self.cum_kwh += e.total_kwh
        self.reward_total += base
        if hasattr(self.controller, "update_energy"):
            self.controller.update_energy(e.per_train_kwh, e.total_kwh)
        self.prev_action = action
        self.last_decisions = decisions
        self.done = done
        return self.frame(decisions=decisions, step_kwh=e.total_kwh)


class SessionManager:
    def __init__(self):
        self._sessions: "OrderedDict[str, Session]" = OrderedDict()

    def create(self, task: str, mode: str, seed: int) -> Session:
        s = Session(task, mode, seed)
        self._sessions[s.id] = s
        while len(self._sessions) > MAX_SESSIONS:
            self._sessions.popitem(last=False)
        return s

    def get(self, episode_id: str) -> Optional[Session]:
        return self._sessions.get(episode_id)


def run_to_completion(task: str, mode: str, seed: int) -> dict:
    """Run one controller to episode end; return per-step series + final KPIs."""
    s = Session(task, mode, seed)
    series = []
    guard = 0
    while not s.done and guard <= s.sim.max_steps + 1:
        f = s.step_once()
        m = f["metrics"]
        series.append({
            "t": m["step"], "cum_kwh": m["cum_energy_kwh"],
            "collisions": m["collisions"], "arrived": m["trains_arrived"],
            "total_delay": m["total_delay"],
        })
        guard += 1
    final = s.frame()["metrics"]
    return {"mode": s.mode, "series": series, "final": final}
