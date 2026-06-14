# FAR AWAY 2026 — Submission Kit

**Project:** NeuralRail — Energy-Aware Autonomous Railway Traffic Control
**Theme:** Railways (primary) · Agentic & Autonomous Systems
**Mandatory:** GitHub repo link + (presentation OR video). Both outlined below.

---

## The narrative spine (say this throughout)
1. **Energy-aware RL is the innovation** — not just an autonomous dispatcher, but one that learns to cut energy waste from needless stops/restarts.
2. **Significant extension of prior work** — we fused two of our own repos (NeuralRail's energy model + a railway RL *environment* that had no trained agent) into one new system with a **real trained PPO agent**. The commit history shows the build.
3. **Honest** — deployed simulation/decision-support; not real-train control. Numbers are model estimates.

---

## Deck outline (≤15 slides — banner's suggested structure)
1. **Title** — NeuralRail · energy-aware autonomous railway control · live URL · "simulation only" line.
2. **Problem** — dense sections: cascading delays, collision risk, and **energy wasted** braking/restarting heavy trains.
3. **Solution** — a PPO agent that controls signals/holds to minimise delay + collisions **+ energy**, in a live control room.
4. **Key features** — autonomous RL control · energy-aware reward · agent-vs-baseline proof · manual what-if overrides · live deployed.
5. **How it works** — observation → policy → action → sim+energy → reward (the diagram from the README).
6. **The energy idea** — physics model folded into the reward; weighted so safety always wins.
7. **Tech stack** — MaskablePPO/SB3 + PyTorch · FastAPI + WebSocket · React/TS · Docker.
8. **Architecture** — one `env/` shared by training and serving; the system diagram.
9. **The RL is real** — training curves (reward ↑, energy ↓, collisions → 0). Screenshot the training-proof panel.
10. **Demo** — screenshots/GIF: live map, decision feed, KPI rail.
11. **Results** — eval table: RL vs no-control/greedy/random — energy kWh/₹/CO₂ at equal/better throughput.
12. **Eligibility & honesty** — the prior-work delta table + what's real vs simulated + disclaimer.
13. **Real-world impact** — energy/₹/CO₂ framing at section scale; section-controller decision support.
14. **Scalability & roadmap** — more trains/segments; tier-2 decision-support; tier-3 (years, certification).
15. **Close** — repo + live URL + team.

## Video script (2–5 min)
- **0:00 Hook** — "Every time a loaded freight train stops and restarts, it burns hundreds of kWh. Multiply by a busy section…"
- **0:30** — Open the live tool, pick **Rush Hour**. Point out the control-room view.
- **0:50** — Let the **RL agent** run; read one decision from the feed ("HOLD freight — yield to express"). Watch trains glide, signals switch.
- **1:40** — Hit **Compare**: same scenario+seed, RL vs baselines — "RL: fewer collisions, X % less energy at equal throughput."
- **2:30** — Switch to **Manual**: drop a what-if override, show it's a decision-support tool, not just a demo.
- **3:00** — **Training-proof** panel: "this is a real trained PPO agent — reward up, energy down."
- **3:30** — Honesty line + disclaimer; "built by significantly extending two of our prior projects."
- **3:50** — Repo + live URL.

## Pre-submission checklist
- [ ] Trained checkpoints committed (`models/*/best.zip`) so the demo shows the **RL** controller.
- [ ] Live URL deployed (HF Spaces) and reachable; cold-start handled.
- [ ] `eval/run_eval.py` table screenshot for slide 11.
- [ ] Training-proof panel populated (curves) for slide 9.
- [ ] README live URL + video link filled in.
- [ ] Repo public; no secrets (verified — only `.env.example`).
- [ ] Commit history is incremental and descriptive.
