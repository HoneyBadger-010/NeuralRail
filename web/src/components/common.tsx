import type { ReactNode } from "react";

export const TRAIN_COLOR: Record<string, string> = {
  "high-speed": "var(--t-hs)",
  express: "var(--t-ex)",
  regular: "var(--t-reg)",
  freight: "var(--t-frt)",
};
export const trainColor = (type: string) => TRAIN_COLOR[type] ?? "var(--t-reg)";

export const SIGNAL_COLOR: Record<string, string> = {
  red: "var(--sig-red)",
  yellow: "var(--sig-amber)",
  green: "var(--sig-green)",
};
export const SIGNAL_GLOW: Record<string, string> = {
  red: "var(--sig-red-glow)",
  yellow: "var(--sig-amber-glow)",
  green: "var(--sig-green-glow)",
};

export const STATUS_COLOR: Record<string, string> = {
  moving: "var(--sig-green)",
  waiting: "var(--sig-amber)",
  arrived: "var(--ink-1)",
  delayed: "var(--sig-red)",
};

export function fmtInt(n: number): string {
  return Math.round(n).toLocaleString("en-IN");
}

export function Panel({
  title, right, children, style, className,
}: {
  title?: string; right?: ReactNode; children: ReactNode;
  style?: React.CSSProperties; className?: string;
}) {
  return (
    <div className={`panel ${className ?? ""}`} style={{ display: "flex", flexDirection: "column", minHeight: 0, ...style }}>
      {title && (
        <div className="panel-head">
          <span className="label">{title}</span>
          {right}
        </div>
      )}
      <div style={{ minHeight: 0, flex: 1, display: "flex", flexDirection: "column" }}>{children}</div>
    </div>
  );
}
