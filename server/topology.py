"""
Network topology + auto-layout for the UI.

Each segment is a node (trains occupy segments); links are the directed
connections between a segment and its `next_segments`. We compute a cycle-safe
layered left-to-right layout (BFS depth from entry segments) so any task renders
as a clean flow diagram without hand-placed coordinates.
"""

from __future__ import annotations

from collections import deque
from typing import Dict

VIEW_W = 1000
VIEW_H = 560
MARGIN = 70


def compute_topology(sim) -> dict:
    segs = sim._track_segments
    seg_ids = sim.segment_ids()
    next_map = {sid: list(segs[sid].next_segments) for sid in seg_ids}

    incoming = {nx for sid in seg_ids for nx in next_map[sid]}
    sources = [s for s in seg_ids if s not in incoming] or [seg_ids[0]]

    # Cycle-safe BFS layering (shortest depth from any source).
    depth: Dict[str, int] = {s: 0 for s in sources}
    dq = deque(sources)
    while dq:
        cur = dq.popleft()
        for nx in next_map[cur]:
            if nx not in depth:
                depth[nx] = depth[cur] + 1
                dq.append(nx)
    max_existing = max(depth.values()) if depth else 0
    for s in seg_ids:
        depth.setdefault(s, max_existing + 1)

    layers: Dict[int, list] = {}
    for s in seg_ids:
        layers.setdefault(depth[s], []).append(s)
    max_depth = max(layers) if layers else 0

    nodes = {}
    for d, members in sorted(layers.items()):
        members.sort()
        n = len(members)
        x = MARGIN + (d * (VIEW_W - 2 * MARGIN) / max(max_depth, 1))
        for i, sid in enumerate(members):
            y = MARGIN + (i + 0.5) * ((VIEW_H - 2 * MARGIN) / n)
            seg = segs[sid]
            nodes[sid] = {
                "id": sid,
                "x": round(x, 1),
                "y": round(y, 1),
                "is_junction": seg.is_junction,
                "station": seg.station_name,
                "length": seg.length,
            }

    links = [{"from": sid, "to": nx} for sid in seg_ids for nx in next_map[sid]]
    return {"view": {"w": VIEW_W, "h": VIEW_H}, "nodes": nodes, "links": links}
