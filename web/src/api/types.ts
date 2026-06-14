export type SignalAspect = "red" | "yellow" | "green";
export type TrainStatus = "waiting" | "moving" | "arrived" | "delayed";

export interface TrainView {
  id: string;
  segment: string;
  destination: string;
  status: TrainStatus;
  speed: number;
  priority: number;
  type: string;
  delay: number;
}

export interface Decision {
  kind: string;
  text: string;
  reason: string;
}

export interface Conflict {
  segment: string;
  trains: string[];
  type: string;
  by?: string;
}

export interface Metrics {
  step: number;
  max_steps: number;
  step_energy_kwh: number;
  cum_energy_kwh: number;
  cum_energy_inr: number;
  cum_energy_co2_kg: number;
  collisions: number;
  reward_total: number;
  trains_arrived: number;
  trains_on_time: number;
  total_trains: number;
  total_delay: number;
}

export interface Frame {
  episode_id: string;
  task: string;
  mode: string;
  t: number;
  max_steps: number;
  done: boolean;
  trains: Record<string, TrainView>;
  signals: Record<string, SignalAspect>;
  occupied: Record<string, string | null>;
  conflicts: Conflict[];
  decisions: Decision[];
  metrics: Metrics;
  stream_done?: boolean;
}

export interface SegNode {
  id: string;
  x: number;
  y: number;
  is_junction: boolean;
  station: string | null;
  length: number;
}
export interface Topology {
  view: { w: number; h: number };
  nodes: Record<string, SegNode>;
  links: { from: string; to: string }[];
}

export interface Scenario {
  id: string;
  name: string;
  difficulty: string;
  description: string;
  num_trains: number;
  max_steps: number;
  modes: string[];
}

export interface StartResponse {
  episode_id: string;
  task: string;
  mode: string;
  seed: number;
  scenario: { name: string; difficulty: string; description: string };
  modes: string[];
  topology: Topology;
  frame: Frame;
}

export interface CompareResult {
  mode: string;
  series: { t: number; cum_kwh: number; collisions: number; arrived: number; total_delay: number }[];
  final: Metrics;
}
export interface CompareResponse {
  task: string;
  seed: number;
  results: Record<string, CompareResult>;
}

export interface CurvePoint {
  t: number;
  reward: number;
  len: number;
  energy_kwh: number;
  collisions: number;
  arrival_rate: number;
  total_delay: number;
}
