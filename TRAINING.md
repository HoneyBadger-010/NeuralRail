# Training the agent (GPU box — e.g. RTX 4050)

Training is the only step that needs a GPU. The whole pipeline is wiring-verified on CPU; on a 4050 it's fast (basic task in minutes, full curriculum in ~1.5–3 h). Inference/serving is CPU-only.

## 1. Setup
```bash
git clone <this repo> && cd neuralrail-rl
python -m venv .venv && source .venv/bin/activate
# CUDA build of torch (pick the index-url for your CUDA from pytorch.org):
pip install torch --index-url https://download.pytorch.org/whl/cu124
pip install -e ".[train]"
python -c "import torch; print('CUDA:', torch.cuda.is_available(), torch.cuda.get_device_name(0))"
```

## 2. Train
Device is auto-detected (CUDA if present). Outputs go to `models/<task>/best.zip` + `models/<task>/training_curves.json`; TensorBoard logs to `runs/`.
```bash
python -m training.train_ppo --task basic_control          # easy   (~minutes)
python -m training.train_ppo --task express_priority        # medium-hard
python -m training.train_ppo --task junction_management     # medium
python -m training.train_ppo --task rush_hour               # hard   (longest)
tensorboard --logdir runs                                   # watch reward↑ / energy↓
```
Hyperparameters per task live in `configs/train_hyperparams.yaml`; reward weights (incl. the calibrated `energy_ref_kwh`) in `configs/reward_weights.yaml`. Override any from the CLI, e.g. `--timesteps 500000 --w-energy 0.05`.

## 3. Prove the energy term matters (ablation)
```bash
python -m training.train_ppo --task rush_hour --w-energy 0 --out models/rush_hour/ablation.zip
```
Compare `ablation.zip` (delay-only) vs `best.zip` (energy-aware) in eval — the energy-aware agent should use less energy at equal/better throughput.

## 4. Evaluate (RL vs baselines)
```bash
python -m eval.run_eval --task all --seeds 50      # prints the comparison table + writes eval/results.csv
```
This is the headline pitch table: arrival % · collisions · delay · **energy kWh / ₹ / CO₂**, RL vs no-control / random / greedy.

## 5. Ship the trained agent
Commit the checkpoints so the live demo uses RL:
```bash
git add models/*/best.zip models/*/training_curves.json
git commit -m "Add trained PPO checkpoints + curves"
```
Redeploy (see `DEPLOY.md`). The server auto-detects `models/<task>/best.zip` and enables the **RL AGENT** controller; the UI's training-proof panel renders `training_curves.json`.

> Checkpoints are small (a few MB) — fine to commit. Everything else (`runs/`, venv) is gitignored.
