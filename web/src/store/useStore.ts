import { create } from "zustand";
import { api, wsURL } from "../api/client";
import type {
  CompareResponse, CurvePoint, Decision, Frame, Scenario, Topology,
} from "../api/types";

let ws: WebSocket | null = null;
let decisionSeq = 0;

export interface LoggedDecision extends Decision {
  uid: number;
  t: number;
}

interface State {
  scenarios: Scenario[];
  trainedTasks: string[];
  task: string;
  seed: number;
  episodeId: string | null;
  topology: Topology | null;
  modes: string[];
  mode: string;
  frame: Frame | null;
  decisions: LoggedDecision[];
  connected: boolean;
  paused: boolean;
  speed: number;
  booted: boolean;

  compare: CompareResponse | null;
  comparing: boolean;
  curves: CurvePoint[];

  boot: () => Promise<void>;
  start: (task?: string, mode?: string, seed?: number) => Promise<void>;
  send: (msg: Record<string, unknown>) => void;
  play: () => void;
  pause: () => void;
  setSpeed: (s: number) => void;
  stepOnce: () => void;
  setMode: (m: string) => void;
  restart: () => void;
  override: (kind: string, target: string, value?: string) => void;
  runCompare: () => Promise<void>;
  loadCurves: (task: string) => Promise<void>;
}

function connect(episodeId: string, set: (p: Partial<State>) => void, get: () => State) {
  if (ws) { ws.close(); ws = null; }
  const sock = new WebSocket(wsURL(episodeId));
  ws = sock;
  sock.onopen = () => {
    set({ connected: true });
    sock.send(JSON.stringify({ cmd: "speed", value: get().speed }));
  };
  sock.onclose = () => { if (ws === sock) set({ connected: false }); };
  sock.onmessage = (ev) => {
    const frame = JSON.parse(ev.data) as Frame & { error?: string };
    if (frame.error) return;
    const prev = get();
    const newDecs = (frame.decisions || []).map((d) => ({ ...d, uid: ++decisionSeq, t: frame.t }));
    set({
      frame,
      mode: frame.mode ?? prev.mode,
      decisions: newDecs.length ? [...newDecs.reverse(), ...prev.decisions].slice(0, 80) : prev.decisions,
    });
  };
}

export const useStore = create<State>((set, get) => ({
  scenarios: [],
  trainedTasks: [],
  task: "basic_control",
  seed: 0,
  episodeId: null,
  topology: null,
  modes: [],
  mode: "rl",
  frame: null,
  decisions: [],
  connected: false,
  paused: false,
  speed: 2,
  booted: false,
  compare: null,
  comparing: false,
  curves: [],

  boot: async () => {
    const [health, scenarios] = await Promise.all([api.health(), api.scenarios()]);
    set({ trainedTasks: health.trained_tasks, scenarios, booted: true });
    const first = scenarios[0]?.id ?? "basic_control";
    const preferred = health.trained_tasks.includes(first) ? "rl" : "greedy_priority";
    await get().start(first, preferred, 0);
  },

  start: async (task, mode, seed) => {
    const t = task ?? get().task;
    const m = mode ?? get().mode;
    const s = seed ?? get().seed;
    const res = await api.start(t, m, s);
    set({
      task: t, seed: s, episodeId: res.episode_id, topology: res.topology,
      modes: res.modes, mode: res.mode, frame: res.frame, decisions: [], paused: false,
    });
    connect(res.episode_id, set, get);
    get().loadCurves(t);
  },

  send: (msg) => { if (ws && ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(msg)); },
  play: () => { set({ paused: false }); get().send({ cmd: "play" }); },
  pause: () => { set({ paused: true }); get().send({ cmd: "pause" }); },
  setSpeed: (sp) => { set({ speed: sp }); get().send({ cmd: "speed", value: sp }); },
  stepOnce: () => { set({ paused: true }); get().send({ cmd: "step" }); },
  setMode: (m) => { set({ mode: m }); get().send({ cmd: "mode", value: m }); },
  restart: () => { set({ decisions: [], paused: false }); get().send({ cmd: "restart" }); get().send({ cmd: "play" }); },
  override: (kind, target, value) => get().send({ cmd: "override", kind, target, value }),

  runCompare: async () => {
    set({ comparing: true });
    try {
      const res = await api.compare(get().task, get().seed);
      set({ compare: res });
    } finally {
      set({ comparing: false });
    }
  },
  loadCurves: async (task) => {
    try { set({ curves: await api.curves(task) }); } catch { set({ curves: [] }); }
  },
}));
