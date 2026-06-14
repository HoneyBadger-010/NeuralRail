# Deployment (optional)

The submission is the GitHub repo + the local app; hosting is optional. One Docker
image serves the API, the WebSocket stream, and the built SPA from a single origin
(no CORS in prod). CPU-only torch keeps it lean; agent inference is one forward pass.

## Run locally (recommended)
```bash
docker build -t neuralrail-rl .
docker run -p 8000:8000 neuralrail-rl      # http://localhost:8000
```
Or without Docker:
```bash
pip install -e ".[serve]" "stable-baselines3>=2.3" "sb3-contrib>=2.3" torch
cd web && npm install && npm run build && cd ..
uvicorn server.app:app --port 8000
```

## Any Docker host (if you want a public URL later)
The image runs on any container host with WebSocket support (Render, Railway, Fly.io,
a VPS, …). The container listens on port **8000** (`EXPOSE 8000`) — map/route it on
your host. Commit a trained `models/<task>/best.zip` first so the **RL AGENT**
controller is live; otherwise the demo runs on baselines.

## Notes
- Health probe: `GET /api/health` reports `trained_tasks` (which checkpoints are loaded).
- Sessions are in-memory (single container) — fine for a demo; documented limit.
- Rebuild the UI only: `cd web && npm run build` (FastAPI serves `web/dist` live, no restart needed).
