"""
ActionCodec — maps the MultiDiscrete RL action to the simulator's action dict,
and computes action masks for MaskablePPO.

Action space:  MultiDiscrete([3]*S + [2]*T)
  * first S dims: signal aspect per segment  (0=red, 1=yellow, 2=green)
  * next  T dims: per train                  (0=release/run, 1=hold)

Masking (flat array of length sum(nvec), partitioned per sub-action):
  * signal dims: all three aspects always selectable
  * train dims:  'release' (0) always valid; 'hold' (1) masked off for trains
                 that have already ARRIVED/DELAYED (holding them is a no-op)
"""

from __future__ import annotations

from typing import List

import numpy as np

from sim.models import RailwayObservation, TrainStatus

_SIGNAL_BY_IDX = {0: "red", 1: "yellow", 2: "green"}
_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)


class ActionCodec:
    def __init__(self, segment_ids: List[str], train_ids: List[str]):
        self.segment_ids = list(segment_ids)
        self.train_ids = list(train_ids)
        self.nvec = np.array([3] * len(self.segment_ids) + [2] * len(self.train_ids), dtype=np.int64)

    def unpack(self, action) -> dict:
        action = np.asarray(action).astype(int).ravel()
        signals = {}
        for i, sid in enumerate(self.segment_ids):
            signals[sid] = _SIGNAL_BY_IDX.get(int(action[i]), "green")
        holds = {}
        off = len(self.segment_ids)
        for j, tid in enumerate(self.train_ids):
            holds[tid] = bool(int(action[off + j]) == 1)
        return {"signals": signals, "holds": holds}

    def action_masks(self, obs: RailwayObservation) -> np.ndarray:
        mask: List[bool] = []
        # Signal dims — all aspects always valid.
        for _ in self.segment_ids:
            mask.extend([True, True, True])
        # Train dims — release always valid; hold only if not terminal.
        for tid in self.train_ids:
            t = obs.trains.get(tid)
            can_hold = not (t is not None and t.status in _TERMINAL)
            mask.extend([True, can_hold])
        return np.array(mask, dtype=bool)
