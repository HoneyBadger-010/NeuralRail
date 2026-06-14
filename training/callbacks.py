"""Training callbacks: log energy/throughput metrics and dump training curves
(reward, energy, collisions over time) to JSON for the UI 'training-proof' panel.
"""

from __future__ import annotations

import json
import os

from stable_baselines3.common.callbacks import BaseCallback


class MetricsCallback(BaseCallback):
    """Records per-episode metrics to TensorBoard and to a JSON curve file."""

    def __init__(self, curve_path: str, verbose: int = 0):
        super().__init__(verbose)
        self.curve_path = curve_path
        self.records: list[dict] = []

    def _on_step(self) -> bool:
        infos = self.locals.get("infos", [])
        dones = self.locals.get("dones", [])
        for info, done in zip(infos, dones):
            if not done:
                continue
            ep = info.get("episode", {})  # added by Monitor at episode end
            total_trains = max(int(info.get("total_trains", 1)), 1)
            rec = {
                "t": int(self.num_timesteps),
                "reward": float(ep.get("r", 0.0)),
                "len": int(ep.get("l", 0)),
                "energy_kwh": float(info.get("cum_energy_kwh", 0.0)),
                "energy_inr": float(info.get("cum_energy_inr", 0.0)),
                "co2_kg": float(info.get("cum_energy_co2_kg", 0.0)),
                "collisions": int(info.get("collisions", 0)),
                "arrival_rate": float(info.get("trains_arrived", 0)) / total_trains,
                "total_delay": int(info.get("total_delay", 0)),
            }
            self.records.append(rec)
            self.logger.record_mean("rollout/ep_energy_kwh", rec["energy_kwh"])
            self.logger.record_mean("rollout/ep_collisions", rec["collisions"])
            self.logger.record_mean("rollout/ep_arrival_rate", rec["arrival_rate"])
            self.logger.record_mean("rollout/ep_total_delay", rec["total_delay"])
        return True

    def _dump(self):
        os.makedirs(os.path.dirname(self.curve_path) or ".", exist_ok=True)
        with open(self.curve_path, "w") as f:
            json.dump(self.records, f)

    def _on_training_end(self) -> None:
        self._dump()
