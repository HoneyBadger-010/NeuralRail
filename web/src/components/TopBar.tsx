import { useStore } from "../store/useStore";

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
      display: "flex", alignItems: "center", gap: 18, padding: "0 16px", height: 52,
      background: "var(--bg-2)", borderBottom: "1px solid var(--hair-strong)", zIndex: 2,
    }}>
      {/* brand */}
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <div style={{ width: 10, height: 22, background: "var(--hv)", borderRadius: 1,
                      boxShadow: "0 0 12px var(--hv-line)" }} />
        <div>
          <div style={{ fontFamily: "var(--font-display)", fontWeight: 800, fontSize: 16,
                        letterSpacing: "0.04em", lineHeight: 1 }}>
            NEURAL<span style={{ color: "var(--hv)" }}>RAIL</span>
          </div>
          <div className="label" style={{ fontSize: 8.5, marginTop: 2 }}>Centralized Traffic Control</div>
        </div>
      </div>

      <div style={{ width: 1, height: 28, background: "var(--hair)" }} />

      {/* scenario */}
      <div>
        <div className="label">Section</div>
        <div style={{ fontFamily: "var(--font-cond)", fontWeight: 600, fontSize: 13, letterSpacing: "0.04em" }}>
          {scen?.name ?? "—"}
        </div>
      </div>

      {/* mission clock */}
      <div className="mono" style={{ marginLeft: "auto", fontSize: 13, color: "var(--ink-1)" }}>
        <span className="label" style={{ marginRight: 8 }}>Clock</span>
        <span style={{ color: "var(--ink-0)", fontSize: 18 }}>T{String(t).padStart(3, "0")}</span>
        <span style={{ color: "var(--ink-2)" }}> / {max}</span>
      </div>

      {/* controller chip */}
      <div style={{
        display: "flex", alignItems: "center", gap: 7, padding: "5px 11px", borderRadius: "var(--r)",
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
