import { useStore } from "../store/useStore";
import Icon from "./Icon";

const MODE_LABEL: Record<string, string> = {
  rl: "RL AGENT", greedy_priority: "GREEDY", no_control: "NO CONTROL",
  random: "RANDOM", manual: "MANUAL",
};

export default function TopBar() {
  const { frame, mode, connected, scenarios, task } = useStore();
  const scen = scenarios.find((s) => s.id === task);
  const t = frame?.t ?? 0;
  const max = frame?.max_steps ?? 0;
  const isRL = mode === "rl";

  return (
    <header style={{
      display: "flex", alignItems: "center", gap: 18, padding: "0 16px", height: 54,
      background: "var(--bg-2)", borderBottom: "1px solid var(--hair-strong)", zIndex: 2,
    }}>
      {/* brand mark */}
      <div style={{ display: "flex", alignItems: "center", gap: 11 }}>
        <div style={{
          width: 32, height: 32, borderRadius: 6, background: "var(--hv)",
          display: "grid", placeItems: "center", boxShadow: "0 0 16px var(--hv-line)",
        }}>
          <Icon name="train" size={20} color="#15110c" />
        </div>
        <div>
          <div style={{ fontFamily: "var(--font-display)", fontWeight: 800, fontSize: 16,
                        letterSpacing: "0.04em", lineHeight: 1 }}>
            NEURAL<span style={{ color: "var(--hv)" }}>RAIL</span>
          </div>
          <div className="label" style={{ fontSize: 8.5, marginTop: 3 }}>Centralized Traffic Control</div>
        </div>
      </div>

      <div style={{ width: 1, height: 30, background: "var(--hair)" }} />

      {/* section */}
      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <Icon name="route" size={17} color="var(--ink-2)" />
        <div>
          <div className="label">Section</div>
          <div style={{ fontFamily: "var(--font-cond)", fontWeight: 600, fontSize: 13, letterSpacing: "0.04em" }}>
            {scen?.name ?? "—"}
          </div>
        </div>
      </div>

      {/* mission clock */}
      <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8 }}>
        <Icon name="clock" size={16} color="var(--ink-2)" />
        <span className="label">Clock</span>
        <span className="mono" style={{ fontSize: 18, color: "var(--ink-0)" }}>T{String(t).padStart(3, "0")}</span>
        <span className="mono" style={{ fontSize: 13, color: "var(--ink-2)" }}>/ {max}</span>
      </div>

      {/* controller chip */}
      <div style={{
        display: "flex", alignItems: "center", gap: 7, padding: "6px 12px", borderRadius: "var(--r)",
        background: isRL ? "var(--hv)" : "var(--bg-1)", border: `1px solid ${isRL ? "var(--hv)" : "var(--hair-strong)"}`,
      }}>
        <span style={{ width: 7, height: 7, borderRadius: 8,
                       background: connected ? (isRL ? "#15110c" : "var(--sig-green)") : "var(--sig-red)",
                       animation: connected ? "none" : "blink 1s steps(1) infinite" }} />
        <span style={{ fontFamily: "var(--font-cond)", fontWeight: 700, fontSize: 11, letterSpacing: "0.1em",
                       color: isRL ? "#15110c" : "var(--ink-0)" }}>
          CONTROL · {MODE_LABEL[mode] ?? mode}
        </span>
      </div>
    </header>
  );
}
