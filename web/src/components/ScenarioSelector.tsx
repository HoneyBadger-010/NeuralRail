import { useStore } from "../store/useStore";
import { Panel } from "./common";

const DIFF_COLOR: Record<string, string> = {
  easy: "var(--sig-green)", medium: "var(--sig-amber)",
  "medium-hard": "var(--t-frt)", hard: "var(--sig-red)",
};

export default function ScenarioSelector() {
  const { scenarios, task, trainedTasks, start } = useStore();

  return (
    <Panel title="Scenarios">
      <div style={{ overflowY: "auto" }}>
        {scenarios.map((s) => {
          const active = s.id === task;
          const trained = trainedTasks.includes(s.id);
          return (
            <button key={s.id}
              onClick={() => start(s.id, trained ? "rl" : "greedy_priority", 0)}
              style={{
                display: "block", width: "100%", textAlign: "left",
                background: active ? "var(--hv-soft)" : "transparent",
                borderLeft: `2px solid ${active ? "var(--hv)" : "transparent"}`,
                borderBottom: "1px solid var(--hair)", borderTop: "none",
                borderRight: "none", padding: "10px 13px",
              }}>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ fontFamily: "var(--font-display)", fontWeight: 600, fontSize: 13.5,
                               color: active ? "var(--ink-0)" : "var(--ink-1)" }}>{s.name}</span>
                {trained && <span className="mono" style={{ fontSize: 9, color: "var(--hv)" }}>● RL</span>}
              </div>
              <div style={{ display: "flex", gap: 8, marginTop: 4 }}>
                <span className="label" style={{ color: DIFF_COLOR[s.difficulty] ?? "var(--ink-2)" }}>{s.difficulty}</span>
                <span className="mono" style={{ fontSize: 10, color: "var(--ink-2)" }}>{s.num_trains} trains</span>
              </div>
            </button>
          );
        })}
      </div>
    </Panel>
  );
}
