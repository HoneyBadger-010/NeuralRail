# 🚆 NeuralRail — Energy-Aware Autonomous Railway Traffic Control

**An RL agent that runs a railway section to minimise delays, collisions _and_ energy — live, in a control-room dashboard.**

> **FAR AWAY 2026** · Theme: **Railways** (+ Agentic & Autonomous Systems)
> 🔗 Live demo: _<add your deployed URL>_ · 🎥 Video: _<add link>_

> ⚠️ **Decision-support & research simulation only.** NeuralRail is **not** connected to, and **not** for control of, real railway signalling or trains. All figures are model estimates on a stylised network.

---

## What it is

A central traffic controller for a railway network, driven by a **trained reinforcement-learning agent**. Each step the agent observes the whole section (trains, signals, block occupancy) and sets signals / holds trains to move everyone through **safely, on time, and with the least energy**. A custom ops-room UI streams the agent running live, explains every decision, and quantifies energy/₹/CO₂ saved versus baselines.

The novel bit: the agent is **energy-aware**. We fold a physics-based energy model (kinetic energy, regenerative braking, idle draw, restart cost) directly into the RL reward, so the policy learns that **needless stops and restarts of heavy trains waste energy** — and avoids them.

---

## Built on our prior work — and substantially extended

Per FAR AWAY's rules, this combines **two of our own previous projects** into one significantly new system. Here's the honest delta:

| | Prior project | What we reused | What's **new** for FAR AWAY |
|---|---|---|---|
| **NeuralRail** (GDG hackathon) | Web dashboard + a *rule-based* "optimizer" (it was **not** RL, despite the old README) | The physics-based **energy model** (`energy_calculator`, train specs) + the Indian-Railways impact framing | Energy folded into an RL **reward**; brand-new UI |
| **railway_controller_gym_env** | A genuine RL **environment** (OpenEnv/MCP) — but **no trained agent** (LLM-driven) | The simulator core (network, block-signalling, collisions) | Extracted to a pure in-process sim; wrapped as **Gymnasium**; **trained a real PPO agent** on it |

**New work in this repo:** a trainable Gymnasium env + action masking, the energy-aware reward, a **MaskablePPO** training pipeline, baselines + an evaluation harness, a FastAPI + WebSocket live server, and a bespoke React control-room UI. The old "RL agent" claim is now **literally true** — and the commit history shows it being built.

---

## What's real vs simulated

**Real**
- ✅ **Trained PPO policy** (MaskablePPO, stable-baselines3) — actual learning, with training curves.
- ✅ **Physics-based energy model** — KE = ½mv², braking loss with 30 % regenerative recovery, idle power, restart acceleration; real Indian Railways rolling-stock masses/speeds.
- ✅ **Multi-train simulator** with block-signalling and collision detection (4 scenarios, easy → rush-hour).
- ✅ **₹ / CO₂ conversions** at stated constants (₹5/kWh, 0.8 kg CO₂/kWh).

**Simulated / illustrative**
- The network is a **stylised** section, scenarios are synthetic, and energy savings are **model estimates** — not measurements from live track.

---

## How it works

```
            ┌──────────── observation (whole section) ────────────┐
            │  trains: pos · speed · priority · delay              │
            │  blocks: signal aspect · occupancy                  │
            └──────────────────────────┬──────────────────────────┘
                                       ▼
                         ┌───────────────────────────┐
                         │   PPO policy (the agent)   │   ← trained, action-masked
                         └─────────────┬─────────────┘
                                       ▼ MultiDiscrete action
                        set signals  ·  hold / release trains
                                       ▼
        ┌──────────────────────────────────────────────────────────┐
        │  Simulator advances 1 step (block-signalling, movement)    │
        │  Energy model scores the step (idle/brake/restart/cruise)  │
        └──────────────────────────────┬─────────────────────────────┘
                                       ▼
     reward = base(delays + collisions + priority) − w_energy · norm(energy_kWh)
```

The energy term is **weighted small** (a collision swings the base reward ~10× more), so safety always dominates — energy is the tie-breaker that makes the agent efficient.

## Architecture

```
Gymnasium env (env/) ──imported by both──┐
   ▲                                      ├─► training/  → MaskablePPO → models/<task>/best.zip
   │ sim/ (pure simulator)                └─► server/    → FastAPI + WebSocket
   │ physics/ (energy model + NeuralRail specs)              │
                                                              ▼
                                            web/ (React CTC dashboard, served as static)
```

---

## Scenarios

| Task | Trains | Difficulty | Notes |
|---|---|---|---|
| `basic_control` | 2 | easy | single shared crossing |
| `junction_management` | 4 | medium | two junctions, crossing routes |
| `express_priority` | 5 | medium-hard | cascading conflicts, tight schedules |
| `rush_hour` | 6 | hard | congestion + delays + weather |

---

## Quickstart (local)

**Backend + UI (one origin):**
```bash
python -m venv .venv && source .venv/bin/activate
pip install -e ".[serve]" "stable-baselines3>=2.3" "sb3-contrib>=2.3" torch
cd web && npm install && npm run build && cd ..
uvicorn server.app:app --port 8000      # open http://localhost:8000
```

**UI dev server (hot reload, talks to :8000):**
```bash
cd web && npm run dev                    # http://localhost:5173
```

**Docker (production image — API + UI):**
```bash
docker build -t neuralrail-rl . && docker run -p 8000:8000 neuralrail-rl
```

The demo runs out of the box on **baselines**; once a trained checkpoint exists in `models/<task>/best.zip`, the **RL AGENT** controller lights up automatically.

---

## Training (GPU) & evaluation

Training is the only GPU step — see **[TRAINING.md](TRAINING.md)**. In short:
```bash
pip install -e ".[train]" torch          # CUDA torch on the GPU box
python -m training.train_ppo --task basic_control     # ~minutes on an RTX 4050
python -m eval.run_eval --task all                    # RL-vs-baseline table + energy KPIs
```

Baseline reference (no RL yet) already shows the energy signal is meaningful — e.g. random control burns **2–5× more energy** than orderly control, and on `rush_hour` only ~64 % of trains arrive in time under no-control, leaving clear headroom for the agent.

---

## Tech stack
Python · Gymnasium · **stable-baselines3 / sb3-contrib (MaskablePPO)** · PyTorch · FastAPI · WebSockets · React + TypeScript + Vite · Docker.

## Repo structure
```
sim/        pure railway simulator (extracted, behaviour-preserving)
physics/    energy model + vendored NeuralRail specs/constants
env/        Gymnasium wrapper · obs encoder · action codec (masking)
training/   MaskablePPO pipeline + curves
agents/     baselines + trained-policy controller
eval/       evaluation harness (energy/throughput/delay KPIs)
server/     FastAPI + WebSocket live server
web/        React CTC control-room UI
configs/    tasks + hyperparameters + reward weights
```

## Roadmap
Tier 1 (now): live, deployed simulation. → Tier 2: usable decision-support tool (what-if overrides, exportable reports). → Tier 3 (out of scope; years of certification): real signalling integration.

## License
MIT.

## Acknowledgements
Indian Railways operational framing · RDSO energy data · OpenEnv. Built for FAR AWAY 2026.
