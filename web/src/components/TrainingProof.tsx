import type { CurvePoint } from "../api/types";
import { fmtInt, Panel } from "./common";

function poly(vals: number[], w: number, h: number, pad = 4): string {
  if (vals.length < 2) return "";
  const lo = Math.min(...vals), hi = Math.max(...vals);
  const span = hi - lo || 1;
  return vals.map((v, i) => {
    const x = pad + (i / (vals.length - 1)) * (w - 2 * pad);
    const y = h - pad - ((v - lo) / span) * (h - 2 * pad);
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  }).join(" ");
}

export default function TrainingProof({ curves }: { curves: CurvePoint[] }) {
  const W = 520, H = 96;
  // Downsample to ~120 points for a clean line.
  const step = Math.max(1, Math.floor(curves.length / 120));
  const pts = curves.filter((_, i) => i % step === 0);
  const has = pts.length >= 2;
  const last = curves[curves.length - 1];

  return (
    <Panel title="Training Proof — Real PPO" right={
      has ? <span className="mono" style={{ fontSize: 10, color: "var(--ink-2)" }}>{fmtInt(last.t)} steps</span> : undefined
    }>
      <div style={{ padding: "8px 12px 10px" }}>
        {!has ? (
          <div className="mono" style={{ fontSize: 11, color: "var(--ink-2)", lineHeight: 1.6 }}>
            No checkpoint yet. Train on GPU to populate:<br />
            <span style={{ color: "var(--sig-green)" }}>reward ↑</span> ·{" "}
            <span style={{ color: "var(--en-good)" }}>energy ↓</span> ·{" "}
            <span style={{ color: "var(--sig-red)" }}>collisions → 0</span>
          </div>
        ) : (
          <>
            <svg viewBox={`0 0 ${W} ${H}`} style={{ width: "100%", height: H }}>
              <polyline points={poly(pts.map((p) => p.reward), W, H)}
                        fill="none" stroke="var(--sig-green)" strokeWidth={1.4} />
              <polyline points={poly(pts.map((p) => p.energy_kwh), W, H)}
                        fill="none" stroke="var(--en-good)" strokeWidth={1.4} strokeDasharray="3 3" />
            </svg>
            <div style={{ display: "flex", gap: 14, marginTop: 4 }}>
              <span className="mono" style={{ fontSize: 10, color: "var(--sig-green)" }}>— reward</span>
              <span className="mono" style={{ fontSize: 10, color: "var(--en-good)" }}>-- energy/ep</span>
            </div>
          </>
        )}
      </div>
    </Panel>
  );
}
