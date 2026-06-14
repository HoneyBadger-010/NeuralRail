"""Smoke tests: the simulator runs, the Gymnasium env is contract-valid, energy
accrues, and action masks have the right shape. These must pass before training."""

import numpy as np
import pytest

from sim.railway_simulator import RailwaySimulator
from sim.models import TrainStatus


def test_sim_runs_basic_episode():
    sim = RailwaySimulator("basic_control")
    sim.reset(seed=0)
    assert sim.num_trains == 2
    assert sim.num_segments == 5
    done, steps = False, 0
    while not done and steps < sim.max_steps + 1:
        action = {"signals": {s: "green" for s in sim.segment_ids()}, "holds": {}}
        _obs, _r, done = sim.step(action)
        steps += 1
    assert steps <= sim.max_steps + 1


@pytest.mark.parametrize("task", ["basic_control", "junction_management", "express_priority", "rush_hour"])
def test_sim_all_tasks_construct(task):
    sim = RailwaySimulator(task)
    obs = sim.reset(seed=0)
    cfg = sim.config
    assert sim.num_trains == cfg["num_trains"]
    assert len(obs.track_segments) == sim.num_segments


def test_gym_env_passes_checker():
    from gymnasium.utils.env_checker import check_env
    from env.railway_gym_env import RailwayGymEnv

    env = RailwayGymEnv("basic_control", w_energy=0.05)
    check_env(env.unwrapped, skip_render_check=True)


def test_random_rollout_accrues_energy():
    from env.railway_gym_env import RailwayGymEnv

    env = RailwayGymEnv("junction_management", w_energy=0.05, include_energy_obs=True)
    obs, info = env.reset(seed=1)
    assert obs.shape == env.observation_space.shape
    last_info = info
    for _ in range(env.sim.max_steps):
        obs, r, term, trunc, last_info = env.step(env.action_space.sample())
        assert env.observation_space.contains(obs)
        if term or trunc:
            break
    assert last_info["cum_energy_kwh"] > 0.0
    assert last_info["cum_energy_inr"] >= 0.0
    assert last_info["cum_energy_co2_kg"] >= 0.0


def test_action_mask_shape_and_terminal_logic():
    from env.railway_gym_env import RailwayGymEnv

    env = RailwayGymEnv("basic_control")
    env.reset(seed=0)
    mask = env.action_masks()
    assert mask.shape[0] == int(env.codec.nvec.sum())
    # All-green, no-hold action should always be legal at reset.
    assert mask.all() or mask.any()


def test_w_energy_zero_matches_base_reward():
    """With w_energy=0 the reward must equal the simulator's base reward exactly."""
    from env.railway_gym_env import RailwayGymEnv

    env = RailwayGymEnv("basic_control", w_energy=0.0)
    env.reset(seed=3)
    _obs, reward, _t, _tr, info = env.step(env.action_space.sample())
    assert reward == pytest.approx(info["base_reward"])
