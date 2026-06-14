import { useStore } from "../store/useStore";

const MODE_LABEL: Record<string, string> = {
  rl: "RL AGENT", greedy_priority: "GREEDY", no_control: "NO CONTROL",
  random: "RANDOM", manual: "MANUAL",
};

const btn = (active = false): React.CSSProperties => ({
  background: active ? "var(--hv-soft)" : "var(--bg-2)",
  color: active ? "var(--hv)" : "var(--ink-1)",
  border: `1px solid ${active ? "var(--hv-line)" : "var(--hair)"}`,
  borderRadius: "var(--r)", padding: "6px 11px", fontSize: 11,
  textTransform: "uppercase", letterSpacing: "0.1em", fontWeight: 600,
});

export default function TransportBar() {
  const { paused, speed, mode, modes, frame, play, pause, setSpeed, stepOnce, restart, setMode } = useStore();
  const t = frame?.t ?? 0;
  const max = frame?.max_steps ?? 0;

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 16, padding: "10px 14px", flexWrap: "wrap" }}>
      <div style={{ display: "flex", gap: 6 }}>
        <button style={btn()} onClick={restart} title="Restart episode">↺ Restart</button>
        <button style={btn()} onClick={stepOnce} title="Step once">⏭ Step</button>
        <button style={btn(!paused)} onClick={() => (paused ? play() : pause())}>
          {paused ? "▶ Play" : "❚❚ Pause"}
        </button>
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <span className="label">Speed</span>
        {[1, 2, 5, 10].map((s) => (
          <button key={s} style={btn(speed === s)} onClick={() => setSpeed(s)}>{s}×</button>
        ))}
      </div>

      <div className="mono" style={{ color: "var(--ink-1)", fontSize: 12 }}>
        STEP <span style={{ color: "var(--ink-0)" }}>{String(t).padStart(2, "0")}</span>
        <span style={{ color: "var(--ink-2)" }}> / {max}</span>
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: 6, marginLeft: "auto" }}>
        <span className="label">Controller</span>
        <div style={{ display: "flex", border: "1px solid var(--hair)", borderRadius: "var(--r)", overflow: "hidden" }}>
          {modes.map((m) => {
            const active = mode === m;
            return (
              <button key={m} onClick={() => setMode(m)}
                style={{
                  background: active ? (m === "rl" ? "var(--hv)" : "var(--bg-2)") : "transparent",
                  color: active ? (m === "rl" ? "#15110c" : "var(--ink-0)") : "var(--ink-2)",
                  border: "none", borderRight: "1px solid var(--hair)",
                  padding: "6px 10px", fontSize: 10.5, fontWeight: 700,
                  textTransform: "uppercase", letterSpacing: "0.08em",
                }}>
                {MODE_LABEL[m] ?? m}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
