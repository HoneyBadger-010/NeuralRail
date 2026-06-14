import { useStore } from "../store/useStore";
import { fmtInt, Panel } from "./common";

const LABEL: Record<string, string> = {
  rl: "RL AGENT", greedy_priority: "GREEDY", no_control: "NO CONTROL", random: "RANDOM",
};

export default function ComparePanel() {
  const { compare, comparing, runCompare, task } = useStore();
  const results = compare && compare.task === task ? Object.entries(compare.results) : [];
  const maxE = Math.max(1, ...results.map(([, r]) => r.final.cum_energy_kwh));

  // Verdict: RL (or best-energy) vs no_control.
  let verdict: string | null = null;
  if (results.length) {
    const map = Object.fromEntries(results.map(([k, r]) => [k, r]));
    const base = map["no_control"]?.final;
    const star = map["rl"]?.final ?? results.slice().sort((a, b) => a[1].final.cum_energy_kwh - b[1].final.cum_energy_kwh)[0][1].final;
    if (base && star && base.cum_energy_kwh > 0) {
      const pct = Math.round((1 - star.cum_energy_kwh / base.cum_energy_kwh) * 100);
      verdict = `${pct >= 0 ? pct : 0}% less energy vs no-control · ${star.trains_arrived}/${star.total_trains} arrived`;
    }
  }

  return (
    <Panel title="Agent vs Baseline" right={
      <button onClick={runCompare} disabled={comparing}
        style={{ background: "var(--hv)", color: "#15110c", border: "none", borderRadius: "var(--r)",
                 padding: "4px 9px", fontSize: 10, fontWeight: 700, letterSpacing: "0.08em",
                 opacity: comparing ? 0.6 : 1 }}>
        {comparing ? "RUNNING…" : "RUN ▸"}
      </button>
    }>
      <div style={{ padding: "10px 12px", display: "flex", flexDirection: "column", gap: 10 }}>
        {results.length === 0 && (
          <div className="mono" style={{ fontSize: 11, color: "var(--ink-2)" }}>
            Run the same scenario+seed under each controller to completion.
          </div>
        )}
        {results.map(([key, r]) => {
          const e = r.final.cum_energy_kwh;
          const pct = (e / maxE) * 100;
          const isRL = (r.mode ?? key) === "rl";
          return (
            <div key={key}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 3 }}>
                <span className="label" style={{ color: isRL ? "var(--hv)" : "var(--ink-1)" }}>
                  {LABEL[r.mode ?? key] ?? key}
                </span>
                <span className="mono" style={{ fontSize: 11, color: "var(--ink-0)" }}>{fmtInt(e)} kWh</span>
              </div>
              <div style={{ height: 8, background: "var(--inset)", borderRadius: 2, overflow: "hidden" }}>
                <div style={{ width: `${pct}%`, height: "100%",
                              background: isRL ? "var(--hv)" : "linear-gradient(90deg,var(--en-good),var(--en-warn))" }} />
              </div>
              <div className="mono" style={{ fontSize: 9.5, color: "var(--ink-2)", marginTop: 2 }}>
                {r.final.trains_arrived}/{r.final.total_trains} arrived · delay {r.final.total_delay} · col {r.final.collisions}
              </div>
            </div>
          );
        })}
        {verdict && (
          <div className="mono" style={{ fontSize: 11, color: "var(--en-good)", borderTop: "1px solid var(--hair)", paddingTop: 8 }}>
            ▸ {verdict}
          </div>
        )}
      </div>
    </Panel>
  );
}
