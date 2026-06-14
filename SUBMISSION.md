# Pitch & Demo Kit

A ready-to-use outline for a slide deck and a 2–5 minute demo video.

## Narrative spine
1. **Energy-aware RL is the innovation** — not just an autonomous dispatcher, but one that *learns* to cut energy waste from needless stops/restarts while keeping trains safe and on time.
2. **It's a real trained agent** — MaskablePPO, 204,800 steps, with training curves and an agent-vs-baseline comparison to prove it.
3. **Honest scope** — a deployed simulation / decision-support tool, not real-train control; numbers are model estimates.

## Deck outline (≤15 slides)
1. **Title** — NeuralRail · energy-aware autonomous railway control · live URL · "simulation only" line.
2. **Problem** — dense networks: cascading delays, collision risk, and **energy wasted** braking/restarting heavy trains.
3. **Solution** — a PPO agent that controls signals/holds to minimise delay + collisions **+ energy**, in a live control room.
4. **Key features** — autonomous RL control · energy-aware reward · agent-vs-baseline proof · manual what-if · live deploy.
5. **How it works** — observation → policy → action → sim + energy → reward (the README flowchart).
6. **The energy idea** — physics model folded into the reward; weighted so safety always wins.
7. **Tech stack** — MaskablePPO/SB3 + PyTorch · FastAPI + WebSocket · React/TS · Docker.
8. **Architecture** — one `env/` shared by training and serving; the system diagram.
9. **The RL is real** — training curves (reward ↑, energy ↓, collisions → 0).
10. **Demo** — live map, decision feed, KPI rail (screenshots).
11. **Results** — eval table: RL vs no-control/greedy/random — energy at equal/better throughput.
12. **Honesty** — what's real vs simulated; the disclaimer.
13. **Impact** — energy/CO₂ savings at scale; controller decision-support.
14. **Scalability & roadmap** — more trains/segments; decision-support tool; (long-term) real integration.
15. **Close** — repo + live URL + team.

## Video script (2–5 min)
- **0:00 Hook** — "Every time a loaded train stops and restarts, it burns hundreds of kWh. Multiply across a busy section…"
- **0:30** — Open the live tool, pick a scenario; point out the control-room view.
- **0:50** — Let the **RL agent** run; read one decision from the feed; watch trains glide, signals switch.
- **1:40** — Hit **Compare**: same scenario+seed, RL vs baselines — "fewer/equal collisions, less energy at equal throughput."
- **2:30** — Switch to **Manual**: a what-if override → it's a decision-support tool, not just a demo.
- **3:00** — **Training-proof** panel: "this is a real trained PPO agent — reward up, energy down."
- **3:30** — Honesty line + disclaimer; "significant extension of two earlier projects."
- **3:50** — Repo + live URL.

## Pre-submission checklist
- [ ] Trained checkpoint committed (`models/<task>/best.zip`) so the demo shows the **RL** controller.
- [ ] (Optional) Live URL deployed and reachable.
- [ ] `eval/run_eval.py` table screenshot for the results slide.
- [ ] Training-proof panel populated (curves) for the "RL is real" slide.
- [ ] README live URL + video link filled in.
- [ ] Repo public; no secrets (only `.env.example`).
- [ ] Commit history is incremental and descriptive.
