"""
FastAPI backend for the NeuralRail RL ops dashboard.

Serves the trained agent (or baselines) driving live episodes, streams frames
over WebSocket for smooth animation, exposes agent-vs-baseline comparison and
training curves, and serves the built SPA from one origin (no CORS in prod).

Run:  uvicorn server.app:app --reload --port 8000
"""

from __future__ import annotations

import asyncio
import json
import os

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from configs.tasks import TASK_NAMES
from sim.railway_simulator import RailwaySimulator

from .controllers import available_modes, has_checkpoint
from .schemas import CompareRequest, ModeRequest, OverrideRequest, StartRequest, StepRequest
from .sessions import SCENARIO_META, SessionManager, run_to_completion
from .topology import compute_topology

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
app = FastAPI(title="NeuralRail RL", version="1.0.0")
app.add_middleware(
    CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"],
)
manager = SessionManager()


# ----------------------------- REST ------------------------------------ #
@app.get("/api/health")
def health():
    return {
        "status": "ok",
        "tasks": TASK_NAMES,
        "trained_tasks": [t for t in TASK_NAMES if has_checkpoint(t)],
    }


@app.get("/api/scenarios")
def scenarios():
    out = []
    for task in TASK_NAMES:
        sim = RailwaySimulator(task)
        sim.reset(seed=0)
        out.append({
            "id": task, **SCENARIO_META[task],
            "num_trains": sim.num_trains, "max_steps": sim.max_steps,
            "modes": available_modes(task),
        })
    return out


@app.post("/api/episode/start")
def start(req: StartRequest):
    s = manager.create(req.task, req.mode, req.seed)
    return {
        "episode_id": s.id, "task": s.task, "mode": s.mode, "seed": s.seed,
        "scenario": SCENARIO_META.get(s.task, {}),
        "modes": available_modes(s.task),
        "topology": compute_topology(s.sim),
        "frame": s.frame(decisions=[]),
    }


@app.get("/api/episode/{episode_id}/state")
def state(episode_id: str):
    s = manager.get(episode_id)
    if s is None:
        return JSONResponse({"error": "unknown episode"}, status_code=404)
    return s.frame()


@app.post("/api/episode/{episode_id}/step")
def step(episode_id: str, req: StepRequest):
    s = manager.get(episode_id)
    if s is None:
        return JSONResponse({"error": "unknown episode"}, status_code=404)
    frames = [s.step_once() for _ in range(req.n) if not s.done]
    return {"frames": frames, "done": s.done}


@app.post("/api/episode/{episode_id}/mode")
def set_mode(episode_id: str, req: ModeRequest):
    s = manager.get(episode_id)
    if s is None:
        return JSONResponse({"error": "unknown episode"}, status_code=404)
    s.set_mode(req.mode)
    return {"mode": s.mode}


@app.post("/api/episode/{episode_id}/override")
def override(episode_id: str, req: OverrideRequest):
    s = manager.get(episode_id)
    if s is None:
        return JSONResponse({"error": "unknown episode"}, status_code=404)
    ok = s.set_override(req.kind, req.target, req.value)
    return {"ok": ok, "mode": s.mode}


@app.get("/api/episode/{episode_id}/metrics")
def metrics(episode_id: str):
    s = manager.get(episode_id)
    if s is None:
        return JSONResponse({"error": "unknown episode"}, status_code=404)
    return s.frame()["metrics"]


@app.post("/api/compare")
def compare(req: CompareRequest):
    if req.modes:
        modes = req.modes
    elif has_checkpoint(req.task):
        modes = ["rl", "greedy_priority", "no_control"]
    else:
        modes = ["greedy_priority", "no_control", "random"]
    results = {m: run_to_completion(req.task, m, req.seed) for m in modes}
    return {"task": req.task, "seed": req.seed, "results": results}


@app.get("/api/training-curves/{task}")
def training_curves(task: str):
    path = os.path.join(ROOT, "models", task, "training_curves.json")
    if os.path.exists(path):
        with open(path) as f:
            return json.load(f)
    return []


# --------------------------- WebSocket ---------------------------------- #
@app.websocket("/api/episode/{episode_id}/stream")
async def stream(ws: WebSocket, episode_id: str):
    await ws.accept()
    s = manager.get(episode_id)
    if s is None:
        await ws.send_json({"error": "unknown episode"})
        await ws.close()
        return

    st = {"paused": False, "speed": 1, "tick": 0.5, "step_req": 0}

    async def receiver():
        try:
            while True:
                msg = await ws.receive_json()
                cmd = msg.get("cmd")
                if cmd == "pause":
                    st["paused"] = True
                elif cmd == "play":
                    st["paused"] = False
                elif cmd == "speed":
                    st["speed"] = max(1, min(int(msg.get("value", 1)), 10))
                elif cmd == "tick":
                    st["tick"] = max(0.1, min(float(msg.get("value", 0.5)), 2.0))
                elif cmd == "step":
                    st["step_req"] += 1
                elif cmd == "mode":
                    s.set_mode(msg.get("value", "rl"))
                elif cmd == "override":
                    s.set_override(msg.get("kind"), msg.get("target"), msg.get("value"))
                elif cmd == "restart":
                    s.sim.reset(seed=s.seed)
                    s.done = False
                    s.cum_kwh = 0.0
                    s.reward_total = 0.0
                    s.prev_action = None
                    if hasattr(s.controller, "reset"):
                        s.controller.reset()
        except Exception:
            return

    recv_task = asyncio.create_task(receiver())
    done_sent = False
    try:
        await ws.send_json(s.frame(decisions=[]))
        while True:
            if s.done:
                if not done_sent:
                    await ws.send_json({**s.frame(decisions=[]), "stream_done": True})
                    done_sent = True
                await asyncio.sleep(0.15)
                continue
            done_sent = False

            if st["paused"]:
                if st["step_req"] > 0:
                    st["step_req"] -= 1
                    await ws.send_json(s.step_once())
                await asyncio.sleep(0.1)
                continue

            frame = None
            for _ in range(st["speed"]):
                frame = s.step_once()
                if s.done:
                    break
            if frame:
                await ws.send_json(frame)
            await asyncio.sleep(st["tick"])
    except WebSocketDisconnect:
        pass
    except Exception:
        pass
    finally:
        recv_task.cancel()


# --------------------------- Static SPA --------------------------------- #
_DIST = os.path.join(ROOT, "web", "dist")
if os.path.isdir(_DIST):
    app.mount("/", StaticFiles(directory=_DIST, html=True), name="spa")
else:
    @app.get("/")
    def root():
        return {"service": "NeuralRail RL", "ui": "build web/ to enable the SPA", "api": "/api/health"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server.app:app", host="0.0.0.0", port=8000, reload=False)
