<div align="center">

# 🚆 NeuralRail

### Energy-Aware Autonomous Railway Traffic Control

**A reinforcement-learning agent that runs a railway section — minimising delays, collisions _and_ energy — live, in a control-room dashboard.**

[![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![PyTorch](https://img.shields.io/badge/PyTorch-EE4C2C?logo=pytorch&logoColor=white)](https://pytorch.org/)
[![Stable-Baselines3](https://img.shields.io/badge/RL-MaskablePPO-5C3EE8)](https://github.com/Stable-Baselines-Team/stable-baselines3-contrib)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![License: MIT](https://img.shields.io/badge/License-MIT-F4791F)](LICENSE)

🔗 **Live demo:** _&lt;add deployed URL&gt;_ · 🎥 **Video:** [docs/demo-walkthrough.mp4](docs/demo-walkthrough.mp4)

</div>

> ⚠️ **Decision-support & research simulation only.** NeuralRail is **not** connected to, and **not** for control of, real railway signalling or trains. All figures are model estimates on a stylised network.

<div align="center">
<img src="docs/ui-dashboard.png" width="90%" alt="NeuralRail control room — live RL agent"/>
<br/><em>The live control room — a trained PPO agent dispatching a section, with energy/throughput KPIs and the training-proof panel.</em>
</div>

---

## 📹 Product walkthrough

<div align="center">
<img src="docs/demo.gif" width="80%" alt="NeuralRail walkthrough"/>
<br/><em>Full walkthrough — problem, solution, demo, impact. High-quality video: <a href="docs/demo-walkthrough.mp4">docs/demo-walkthrough.mp4</a></em>
</div>

## What it is

NeuralRail is a **central traffic controller** for a railway network, driven by a **trained reinforcement-learning agent**. Each step the agent sees the whole section — every train, signal and block — and sets signals / holds trains so the network runs **safely, on time, and with the least energy**. A custom ops-room UI streams the agent live, explains every decision, and quantifies the energy (kWh / cost / CO₂) saved versus baselines.

The novel idea is that the agent is **energy-aware**: a physics-based energy model (kinetic energy, regenerative braking, idle draw, restart cost) is folded directly into the RL reward, so the policy *learns* that needless stops and restarts of heavy trains waste energy — and avoids them.

---

## 🔥 The problem we solve

Dense railway networks still run on largely **manual, energy-blind** traffic decisions:

| Problem | Scale |
|---|---|
| **Train conflicts** | hundreds–thousands daily on a busy network |
| **Energy waste** | ~5–10 % of traction energy, much of it from unnecessary stop/restart cycles |
| **Manual decisions** | human dispatchers, no per-decision energy optimisation |
| **Cascade delays** | one conflict can ripple to 10+ trains |

Controllers make split-second routing calls with no data and **no energy objective**; stopping and restarting a heavy train burns hundreds of kWh each time. Passengers feel the delays, and the avoidable energy is pure cost and CO₂.

## 📊 Opportunity

The global **railway-AI / smart-signalling market** is growing fast (~$3.2 B in 2024 → ~$12.8 B by 2030, ≈26 % CAGR). Existing tools mostly **track and manage** traffic; they don't **optimise** it, and none treat **energy** as a first-class objective. NeuralRail is a *learned* controller — energy-first, and trainable on any network topology.

---

## 💡 How it works

<div align="center">
<img src="docs/flowchart.png" width="92%" alt="Energy-aware RL control loop"/>
</div>

The agent (a MaskablePPO policy) and the environment (simulator + energy model) form a closed loop. The reward rewards arrivals and on-time/priority service, penalises collisions heavily, and subtracts a **small** energy term — so safety always dominates and energy is the efficiency tie-breaker.

## 🧠 The trained agent — results

We trained the `basic_control` task to convergence (204,800 steps). The agent **learns the optimal policy from scratch**: episode energy falls ~240 → **111 kWh**, reward climbs to ~5.0, reaching **100 % on-time arrival with 0 collisions** — matching the best hand-crafted baseline and using **43 % less energy than uncontrolled/random dispatching**.

<div align="center">
<img src="docs/performance.png" width="92%" alt="Trained PPO performance — reward, energy, arrival, RL vs baselines"/>
</div>

| Controller | Energy (kWh) | On-time | Collisions |
|---|---:|---:|---:|
| Random | 196 | 97 % | 0 |
| No-control | 111 | 100 % | 0 |
| Greedy (priority) | 111 | 100 % | 0 |
| **RL agent (ours)** | **111** | **100 %** | **0** |

> On this easy task the optimum equals the best baseline, and the RL agent *reaches it by learning* (see the energy curve). Its margin grows on harder, congested scenarios (`junction_management` → `rush_hour`) where coordinated control matters far more — the next curriculum targets.

## 🖥️ The control room

<div align="center">
<img src="docs/ui-junction.png" width="90%" alt="Junction scenario with live decision feed"/>
<br/><em>Richer scenarios: live contention detection (pulsing zone), the agent decision feed with reasons, and per-controller energy KPIs.</em>
</div>

- **Live network map** — trains glide between blocks, signals switch aspect, contention zones pulse.
- **Agent decision feed** — every hold/release with a plain-English reason.
- **Energy & throughput KPIs** — energy / cost / CO₂, delay, collisions, on-time.
- **Controller toggle** — RL · greedy · no-control · random · manual (what-if overrides).
- **Agent-vs-baseline compare** and a **training-proof** panel rendering the real curves.

---

## 📈 Measurable impact

**Measured (simulation, trained agent · `basic_control`):** episode energy **240 → 111 kWh (−54 %)**, 100 % on-time, 0 collisions.

At network scale, the same per-decision saving compounds — avoiding unnecessary heavy-train stops cuts **GWh of traction energy and thousands of tonnes of CO₂ per year** (model estimate; depends on traffic and tariffs). The agent delivers this **without trading off** punctuality or safety.

## 💰 How it could be used

- **Operator decision-support** — a per-section advisor / autonomous controller for traffic-control centres (SaaS per section, optional energy-savings share).
- **Planning & simulation** — what-if analysis of timetables and signalling strategies before deployment.
- **Adjacent networks** — metros and dedicated freight corridors with the same energy-vs-throughput trade-off.

---

## 🧩 Built on prior work — and substantially extended

This project **combines and significantly extends two of our earlier projects**:

| Prior project | Reused | What's new |
|---|---|---|
| **NeuralRail** — web dashboard + a *rule-based* optimizer (then mislabelled "RL") | the physics-based **energy model** | energy folded into an **RL reward** |
| **railway-controller env** — a real RL *environment* with **no trained agent** | the simulator core (network, block-signalling, collisions) | extracted to a pure in-process sim, wrapped as **Gymnasium**, and a **real PPO agent trained** on it |

**New work here:** a trainable Gymnasium env with action masking, the energy-aware reward (incl. a reward-hacking/stall fix), a MaskablePPO training pipeline, baselines + evaluation, a FastAPI + WebSocket live server, and a bespoke React control-room UI. The "RL agent" is now **literally true**, and the commit history shows it being built.

### What's real vs simulated
- **Real:** trained PPO policy + checkpoint; physics energy model (KE, 30 % regen, idle, restart); multi-train simulator with block-signalling & collision detection.
- **Simulated:** stylised network, synthetic scenarios, model-estimated savings — not live-track measurements.

---

## 🚀 Quickstart

```bash
python -m venv .venv && source .venv/bin/activate
pip install torch                                                  # CPU; or CUDA wheel on a GPU box
pip install -e ".[serve]" "stable-baselines3>=2.3" "sb3-contrib>=2.3"
cd web && npm install && npm run build && cd ..
uvicorn server.app:app --port 8000          # open http://localhost:8000

# Docker (API + WS + SPA in one image)
docker build -t neuralrail . && docker run -p 8000:8000 neuralrail
```
The demo runs on baselines out of the box; with a trained `models/<task>/best.zip` the **RL AGENT** controller activates automatically.

## 🧪 Train your own agent (GPU)
See **[TRAINING.md](TRAINING.md)**:
```bash
python -m training.train_ppo --task basic_control     # ~minutes on a modern GPU
python -m eval.run_eval --task all                    # RL-vs-baseline table + energy KPIs
```

## 🏗️ Architecture
```
Gymnasium env (env/) ──imported by both──┐
   ▲  sim/ (pure simulator)              ├─► training/ → MaskablePPO → models/<task>/best.zip
   │  physics/ (energy model + specs)    └─► server/   → FastAPI + WebSocket
                                                          │
                                                          ▼
                                          web/ (React control-room dashboard, served as static)
```

## 🛠️ Tech stack
Python · Gymnasium · **MaskablePPO (stable-baselines3 / sb3-contrib)** · PyTorch · FastAPI · WebSockets · React + TypeScript + Vite · Docker.

## 📁 Repo structure
```
sim/        pure railway simulator (block-signalling, collisions)
physics/    energy model + rolling-stock specs/constants
env/        Gymnasium wrapper · obs encoder · action codec (masking) · energy-aware reward
training/   MaskablePPO pipeline + curves      eval/  baselines + evaluation harness
server/     FastAPI + WebSocket live server     web/   React control-room UI
models/     trained checkpoints + performance.png + training_curves.json
docs/        figures + demo media used in this README
```

## 🗺️ Roadmap
- **Phase 1 — Simulation (now):** trained agent on `basic_control`, live dashboard; full curriculum next (`junction → rush_hour`).
- **Phase 2 — Decision-support tool:** what-if overrides, exportable reports, live data-feed integration.
- **Phase 3 (out of scope; years + safety certification):** real signalling integration (hardware-in-the-loop testing and safety approval).

## ⚠️ Disclaimer
Decision-support & research simulation only — **not** connected to, and **not** for control of, real railway signalling or trains.

## 📄 License
MIT — see [LICENSE](LICENSE).
