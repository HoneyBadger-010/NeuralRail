"""Environment factory for vectorised PPO training."""

from __future__ import annotations

from typing import Optional

from stable_baselines3.common.monitor import Monitor

from env.railway_gym_env import RailwayGymEnv


def make_env(
    task_name: str,
    w_energy: float = 0.0,
    energy_ref_kwh: Optional[float] = None,
    include_energy_obs: bool = False,
    use_masking: bool = True,
    randomize: bool = True,
    seed: int = 0,
    rank: int = 0,
):
    """Return a thunk that builds one Monitor-wrapped RailwayGymEnv.

    cloudpickle (used by SubprocVecEnv) can serialise this closure, so it works
    with both DummyVecEnv and SubprocVecEnv.
    """

    def _init():
        env = RailwayGymEnv(
            task_name,
            w_energy=w_energy,
            energy_ref_kwh=energy_ref_kwh,
            include_energy_obs=include_energy_obs,
            use_masking=use_masking,
            randomize=randomize,
        )
        env = Monitor(env)
        env.reset(seed=seed + rank)
        return env

    return _init
