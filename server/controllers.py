"""
Controller factory + a manual/override controller + a decision explainer that
turns each step's action into a human-readable feed ("HELD freight FRT — lower
priority than express EX1").
"""

from __future__ import annotations

import os
from typing import Optional

from agents.baselines import GreedyPriorityAgent, make_baseline
from agents.policy_loader import load_rl_controller
from sim.models import TrainStatus

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)
BASE_MODES = ["no_control", "random", "greedy_priority", "manual"]


def has_checkpoint(task: str) -> bool:
    return os.path.exists(os.path.join(ROOT, "models", task, "best.zip"))


def available_modes(task: str) -> list[str]:
    modes = list(BASE_MODES)
    if has_checkpoint(task):
        modes.insert(0, "rl")
    return modes


class ManualController:
    """Autonomous base controller + sticky human overrides (decision-support).

    Overrides persist until changed/cleared, and are layered on top of the base
    controller's action each step — so a controller still runs the rest of the
    network while the operator pins specific trains/signals.
    """

    name = "manual"

    def __init__(self, base):
        self.base = base
        self.signal_overrides: dict[str, str] = {}
        self.hold_overrides: dict[str, bool] = {}

    def reset(self):
        if hasattr(self.base, "reset"):
            self.base.reset()
        self.signal_overrides.clear()
        self.hold_overrides.clear()

    def set_override(self, kind: str, target: str, value: Optional[str] = None):
        if kind == "signal" and value:
            self.signal_overrides[target] = value
        elif kind == "hold":
            self.hold_overrides[target] = True
        elif kind == "release":
            self.hold_overrides[target] = False
        elif kind == "clear":
            self.signal_overrides.pop(target, None)
            self.hold_overrides.pop(target, None)

    def act(self, sim) -> dict:
        action = self.base.act(sim)
        action["signals"].update(self.signal_overrides)
        action["holds"].update(self.hold_overrides)
        return action


def build_controller(mode: str, task: str):
    """Return (controller, effective_mode). Falls back gracefully if RL is asked
    for but no checkpoint exists."""
    if mode == "rl":
        rl = load_rl_controller(task)
        if rl is not None:
            return rl, "rl"
        # graceful fallback so the demo always works pre-training
        return GreedyPriorityAgent(), "greedy_priority"
    if mode == "manual":
        base = load_rl_controller(task) or GreedyPriorityAgent()
        return ManualController(base), "manual"
    return make_baseline(mode), mode


def explain_decision(sim, action: dict, prev_action: Optional[dict]) -> list[dict]:
    """Produce decision-feed entries for what changed this step + why."""
    entries: list[dict] = []
    trains = sim._trains
    routes = sim._train_routes
    prev_holds = (prev_action or {}).get("holds", {})

    # Contention map: who else wants each next segment (for the "why").
    wants: dict[str, list[str]] = {}
    for tid, t in trains.items():
        if t.status in _TERMINAL:
            continue
        route = routes.get(tid, [])
        if t.current_segment in route:
            idx = route.index(t.current_segment)
            if idx < len(route) - 1:
                wants.setdefault(route[idx + 1], []).append(tid)

    for tid, hold in action.get("holds", {}).items():
        t = trains.get(tid)
        if t is None or t.status in _TERMINAL:
            continue
        if hold and not prev_holds.get(tid, False):
            # Why: a higher-priority train wants the same next block.
            reason = "buffering for flow"
            route = routes.get(tid, [])
            if t.current_segment in route:
                idx = route.index(t.current_segment)
                if idx < len(route) - 1:
                    nxt = route[idx + 1]
                    rivals = [r for r in wants.get(nxt, []) if r != tid]
                    higher = [r for r in rivals if trains[r].priority > t.priority]
                    if higher:
                        reason = f"yield {nxt} to higher-priority {higher[0]}"
                    elif rivals:
                        reason = f"resolve contention at {nxt}"
            entries.append({
                "kind": "hold",
                "text": f"HOLD {tid} ({t.get_priority_name()})",
                "reason": reason,
            })
        elif (not hold) and prev_holds.get(tid, False):
            entries.append({
                "kind": "release",
                "text": f"RELEASE {tid} ({t.get_priority_name()})",
                "reason": "path clear",
            })

    return entries
