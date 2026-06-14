import { motion } from "framer-motion";
import type { Frame, Topology } from "../api/types";
import { SIGNAL_COLOR, SIGNAL_GLOW, trainColor } from "./common";

export default function NetworkMap({ topo, frame }: { topo: Topology; frame: Frame | null }) {
  const { w, h } = topo.view;
  const nodes = topo.nodes;

  // Group trains by current segment so co-located trains fan out instead of overlapping.
  const bySeg: Record<string, string[]> = {};
  if (frame) for (const t of Object.values(frame.trains)) (bySeg[t.segment] ??= []).push(t.id);

  const conflictSegs = new Set((frame?.conflicts ?? []).map((c) => c.segment));

  return (
    <svg viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="xMidYMid meet"
         style={{ width: "100%", height: "100%", display: "block" }}>
      <defs>
        <pattern id="scan" width="3" height="3" patternUnits="userSpaceOnUse">
          <rect width="3" height="3" fill="transparent" />
          <rect width="3" height="0.5" fill="rgba(236,229,216,0.03)" />
        </pattern>
        <marker id="chev" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto">
          <path d="M2,2 L6,4 L2,6" fill="none" stroke="var(--ink-2)" strokeWidth="1" />
        </marker>
      </defs>

      <rect x="0" y="0" width={w} height={h} fill="url(#scan)" />

      {/* track links */}
      {topo.links.map((l, i) => {
        const a = nodes[l.from], b = nodes[l.to];
        if (!a || !b) return null;
        return (
          <line key={i} x1={a.x} y1={a.y} x2={b.x} y2={b.y}
                stroke="var(--hair-strong)" strokeWidth={3} strokeLinecap="round"
                markerEnd="url(#chev)" />
        );
      })}

      {/* segment blocks + signals + labels */}
      {Object.values(nodes).map((n) => {
        const aspect = frame?.signals[n.id] ?? "green";
        const occupied = !!frame?.occupied[n.id];
        const conflict = conflictSegs.has(n.id);
        const sz = n.is_junction ? 11 : 8;
        return (
          <g key={n.id} transform={`translate(${n.x} ${n.y})`}>
            {conflict && (
              <circle r={20} fill="none" stroke="var(--sig-red)" strokeWidth={1.5}
                      style={{ transformBox: "fill-box", transformOrigin: "center",
                               animation: "pulse-ring 1.4s ease-out infinite" }} />
            )}
            {/* block marker: diamond for junctions, square for plain blocks */}
            {n.is_junction ? (
              <rect x={-sz} y={-sz} width={sz * 2} height={sz * 2} transform="rotate(45)"
                    fill={occupied ? "rgba(244,121,31,0.12)" : "var(--inset)"}
                    stroke={occupied ? "var(--hv-line)" : "var(--hair-strong)"} strokeWidth={1.25} rx={1} />
            ) : (
              <rect x={-sz} y={-sz} width={sz * 2} height={sz * 2}
                    fill={occupied ? "rgba(244,121,31,0.1)" : "var(--inset)"}
                    stroke={occupied ? "var(--hv-line)" : "var(--hair-strong)"} strokeWidth={1.25} rx={1.5} />
            )}
            {/* signal aspect dot */}
            <circle cx={sz + 7} cy={-(sz + 4)} r={3.4}
                    fill={SIGNAL_COLOR[aspect]}
                    style={{ filter: `drop-shadow(0 0 5px ${SIGNAL_GLOW[aspect]})`,
                             animation: aspect === "red" ? "blink 1.6s steps(1) infinite" : undefined }} />
            {/* labels */}
            <text y={sz + 16} textAnchor="middle" fontFamily="var(--font-mono)"
                  fontSize="8.5" fill="var(--ink-2)" letterSpacing="0.02em">{n.id}</text>
            {n.station && (
              <text y={-(sz + 12)} textAnchor="middle" fontFamily="var(--font-cond)"
                    fontSize="9.5" fontWeight="600" letterSpacing="0.08em"
                    fill="var(--ink-1)" style={{ textTransform: "uppercase" }}>
                {n.station.replace("Station ", "")}
              </text>
            )}
          </g>
        );
      })}

      {/* trains — glide between blocks */}
      {frame && Object.values(frame.trains).map((t) => {
        const n = nodes[t.segment];
        if (!n) return null;
        const peers = bySeg[t.segment] ?? [t.id];
        const idx = peers.indexOf(t.id);
        const off = (idx - (peers.length - 1) / 2) * 13;
        const c = trainColor(t.type);
        const dimmed = t.status === "arrived" || t.status === "delayed";
        return (
          <motion.g key={t.id}
            initial={false}
            animate={{ x: n.x, y: n.y + off }}
            transition={{ type: "tween", ease: "easeInOut", duration: 0.42 }}
            style={{ opacity: dimmed ? 0.4 : 1 }}>
            <rect x={-15} y={-7} width={30} height={14} rx={2}
                  fill="var(--bg-2)" stroke={c} strokeWidth={1.5} />
            <rect x={-15} y={-7} width={4} height={14} rx={1} fill={c} />
            <text textAnchor="middle" y={3.5} x={2} fontFamily="var(--font-mono)"
                  fontSize="8.5" fontWeight="600" fill="var(--ink-0)">{t.id}</text>
            {t.status === "waiting" && (
              <circle cx={17} cy={0} r={2.5} fill="var(--sig-amber)"
                      style={{ animation: "blink 1s steps(1) infinite" }} />
            )}
          </motion.g>
        );
      })}
    </svg>
  );
}
