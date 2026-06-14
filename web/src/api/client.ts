import type { CompareResponse, CurvePoint, Scenario, StartResponse } from "./types";

// Dev: talk to the FastAPI server on :8000. Prod: same origin (SPA is served by it).
const API = import.meta.env.DEV ? "http://localhost:8000" : "";

export function wsURL(episodeId: string): string {
  if (import.meta.env.DEV) return `ws://localhost:8000/api/episode/${episodeId}/stream`;
  const proto = location.protocol === "https:" ? "wss" : "ws";
  return `${proto}://${location.host}/api/episode/${episodeId}/stream`;
}

async function jget<T>(path: string): Promise<T> {
  const r = await fetch(API + path);
  if (!r.ok) throw new Error(`${path} -> ${r.status}`);
  return r.json();
}
async function jpost<T>(path: string, body: unknown): Promise<T> {
  const r = await fetch(API + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!r.ok) throw new Error(`${path} -> ${r.status}`);
  return r.json();
}

export const api = {
  health: () => jget<{ status: string; tasks: string[]; trained_tasks: string[] }>("/api/health"),
  scenarios: () => jget<Scenario[]>("/api/scenarios"),
  start: (task: string, mode: string, seed: number) =>
    jpost<StartResponse>("/api/episode/start", { task, mode, seed }),
  compare: (task: string, seed: number) =>
    jpost<CompareResponse>("/api/compare", { task, seed }),
  curves: (task: string) => jget<CurvePoint[]>(`/api/training-curves/${task}`),
};
