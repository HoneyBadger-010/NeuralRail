import { AnimatePresence, motion } from "framer-motion";
import type { LoggedDecision } from "../store/useStore";

const KIND_COLOR: Record<string, string> = {
  hold: "var(--sig-amber)",
  release: "var(--sig-green)",
};

export default function DecisionFeed({ decisions }: { decisions: LoggedDecision[] }) {
  return (
    <div style={{ overflowY: "auto", flex: 1, padding: "4px 0" }}>
      {decisions.length === 0 && (
        <div className="mono" style={{ color: "var(--ink-2)", fontSize: 11, padding: "12px 14px" }}>
          awaiting controller decisions…
        </div>
      )}
      <AnimatePresence initial={false}>
        {decisions.map((d) => {
          const c = KIND_COLOR[d.kind] ?? "var(--ink-1)";
          return (
            <motion.div key={d.uid}
              initial={{ opacity: 0, x: -10, height: 0 }}
              animate={{ opacity: 1, x: 0, height: "auto" }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.25 }}
              style={{ padding: "7px 14px", borderBottom: "1px solid var(--hair)", display: "flex", gap: 10 }}>
              <span className="mono" style={{ color: "var(--ink-2)", fontSize: 10, width: 26, flexShrink: 0, paddingTop: 1 }}>
                t{String(d.t).padStart(2, "0")}
              </span>
              <span style={{ width: 3, alignSelf: "stretch", background: c, flexShrink: 0, borderRadius: 2 }} />
              <div style={{ minWidth: 0 }}>
                <div className="mono" style={{ fontSize: 12, color: "var(--ink-0)", fontWeight: 500 }}>{d.text}</div>
                <div style={{ fontFamily: "var(--font-cond)", fontSize: 11, color: "var(--ink-1)", letterSpacing: "0.02em" }}>
                  {d.reason}
                </div>
              </div>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
}
