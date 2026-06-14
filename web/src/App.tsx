import { useEffect } from "react";
import { useStore } from "./store/useStore";
import { Panel } from "./components/common";
import TopBar from "./components/TopBar";
import NetworkMap from "./components/NetworkMap";
import KpiRail from "./components/KpiRail";
import DecisionFeed from "./components/DecisionFeed";
import TransportBar from "./components/TransportBar";
import ScenarioSelector from "./components/ScenarioSelector";
import ComparePanel from "./components/ComparePanel";
import TrainingProof from "./components/TrainingProof";
import Icon from "./components/Icon";

function BootScreen() {
  return (
    <div style={{ position: "fixed", inset: 0, display: "grid", placeItems: "center", zIndex: 5 }}>
      <div style={{ textAlign: "center" }}>
        <div style={{ width: 14, height: 30, background: "var(--hv)", borderRadius: 2, margin: "0 auto 16px",
                      boxShadow: "0 0 18px var(--hv-line)", animation: "blink 1.1s steps(1) infinite" }} />
        <div className="label" style={{ fontSize: 12, color: "var(--ink-1)" }}>Waking control room…</div>
      </div>
    </div>
  );
}

export default function App() {
  const { booted, topology, frame, decisions, curves, boot } = useStore();

  useEffect(() => { boot().catch((e) => console.error(e)); }, [boot]);

  if (!booted || !topology) return <BootScreen />;

  return (
    <div style={{ position: "relative", zIndex: 1, height: "100vh", display: "flex", flexDirection: "column" }}>
      <TopBar />

      <div style={{ flex: 1, minHeight: 0, display: "grid",
                    gridTemplateColumns: "248px 1fr 322px", gap: "var(--gap)", padding: "var(--gap)" }}>
        {/* LEFT RAIL */}
        <div style={{ display: "flex", flexDirection: "column", gap: "var(--gap)", minHeight: 0 }}>
          <ScenarioSelector />
          <ComparePanel />
        </div>

        {/* CENTER */}
        <div style={{ display: "flex", flexDirection: "column", gap: "var(--gap)", minHeight: 0 }}>
          <Panel title="Live Network" style={{ flex: 1 }}
                 right={<span className="mono" style={{ fontSize: 10, color: "var(--ink-2)" }}>
                   {frame?.conflicts.length ? `${frame.conflicts.length} contention zone(s)` : "clear"}
                 </span>}>
            <div style={{ flex: 1, background: "var(--inset)", borderRadius: "0 0 var(--r-lg) var(--r-lg)",
                          padding: 8, minHeight: 0 }}>
              <NetworkMap topo={topology} frame={frame} />
            </div>
          </Panel>
          <TrainingProof curves={curves} />
        </div>

        {/* RIGHT RAIL */}
        <div style={{ display: "flex", flexDirection: "column", gap: "var(--gap)", minHeight: 0 }}>
          <Panel title="Energy & Throughput">
            <KpiRail m={frame?.metrics ?? null} />
          </Panel>
          <Panel title="Agent Decision Feed" style={{ flex: 1 }}>
            <DecisionFeed decisions={decisions} />
          </Panel>
        </div>
      </div>

      {/* FOOTER */}
      <div style={{ borderTop: "1px solid var(--hair-strong)", background: "var(--bg-2)" }}>
        <TransportBar />
        <div className="hazard" style={{ display: "flex", alignItems: "center", gap: 8,
              padding: "6px 16px", borderTop: "1px solid var(--hair)" }}>
          <Icon name="warning" size={14} color="var(--hv)" />
          <span className="label" style={{ color: "var(--hv)" }}>Notice</span>
          <span className="mono" style={{ fontSize: 10.5, color: "var(--ink-1)" }}>
            Decision-support & research simulation only — NOT connected to, and NOT for control of, real railway signalling or trains.
          </span>
        </div>
      </div>
    </div>
  );
}
