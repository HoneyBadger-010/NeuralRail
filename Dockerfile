# ---------- stage 1: build the SPA ----------
FROM node:22-slim AS web
WORKDIR /web
COPY web/package*.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

# ---------- stage 2: runtime (FastAPI serves API + WS + the built SPA) ----------
FROM python:3.12-slim AS runtime
WORKDIR /app
ENV PYTHONUNBUFFERED=1 PIP_NO_CACHE_DIR=1

# CPU-only torch keeps the image lean (inference is a single forward pass).
RUN pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

COPY pyproject.toml ./
COPY configs ./configs
COPY sim ./sim
COPY physics ./physics
COPY env ./env
COPY agents ./agents
COPY training ./training
COPY eval ./eval
COPY server ./server
COPY models ./models
RUN pip install --no-cache-dir -e ".[serve]" "stable-baselines3>=2.3" "sb3-contrib>=2.3"

# built SPA from stage 1 (served as static by FastAPI)
COPY --from=web /web/dist /app/web/dist

EXPOSE 8000
CMD ["uvicorn", "server.app:app", "--host", "0.0.0.0", "--port", "8000", "--timeout-graceful-shutdown", "3"]
