"""
RailwayGymEnv — the Gymnasium wrapper that composes the simulator + energy model
into a trainable RL environment. This is the integration heart: it is the ONE
place that knows about Gymnasium, and it is imported by BOTH the training code
and the live server, so there is no separate obs/action contract to keep in sync.

Reward = base_reward(delays + collisions + priority)  -  w_energy * norm_energy

The energy term is folded in here (not in the simulator), so:
  * the simulator stays usable standalone, and
  * w_energy=0 exactly reproduces the original gym-env agent (clean ablation).
"""

from __future__ import annotations

import random
from typing import Optional

import gymnasium as gym
import numpy as np
from gymnasium import spaces

from configs.tasks import STEP_SECONDS
from physics.attribute_mapper import PhysicalAttributeMapper
from physics.econ_constants import kwh_to_co2_kg, kwh_to_inr
from physics.energy_model import EnergyModel
from sim.models import TrainStatus
from sim.railway_simulator import RailwaySimulator

from .action_codec import ActionCodec
from .obs_encoder import ObservationEncoder

# Per-task energy normalisation reference (kWh per step). Calibrated as the ~p95
# of the no-control baseline's per-step energy; overridable via constructor.
DEFAULT_ENERGY_REF_KWH = {
    # Calibrated p95 of no-control per-step energy (eval/run_eval.py --calibrate).
    "basic_control": 48.0,
    "junction_management": 57.0,
    "express_priority": 97.0,
    "rush_hour": 67.0,
}
_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)


class RailwayGymEnv(gym.Env):
    metadata = {"render_modes": []}

    def __init__(
        self,
        task_name: str = "basic_control",
        w_energy: float = 0.0,
        energy_ref_kwh: Optional[float] = None,
        include_energy_obs: bool = False,
        use_masking: bool = True,
        randomize: bool = False,
        step_seconds: float = STEP_SECONDS,
    ):
        super().__init__()
        self.task_name = task_name
        self.w_energy = float(w_energy)
        self.include_energy_obs = include_energy_obs
        self.use_masking = use_masking
        self.randomize = randomize

        self.sim = RailwaySimulator(task_name)
        self._segment_ids = self.sim.segment_ids()
        self._train_ids = self.sim.train_ids()
        seg_lengths = {sid: self.sim._track_segments[sid].length for sid in self._segment_ids}

        self.energy_ref_kwh = float(
            energy_ref_kwh if energy_ref_kwh is not None
            else DEFAULT_ENERGY_REF_KWH.get(task_name, 150.0)
        )

        self.mapper = PhysicalAttributeMapper()
        self.energy_model = EnergyModel(self.mapper, step_seconds=step_seconds)
        self.encoder = ObservationEncoder(
            self._segment_ids, self._train_ids, self.sim.max_steps,
            seg_lengths, include_energy_obs=include_energy_obs,
            energy_ref_kwh=self.energy_ref_kwh,
        )
        self.codec = ActionCodec(self._segment_ids, self._train_ids)

        self.observation_space = spaces.Box(low=0.0, high=1.0, shape=(self.encoder.dim,), dtype=np.float32)
        self.action_space = spaces.MultiDiscrete(self.codec.nvec)

        self._cum_kwh = 0.0
        self._last_per_train_kwh: dict = {}

    # ------------------------------------------------------------------ #
    def _energy_ctx(self) -> dict:
        return {"per_train_kwh": self._last_per_train_kwh, "cum_kwh": self._cum_kwh}

    def reset(self, *, seed: Optional[int] = None, options: Optional[dict] = None):
        super().reset(seed=seed)
        task = (options or {}).get("task_name")
        obs = self.sim.reset(seed=seed, task_name=task)
        if task and task != self.task_name:
            # Layout changed — rebuild encoders/codec (only used for eval task-switching).
            self.task_name = task
            self._segment_ids = self.sim.segment_ids()
            self._train_ids = self.sim.train_ids()
            seg_lengths = {sid: self.sim._track_segments[sid].length for sid in self._segment_ids}
            self.encoder = ObservationEncoder(
                self._segment_ids, self._train_ids, self.sim.max_steps, seg_lengths,
                include_energy_obs=self.include_energy_obs, energy_ref_kwh=self.energy_ref_kwh,
            )
            self.codec = ActionCodec(self._segment_ids, self._train_ids)
            self.observation_space = spaces.Box(low=0.0, high=1.0, shape=(self.encoder.dim,), dtype=np.float32)
            self.action_space = spaces.MultiDiscrete(self.codec.nvec)

        if self.randomize:
            self._jitter_schedules(seed)

        self._cum_kwh = 0.0
        self._last_per_train_kwh = {}
        return self.encoder.encode(obs, self._energy_ctx()), self._info(obs, base=0.0, energy_kwh=0.0)

    def step(self, action):
        prev_obs = self.sim.observation
        action_dict = self.codec.unpack(action)
        next_obs, base, done = self.sim.step(action_dict)

        e = self.energy_model.step_energy(prev_obs, next_obs)
        norm_e = min(e.total_kwh / self.energy_ref_kwh, 1.0)
        reward = float(base - self.w_energy * norm_e)

        self._cum_kwh += e.total_kwh
        self._last_per_train_kwh = e.per_train_kwh

        all_arrived = all(t.status in _TERMINAL for t in next_obs.trains.values())
        terminated = bool(all_arrived)
        truncated = bool(self.sim.step_count >= self.sim.max_steps and not all_arrived)

        obs_vec = self.encoder.encode(next_obs, self._energy_ctx())
        info = self._info(next_obs, base=base, energy_kwh=e.total_kwh, energy_term=self.w_energy * norm_e)
        return obs_vec, reward, terminated, truncated, info

    def action_masks(self) -> np.ndarray:
        """Hook used by sb3-contrib MaskablePPO."""
        return self.codec.action_masks(self.sim.observation)

    # ------------------------------------------------------------------ #
    def _info(self, obs, base: float, energy_kwh: float, energy_term: float = 0.0) -> dict:
        arrived = sum(1 for t in obs.trains.values() if t.status in _TERMINAL)
        on_time = sum(1 for t in obs.trains.values() if t.status == TrainStatus.ARRIVED)
        total_delay = sum(t.delay for t in obs.trains.values())
        return {
            "base_reward": float(base),
            "energy_term": float(energy_term),
            "step_energy_kwh": float(energy_kwh),
            "cum_energy_kwh": float(self._cum_kwh),
            "cum_energy_inr": float(kwh_to_inr(self._cum_kwh)),
            "cum_energy_co2_kg": float(kwh_to_co2_kg(self._cum_kwh)),
            "collisions": int(obs.collisions),
            "collisions_this_step": int(self.sim.collisions_this_step),
            "trains_arrived": arrived,
            "trains_on_time": on_time,
            "total_trains": len(obs.trains),
            "total_delay": int(total_delay),
        }

    def _jitter_schedules(self, seed: Optional[int]):
        rng = random.Random(seed)
        for t in self.sim._trains.values():
            jitter = rng.randint(-2, 2)
            t.scheduled_arrival = max(1, t.scheduled_arrival + jitter)
