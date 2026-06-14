"""
ObservationEncoder — the single source of truth for the observation layout.

Flattens a RailwayObservation into a fixed, normalised float32 vector that the
PPO policy consumes. Layout (all values normalised to ~[0, 1]):

  per train  (T trains, 6 features):
      [ current_segment_idx/S, destination_idx/S, status_ord/3,
        speed, priority/3, delay/max_steps ]
  per segment (S segments, 5 features):
      [ signal_ord/2, occupied(0|1), is_junction(0|1), is_station(0|1),
        length/max_len ]
  globals (2):
      [ step/max_steps, min(collisions/T, 1) ]
  optional energy features (T + 1), if include_energy_obs:
      [ per-train last-step kWh / ref ... , cumulative kWh / (ref*max_steps) ]
"""

from __future__ import annotations

from typing import Dict, List, Optional

import numpy as np

from sim.models import RailwayObservation, SignalState, TrainStatus

_STATUS_ORD = {
    TrainStatus.WAITING: 0,
    TrainStatus.MOVING: 1,
    TrainStatus.ARRIVED: 2,
    TrainStatus.DELAYED: 3,
}
_SIGNAL_ORD = {SignalState.RED: 0, SignalState.YELLOW: 1, SignalState.GREEN: 2}

TRAIN_FEATS = 6
SEGMENT_FEATS = 5
GLOBAL_FEATS = 2


class ObservationEncoder:
    def __init__(
        self,
        segment_ids: List[str],
        train_ids: List[str],
        max_steps: int,
        segment_lengths: Dict[str, float],
        include_energy_obs: bool = False,
        energy_ref_kwh: float = 150.0,
    ):
        self.segment_ids = list(segment_ids)
        self.train_ids = list(train_ids)
        self.max_steps = max(1, int(max_steps))
        self.include_energy_obs = include_energy_obs
        self.energy_ref_kwh = max(1e-6, energy_ref_kwh)

        self._seg_index = {sid: i for i, sid in enumerate(self.segment_ids)}
        self._num_seg = max(1, len(self.segment_ids))
        self._num_train = max(1, len(self.train_ids))
        self._max_len = max(1.0, max(segment_lengths.values()) if segment_lengths else 1.0)

        self.dim = (
            len(self.train_ids) * TRAIN_FEATS
            + len(self.segment_ids) * SEGMENT_FEATS
            + GLOBAL_FEATS
            + ((len(self.train_ids) + 1) if include_energy_obs else 0)
        )

    def _seg_idx_norm(self, seg_id: Optional[str]) -> float:
        i = self._seg_index.get(seg_id, -1)
        return (i / self._num_seg) if i >= 0 else 0.0

    def encode(
        self,
        obs: RailwayObservation,
        energy_ctx: Optional[dict] = None,
    ) -> np.ndarray:
        vec = np.zeros(self.dim, dtype=np.float32)
        k = 0

        # Per-train block
        for tid in self.train_ids:
            t = obs.trains.get(tid)
            if t is not None:
                vec[k + 0] = self._seg_idx_norm(t.current_segment)
                vec[k + 1] = self._seg_idx_norm(t.destination)
                vec[k + 2] = _STATUS_ORD.get(t.status, 0) / 3.0
                vec[k + 3] = float(t.speed)
                vec[k + 4] = (t.priority / 3.0)
                vec[k + 5] = min(t.delay / self.max_steps, 1.0)
            k += TRAIN_FEATS

        # Per-segment block
        for sid in self.segment_ids:
            s = obs.track_segments.get(sid)
            if s is not None:
                vec[k + 0] = _SIGNAL_ORD.get(s.signal_state, 2) / 2.0
                vec[k + 1] = 1.0 if s.occupied_by else 0.0
                vec[k + 2] = 1.0 if s.is_junction else 0.0
                vec[k + 3] = 1.0 if s.station_name else 0.0
                vec[k + 4] = min(float(s.length) / self._max_len, 1.0)
            k += SEGMENT_FEATS

        # Globals
        vec[k + 0] = min(obs.current_step / self.max_steps, 1.0)
        vec[k + 1] = min(obs.collisions / self._num_train, 1.0)
        k += GLOBAL_FEATS

        # Optional energy features
        if self.include_energy_obs:
            per = (energy_ctx or {}).get("per_train_kwh", {})
            for tid in self.train_ids:
                vec[k] = min(per.get(tid, 0.0) / self.energy_ref_kwh, 1.0)
                k += 1
            cum = (energy_ctx or {}).get("cum_kwh", 0.0)
            vec[k] = min(cum / (self.energy_ref_kwh * self.max_steps), 1.0)
            k += 1

        return vec
