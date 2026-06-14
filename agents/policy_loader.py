"""
Load a trained policy and wrap it as a controller with the same act(sim)->action
interface as the baselines, so it drops straight into the eval harness and the
live server.
"""

from __future__ import annotations

import os
from typing import Optional

from env.action_codec import ActionCodec
from env.obs_encoder import ObservationEncoder
from sim.railway_simulator import RailwaySimulator


def load_model(path: str, use_masking: bool = True):
    """Load a MaskablePPO (or PPO) checkpoint."""
    if use_masking:
        from sb3_contrib import MaskablePPO
        return MaskablePPO.load(path, device="cpu")
    from stable_baselines3 import PPO
    return PPO.load(path, device="cpu")


class RLController:
    """Wraps a trained policy. Builds observations with the SAME encoder/codec
    the env used (rebuilt from a fresh sim of the task), so there is no layout
    drift between training and inference."""

    name = "rl_agent"

    def __init__(
        self,
        model,
        task_name: str,
        use_masking: bool = True,
        include_energy_obs: bool = False,
        energy_ref_kwh: float = 150.0,
    ):
        self.model = model
        self.use_masking = use_masking
        self.include_energy_obs = include_energy_obs

        ref_sim = RailwaySimulator(task_name)
        seg_ids = ref_sim.segment_ids()
        train_ids = ref_sim.train_ids()
        seg_lengths = {s: ref_sim._track_segments[s].length for s in seg_ids}
        self.encoder = ObservationEncoder(
            seg_ids, train_ids, ref_sim.max_steps, seg_lengths,
            include_energy_obs=include_energy_obs, energy_ref_kwh=energy_ref_kwh,
        )
        self.codec = ActionCodec(seg_ids, train_ids)
        self._cum_kwh = 0.0
        self._last_per_train: dict = {}

    def reset(self):
        self._cum_kwh = 0.0
        self._last_per_train = {}

    def update_energy(self, per_train_kwh: dict, total_kwh: float):
        """Called by the harness after each step so energy obs stays in sync."""
        self._last_per_train = per_train_kwh
        self._cum_kwh += total_kwh

    def act(self, sim) -> dict:
        obs = sim.observation
        ctx = {"per_train_kwh": self._last_per_train, "cum_kwh": self._cum_kwh}
        vec = self.encoder.encode(obs, ctx)
        if self.use_masking:
            mask = self.codec.action_masks(obs)
            action, _ = self.model.predict(vec, action_masks=mask, deterministic=True)
        else:
            action, _ = self.model.predict(vec, deterministic=True)
        return self.codec.unpack(action)


def load_rl_controller(task_name: str, model_path: Optional[str] = None, **kwargs):
    """Convenience: default path is models/<task>/best.zip."""
    if model_path is None:
        root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        model_path = os.path.join(root, "models", task_name, "best.zip")
    if not os.path.exists(model_path):
        return None
    model = load_model(model_path, use_masking=kwargs.get("use_masking", True))
    return RLController(model, task_name, **kwargs)
