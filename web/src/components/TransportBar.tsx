import { useStore } from "../store/useStore";
import Icon, { type IconName } from "./Icon";

const MODE_LABEL: Record<string, string> = {
  rl: "RL AGENT", greedy_priority: "GREEDY", no_control: "NO CONTROL",
  random: "RANDOM", manual: "MANUAL",
};

function CtrlBtn({ icon, label, active, onClick, title }: {
  icon: IconName; label: string; active?: boolean; onClick: () => void; title?: string;
}) {
  return (
    <button onClick={onClick} title={title}
      style={{
        display: "inline-flex", alignItems: "center", gap: 7,
        background: active ? "var(--hv-soft)" : "var(--bg-1)",
        color: active ? "var(--hv)" : "var(--ink-1)",
        border: `1px solid ${active ? "var(--hv-line)" : "var(--hair)"}`,
        borderRadius: "var(--r)", padding: "7px 12px", height: 32,
        fontSize: 11, textTransform: "uppercase", letterSpacing: "0.09em", fontWeight: 600,
      }}>
      <Icon name={icon} size={15} />{label}
    </button>
  );
}

export default function TransportBar() {
  const { paused, speed, mode, modes, frame, play, pause, setSpeed, stepOnce, restart, setMode } = useStore();
  const t = frame?.t ?? 0;
  const max = frame?.max_steps ?? 0;

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 18, padding: "10px 14px", flexWrap: "wrap" }}>
      <div style={{ display: "flex", gap: 7 }}>
        <CtrlBtn icon="replay" label="Restart" onClick={restart} title="Restart episode" />
        <CtrlBtn icon="skip" label="Step" onClick={stepOnce} title="Advance one step" />
        <CtrlBtn icon={paused ? "play" : "pause"} label={paused ? "Play" : "Pause"} active={!paused}
                 onClick={() => (paused ? play() : pause())} />
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: 7 }}>
        <Icon name="speed" size={15} color="var(--ink-2)" />
        <span className="label">Speed</span>
        <div style={{ display: "flex", border: "1px solid var(--hair)", borderRadius: "var(--r)", overflow: "hidden" }}>
          {[1, 2, 5, 10].map((s) => (
            <button key={s} onClick={() => setSpeed(s)}
              style={{
                background: speed === s ? "var(--hv-soft)" : "transparent",
                color: speed === s ? "var(--hv)" : "var(--ink-2)",
                border: "none", borderRight: "1px solid var(--hair)",
                padding: "7px 11px", height: 32, fontSize: 11, fontWeight: 700, fontFamily: "var(--font-mono)",
              }}>{s}×</button>
          ))}
        </div>
      </div>

      <div className="mono" style={{ color: "var(--ink-1)", fontSize: 12, display: "flex", alignItems: "center", gap: 6 }}>
        <Icon name="clock" size={14} color="var(--ink-2)" />
        STEP <span style={{ color: "var(--ink-0)" }}>{String(t).padStart(2, "0")}</span>
        <span style={{ color: "var(--ink-2)" }}>/ {max}</span>
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: 8, marginLeft: "auto" }}>
        <Icon name="tune" size={15} color="var(--ink-2)" />
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
                  padding: "8px 11px", height: 32, fontSize: 10.5, fontWeight: 700,
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
