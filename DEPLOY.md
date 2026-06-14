# Deployment

One Docker image serves the API, the WebSocket stream, and the built SPA from a single origin (no CORS in prod). CPU-only torch keeps it lean; agent inference is one forward pass.

## Local
```bash
docker build -t neuralrail-rl .
docker run -p 8000:8000 neuralrail-rl      # http://localhost:8000
```

## Hugging Face Spaces (recommended — free, WebSocket-capable, public URL)
The repo already builds the gym-env's predecessor on a Space, so this matches precedent.

1. Create a new **Space** → SDK: **Docker**.
2. Push this repo to the Space. Add a `README.md` at the Space root with frontmatter (or keep a separate Space README):
   ```yaml
   ---
   title: NeuralRail CTC
   emoji: 🚆
   colorFrom: gray
   colorTo: orange
   sdk: docker
   app_port: 8000
   pinned: false
   ---
   ```
3. The Space builds the `Dockerfile` and exposes port 8000.
4. **Commit trained checkpoints first** (`models/<task>/best.zip`) so the RL controller is live; otherwise the demo runs on baselines.

**Cold start:** free Spaces sleep on inactivity. The image is small and the model loads lazily; the UI shows a "Waking control room…" boot screen. For a judging window where you need always-on, deploy the same image to **Fly.io** or **Render** (both support WebSockets; paid tiers stay warm) and keep the Space as the public link.

## Notes
- Health probe: `GET /api/health` reports `trained_tasks` (which checkpoints are loaded).
- Sessions are in-memory (single container) — fine for a demo; documented limit.
- To rebuild the UI only: `cd web && npm run build` (FastAPI serves `web/dist` live, no restart needed).
