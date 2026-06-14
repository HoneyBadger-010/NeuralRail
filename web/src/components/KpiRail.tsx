import { motion } from "framer-motion";
import type { Metrics } from "../api/types";
import { fmtInt } from "./common";

function Tile({ label, value, unit, accent, sub }: {
  label: string; value: string; unit?: string; accent?: string; sub?: string;
}) {
  return (
    <div style={{
      padding: "10px 12px", borderRight: "1px solid var(--hair)",
      borderBottom: "1px solid var(--hair)", position: "relative", minWidth: 0,
    }}>
      {accent && <div style={{ position: "absolute", left: 0, top: 10, bottom: 10, width: 2, background: accent }} />}
      <div className="label" style={{ marginBottom: 6 }}>{label}</div>
      <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
        <motion.span key={value} initial={{ opacity: 0.35 }} animate={{ opacity: 1 }} transition={{ duration: 0.3 }}
          className="mono" style={{ fontSize: 22, fontWeight: 600, color: accent ?? "var(--ink-0)", lineHeight: 1 }}>
          {value}
        </motion.span>
        {unit && <span className="mono" style={{ fontSize: 11, color: "var(--ink-2)" }}>{unit}</span>}
      </div>
      {sub && <div className="mono" style={{ fontSize: 10, color: "var(--ink-2)", marginTop: 4 }}>{sub}</div>}
    </div>
  );
}

export default function KpiRail({ m }: { m: Metrics | null }) {
  const v = m ?? ({} as Partial<Metrics>);
  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", borderTop: "1px solid var(--hair)" }}>
      <Tile label="Energy used" value={fmtInt(v.cum_energy_kwh ?? 0)} unit="kWh" accent="var(--en-good)" />
      <Tile label="Cost" value={`₹${fmtInt(v.cum_energy_inr ?? 0)}`} accent="var(--en-good)" />
      <Tile label="CO₂" value={fmtInt(v.cum_energy_co2_kg ?? 0)} unit="kg" accent="var(--en-warn)" />
      <Tile label="On-time"
            value={`${v.trains_on_time ?? 0}/${v.total_trains ?? 0}`}
            sub={`${v.trains_arrived ?? 0} arrived`} accent="var(--sig-green)" />
      <Tile label="Total delay" value={fmtInt(v.total_delay ?? 0)} unit="steps" accent="var(--sig-amber)" />
      <Tile label="Collisions" value={fmtInt(v.collisions ?? 0)}
            accent={(v.collisions ?? 0) > 0 ? "var(--sig-red)" : "var(--ink-2)"} />
    </div>
  );
}
