"""
Train an energy-aware railway-control agent with MaskablePPO (sb3-contrib).

Run on a CUDA machine (e.g. RTX 4050) — device is auto-detected. Examples:

    # full training of the easy task
    python -m training.train_ppo --task basic_control

    # curriculum (run sequentially)
    python -m training.train_ppo --task express_priority
    python -m training.train_ppo --task junction_management
    python -m training.train_ppo --task rush_hour

    # energy ablation (reproduce the original non-energy agent)
    python -m training.train_ppo --task basic_control --w-energy 0 --out models/basic_control/ablation.zip

Outputs: models/<task>/best.zip  and  models/<task>/training_curves.json
TensorBoard logs under runs/.
"""

from __future__ import annotations

import sys as _sys
try:  # make ✓/box-drawing output work on Windows consoles (cp1252)
    _sys.stdout.reconfigure(encoding="utf-8")
    _sys.stderr.reconfigure(encoding="utf-8")
except Exception:
    pass

import argparse
import os

import yaml

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HYPERPARAMS = os.path.join(ROOT, "configs", "train_hyperparams.yaml")
REWARDS = os.path.join(ROOT, "configs", "reward_weights.yaml")


def _load_yaml(path: str) -> dict:
    with open(path) as f:
        return yaml.safe_load(f)


def main():
    p = argparse.ArgumentParser(description="Train MaskablePPO railway controller")
    p.add_argument("--task", default="basic_control",
                   choices=["basic_control", "express_priority", "junction_management", "rush_hour"])
    p.add_argument("--timesteps", type=int, default=None, help="override total_timesteps")
    p.add_argument("--n-envs", type=int, default=None, help="override parallel envs")
    p.add_argument("--n-steps", type=int, default=None)
    p.add_argument("--batch-size", type=int, default=None)
    p.add_argument("--w-energy", type=float, default=None, help="energy reward weight (0 = ablation)")
    p.add_argument("--energy-ref", type=float, default=None, help="energy normalisation ref (kWh/step)")
    p.add_argument("--energy-obs", action="store_true", help="include energy features in observation")
    p.add_argument("--no-mask", action="store_true", help="disable action masking (plain PPO)")
    p.add_argument("--no-randomize", action="store_true", help="disable schedule jitter (deterministic)")
    p.add_argument("--vec", choices=["dummy", "subproc"], default=None)
    p.add_argument("--seed", type=int, default=0)
    p.add_argument("--device", default="auto")
    p.add_argument("--out", default=None, help="checkpoint path (default models/<task>/best.zip)")
    args = p.parse_args()

    import torch
    from stable_baselines3.common.vec_env import DummyVecEnv, SubprocVecEnv

    from training.callbacks import MetricsCallback
    from training.make_env import make_env

    use_masking = not args.no_mask
    if use_masking:
        from sb3_contrib import MaskablePPO as Algo
    else:
        from stable_baselines3 import PPO as Algo

    hp = _load_yaml(HYPERPARAMS)[args.task]
    rw = _load_yaml(REWARDS)[args.task]

    n_envs = args.n_envs or hp["n_envs"]
    n_steps = args.n_steps or hp["n_steps"]
    batch_size = args.batch_size or hp["batch_size"]
    total_timesteps = args.timesteps or hp["total_timesteps"]
    w_energy = rw["w_energy"] if args.w_energy is None else args.w_energy
    energy_ref = args.energy_ref if args.energy_ref is not None else rw["energy_ref_kwh"]
    randomize = not args.no_randomize
    vec_kind = args.vec or ("subproc" if n_envs > 1 else "dummy")

    out = args.out or os.path.join(ROOT, "models", args.task, "best.zip")
    curve_path = os.path.join(os.path.dirname(out), "training_curves.json")
    os.makedirs(os.path.dirname(out), exist_ok=True)

    device = args.device
    if device == "auto":
        device = "cuda" if torch.cuda.is_available() else "cpu"

    print("=" * 70)
    print(f"Training {args.task} | masking={use_masking} | device={device}")
    print(f"  timesteps={total_timesteps} n_envs={n_envs} n_steps={n_steps} batch={batch_size}")
    print(f"  w_energy={w_energy} energy_ref_kwh={energy_ref} energy_obs={args.energy_obs}")
    if device == "cuda":
        print(f"  GPU: {torch.cuda.get_device_name(0)}")
    print("=" * 70)

    thunks = [
        make_env(args.task, w_energy=w_energy, energy_ref_kwh=energy_ref,
                 include_energy_obs=args.energy_obs, use_masking=use_masking,
                 randomize=randomize, seed=args.seed, rank=i)
        for i in range(n_envs)
    ]
    VecCls = SubprocVecEnv if vec_kind == "subproc" else DummyVecEnv
    venv = VecCls(thunks)

    model = Algo(
        hp["policy"], venv,
        policy_kwargs=dict(net_arch=hp["net_arch"]),
        n_steps=n_steps, batch_size=batch_size, n_epochs=hp["n_epochs"],
        gamma=hp["gamma"], gae_lambda=hp["gae_lambda"], clip_range=hp["clip_range"],
        ent_coef=hp["ent_coef"], learning_rate=hp["learning_rate"],
        vf_coef=hp["vf_coef"], max_grad_norm=hp["max_grad_norm"],
        tensorboard_log=os.path.join(ROOT, "runs"), device=device,
        seed=args.seed, verbose=1,
    )

    cb = MetricsCallback(curve_path)
    model.learn(total_timesteps=total_timesteps, callback=cb, progress_bar=False)
    model.save(out)
    venv.close()
    print(f"\n✓ Saved model -> {out}")
    print(f"✓ Saved curves -> {curve_path}")


if __name__ == "__main__":
    main()
