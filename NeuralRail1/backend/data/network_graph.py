"""
NeuralRail v2.0 - Complex Railway Network Graph
Graph-based representation for multi-route, multi-junction network
"""

from enum import Enum
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass

# =============================================================================
# ENUMS FOR NETWORK ELEMENTS
# =============================================================================

class NodeType(Enum):
    TERMINAL = "terminal"           # Dead-end station (e.g., NDLS, AGC)
    JUNCTION = "junction"           # Multiple routes meet
    STATION = "station"             # Regular station
    HALT = "halt"                   # Small halt/flag station
    CROSSING = "crossing"           # Diamond/grade crossing point

class TrackType(Enum):
    SINGLE = 1                      # Single line with crossing loops
    DOUBLE = 2                      # Standard double line
    TRIPLE = 3                      # Triple line (ghat sections)
    QUADRUPLE = 4                   # Quad line (suburban)

class SignalType(Enum):
    AUTOMATIC = "automatic"         # Track circuit based
    ABSOLUTE = "absolute"           # Token/tablet system
    CTC = "ctc"                     # Centralized Traffic Control
    MANUAL = "manual"               # Manual signals

class JunctionType(Enum):
    SIMPLE = "simple"               # Two routes meet
    Y_JUNCTION = "y_junction"       # Route splits into two
    TRIANGLE = "triangle"           # Three-way connection
    DIAMOND = "diamond"             # Grade crossing
    FLYING = "flying"               # Grade-separated (overpass)


# =============================================================================
# DATA CLASSES FOR NETWORK ELEMENTS
# =============================================================================

@dataclass
class Platform:
    """Platform at a station"""
    number: int
    length_m: int
    track_number: int
    can_handle: List[str]  # Train types that can use this platform
    occupied_by: Optional[str] = None

@dataclass
class Node:
    """Station/Junction node in the network"""
    code: str
    name: str
    node_type: NodeType
    km_marker: float
    elevation_m: int
    platforms: List[Platform]
    routes: List[str]  # Route IDs passing through
    junction_type: Optional[JunctionType] = None
    
@dataclass
class Edge:
    """Track segment between two nodes"""
    edge_id: str
    from_node: str
    to_node: str
    distance_km: float
    track_type: TrackType
    max_speed_kmh: int
    gradient_percent: float
    electrified: bool
    signal_type: SignalType
    crossing_loops: List[float] = None  # km markers for crossing loops (single line)

@dataclass
class Crossing:
    """Diamond/grade crossing point"""
    crossing_id: str
    location_node: str
    routes_crossing: List[str]
    crossing_type: JunctionType
    clearance_time_sec: int  # Time needed to clear crossing

# =============================================================================
# DELHI SECTION NETWORK (Matching delhi_junction.svg)
# =============================================================================
# 4 Routes converging on New Delhi Hub:
# - NORTH (Green): Sonipat → Narela → Old Delhi → New Delhi
# - SOUTH (Cyan): New Delhi → Nizamuddin → Faridabad → Palwal → Mathura
# - EAST (Orange): New Delhi → Anand Vihar → Ghaziabad
# - WEST (Purple): New Delhi → Sadar Bazar → Sarai Rohilla → Delhi Cantt

# Network Nodes (Stations & Junctions)
NODES: Dict[str, dict] = {
    # === NEW DELHI HUB (Central) ===
    "NDLS": {
        "name": "New Delhi",
        "type": NodeType.TERMINAL,
        "km": 0,
        "elevation": 216,
        "platforms": 16,
        "routes": ["NORTH_LINE", "SOUTH_LINE", "EAST_LINE", "WEST_LINE"],
        "junction_type": None,
        "is_hub": True
    },
    
    # === NORTH LINE (Punjab/Haryana) ===
    "SNP": {
        "name": "Sonipat",
        "type": NodeType.STATION,
        "km": 42,  # From NDLS
        "elevation": 220,
        "platforms": 4,
        "routes": ["NORTH_LINE"],
        "junction_type": None,
        "tracks": 2
    },
    "NRL": {
        "name": "Narela",
        "type": NodeType.STATION,
        "km": 32,
        "elevation": 218,
        "platforms": 3,
        "routes": ["NORTH_LINE"],
        "junction_type": None,
        "tracks": 2
    },
    "DLI": {
        "name": "Old Delhi Junction",
        "type": NodeType.JUNCTION,
        "km": 7,
        "elevation": 216,
        "platforms": 16,
        "routes": ["NORTH_LINE", "WEST_LINE"],
        "junction_type": JunctionType.Y_JUNCTION,
        "tracks": 2
    },
    
    # === SOUTH LINE (Agra) ===
    "NZM": {
        "name": "Hazrat Nizamuddin",
        "type": NodeType.STATION,
        "km": 5,  # South of NDLS
        "elevation": 214,
        "platforms": 7,
        "routes": ["SOUTH_LINE"],
        "junction_type": None,
        "tracks": 3,
        "has_loop": True,
        "loop_type": "passing"
    },
    "FDB": {
        "name": "Faridabad",
        "type": NodeType.STATION,
        "km": 25,
        "elevation": 210,
        "platforms": 4,
        "routes": ["SOUTH_LINE"],
        "junction_type": None,
        "tracks": 3,
        "t3_merge": True
    },
    "PWL": {
        "name": "Palwal",
        "type": NodeType.STATION,
        "km": 60,
        "elevation": 200,
        "platforms": 4,
        "routes": ["SOUTH_LINE"],
        "junction_type": None,
        "tracks": 2
    },
    "MTJ": {
        "name": "Mathura Junction",
        "type": NodeType.JUNCTION,
        "km": 141,
        "elevation": 174,
        "platforms": 8,
        "routes": ["SOUTH_LINE", "AGRA_BRANCH"],
        "junction_type": JunctionType.TRIANGLE,
        "tracks": 4
    },
    
    # === EAST LINE (Lucknow/Kanpur) ===
    "ANVT": {
        "name": "Anand Vihar Terminal",
        "type": NodeType.TERMINAL,
        "km": 12,  # East of NDLS
        "elevation": 212,
        "platforms": 7,
        "routes": ["EAST_LINE"],
        "junction_type": None,
        "tracks": 2
    },
    "GZB": {
        "name": "Ghaziabad Junction",
        "type": NodeType.JUNCTION,
        "km": 25,
        "elevation": 210,
        "platforms": 6,
        "routes": ["EAST_LINE", "MORADABAD_LINE"],
        "junction_type": JunctionType.Y_JUNCTION,
        "tracks": 2,
        "has_loop": True,
        "loop_type": "overtaking"
    },
    
    # === WEST LINE (Jaipur) ===
    "DSB": {
        "name": "Sadar Bazar",
        "type": NodeType.JUNCTION,
        "km": 4,  # West of NDLS
        "elevation": 216,
        "platforms": 2,
        "routes": ["WEST_LINE", "NORTH_LINE"],
        "junction_type": JunctionType.Y_JUNCTION,
        "tracks": 2,
        "has_loop": True,
        "loop_type": "holding"
    },
    "DEE": {
        "name": "Sarai Rohilla",
        "type": NodeType.TERMINAL,
        "km": 10,
        "elevation": 218,
        "platforms": 10,
        "routes": ["WEST_LINE"],
        "junction_type": None,
        "tracks": 2
    },
    "DEC": {
        "name": "Delhi Cantt",
        "type": NodeType.STATION,
        "km": 15,
        "elevation": 220,
        "platforms": 4,
        "routes": ["WEST_LINE"],
        "junction_type": None,
        "tracks": 2
    },
    
    # === EXTENDED NETWORK (for longer routes) ===
    "AGC": {
        "name": "Agra Cantt",
        "type": NodeType.STATION,
        "km": 195,
        "elevation": 171,
        "platforms": 6,
        "routes": ["SOUTH_LINE"],
        "junction_type": None
    },
    "CNB": {
        "name": "Kanpur Central",
        "type": NodeType.JUNCTION,
        "km": 440,
        "elevation": 126,
        "platforms": 10,
        "routes": ["EAST_LINE"],
        "junction_type": JunctionType.SIMPLE
    },
    "LKO": {
        "name": "Lucknow Junction",
        "type": NodeType.JUNCTION,
        "km": 512,
        "elevation": 123,
        "platforms": 9,
        "routes": ["EAST_LINE"],
        "junction_type": JunctionType.TRIANGLE
    }
}


# Network Edges (Track Segments) - Delhi Section
EDGES: List[dict] = [
    # === NORTH LINE (Sonipat → Narela → Old Delhi → New Delhi) ===
    {
        "id": "SNP-NRL",
        "from": "SNP",
        "to": "NRL",
        "distance_km": 10,
        "tracks": TrackType.DOUBLE,
        "max_speed": 110,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "NORTH"
    },
    {
        "id": "NRL-DLI",
        "from": "NRL",
        "to": "DLI",
        "distance_km": 25,
        "tracks": TrackType.DOUBLE,
        "max_speed": 100,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "NORTH"
    },
    {
        "id": "DLI-NDLS",
        "from": "DLI",
        "to": "NDLS",
        "distance_km": 7,
        "tracks": TrackType.DOUBLE,
        "max_speed": 60,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "NORTH"
    },
    
    # === SOUTH LINE (New Delhi → Nizamuddin → Faridabad → Palwal → Mathura) ===
    {
        "id": "NDLS-NZM",
        "from": "NDLS",
        "to": "NZM",
        "distance_km": 5,
        "tracks": TrackType.TRIPLE,
        "max_speed": 80,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "SOUTH",
        "has_loop": True,
        "loop_type": "passing"
    },
    {
        "id": "NZM-FDB",
        "from": "NZM",
        "to": "FDB",
        "distance_km": 20,
        "tracks": TrackType.TRIPLE,
        "max_speed": 110,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "SOUTH"
    },
    {
        "id": "FDB-PWL",
        "from": "FDB",
        "to": "PWL",
        "distance_km": 35,
        "tracks": TrackType.DOUBLE,  # T3 merged at Faridabad
        "max_speed": 130,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "SOUTH",
        "note": "T3 merges into T1/T2 at Faridabad"
    },
    {
        "id": "PWL-MTJ",
        "from": "PWL",
        "to": "MTJ",
        "distance_km": 81,
        "tracks": TrackType.DOUBLE,
        "max_speed": 130,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "SOUTH"
    },
    
    # === EAST LINE (New Delhi → Anand Vihar → Ghaziabad) ===
    {
        "id": "NDLS-ANVT",
        "from": "NDLS",
        "to": "ANVT",
        "distance_km": 12,
        "tracks": TrackType.DOUBLE,
        "max_speed": 80,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "EAST"
    },
    {
        "id": "ANVT-GZB",
        "from": "ANVT",
        "to": "GZB",
        "distance_km": 13,
        "tracks": TrackType.DOUBLE,
        "max_speed": 100,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "EAST",
        "has_loop": True,
        "loop_type": "overtaking"
    },
    
    # === WEST LINE (New Delhi → Sadar Bazar → Sarai Rohilla → Delhi Cantt) ===
    {
        "id": "NDLS-DSB",
        "from": "NDLS",
        "to": "DSB",
        "distance_km": 4,
        "tracks": TrackType.DOUBLE,
        "max_speed": 60,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "WEST",
        "has_loop": True,
        "loop_type": "holding"
    },
    {
        "id": "DSB-DEE",
        "from": "DSB",
        "to": "DEE",
        "distance_km": 6,
        "tracks": TrackType.DOUBLE,
        "max_speed": 80,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "WEST"
    },
    {
        "id": "DEE-DEC",
        "from": "DEE",
        "to": "DEC",
        "distance_km": 5,
        "tracks": TrackType.DOUBLE,
        "max_speed": 100,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "WEST"
    },
    
    # === CROSS CONNECTION (Sadar Bazar ↔ Old Delhi) ===
    {
        "id": "DSB-DLI",
        "from": "DSB",
        "to": "DLI",
        "distance_km": 4,
        "tracks": TrackType.DOUBLE,
        "max_speed": 60,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "CROSSOVER",
        "is_crossover": True
    },
    
    # === EXTENDED ROUTES (for longer journeys) ===
    {
        "id": "GZB-CNB",
        "from": "GZB",
        "to": "CNB",
        "distance_km": 415,
        "tracks": TrackType.DOUBLE,
        "max_speed": 130,
        "gradient": 0.1,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "EAST"
    },
    {
        "id": "CNB-LKO",
        "from": "CNB",
        "to": "LKO",
        "distance_km": 72,
        "tracks": TrackType.DOUBLE,
        "max_speed": 130,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "EAST"
    },
    {
        "id": "MTJ-AGC",
        "from": "MTJ",
        "to": "AGC",
        "distance_km": 54,
        "tracks": TrackType.DOUBLE,
        "max_speed": 130,
        "gradient": 0.0,
        "electrified": True,
        "signal": SignalType.AUTOMATIC,
        "line": "SOUTH"
    }
]

# Loops and Sidings (Delhi Section)
LOOPS: List[dict] = [
    {
        "id": "GZB_OVERTAKING",
        "name": "Ghaziabad Overtaking Loop",
        "location": "GZB",
        "type": "overtaking",
        "capacity": 1,
        "length_m": 800,
        "can_hold": ["express_passenger", "passenger", "memu", "demu"],
        "line": "EAST"
    },
    {
        "id": "DSB_HOLDING",
        "name": "Sadar Bazar Holding Loop",
        "location": "DSB",
        "type": "holding",
        "capacity": 1,
        "length_m": 700,
        "can_hold": ["freight_heavy", "freight_container", "express_passenger"],
        "line": "WEST"
    },
    {
        "id": "NZM_PASSING",
        "name": "Nizamuddin Passing Loop",
        "location": "NZM",
        "type": "passing",
        "capacity": 1,
        "length_m": 750,
        "can_hold": ["any"],
        "line": "SOUTH"
    }
]

# Diamond Crossings (none in Delhi section, but kept for compatibility)
CROSSINGS: List[dict] = []


# =============================================================================
# NETWORK GRAPH CLASS
# =============================================================================

class RailwayNetwork:
    """Graph-based railway network for complex routing and conflict detection"""
    
    def __init__(self):
        self.nodes = NODES
        self.edges = {e["id"]: e for e in EDGES}
        self.crossings = {c["id"]: c for c in CROSSINGS}
        self.loops = {l["id"]: l for l in LOOPS}
        self._build_adjacency()
    
    def _build_adjacency(self):
        """Build adjacency list for graph traversal"""
        self.adjacency = {code: [] for code in self.nodes}
        for edge in EDGES:
            self.adjacency[edge["from"]].append((edge["to"], edge["id"]))
            self.adjacency[edge["to"]].append((edge["from"], edge["id"]))
    
    def get_neighbors(self, node_code: str) -> List[Tuple[str, str]]:
        """Get neighboring nodes and edge IDs"""
        return self.adjacency.get(node_code, [])
    
    def get_edge(self, from_node: str, to_node: str) -> Optional[dict]:
        """Get edge between two nodes"""
        for edge in EDGES:
            if (edge["from"] == from_node and edge["to"] == to_node) or \
               (edge["from"] == to_node and edge["to"] == from_node):
                return edge
        return None
    
    def find_path(self, start: str, end: str) -> List[str]:
        """Find shortest path using BFS (simple version)"""
        from collections import deque
        
        if start not in self.nodes or end not in self.nodes:
            return []
        
        queue = deque([(start, [start])])
        visited = {start}
        
        while queue:
            current, path = queue.popleft()
            if current == end:
                return path
            
            for neighbor, _ in self.adjacency[current]:
                if neighbor not in visited:
                    visited.add(neighbor)
                    queue.append((neighbor, path + [neighbor]))
        
        return []
    
    def get_route_distance(self, path: List[str]) -> float:
        """Calculate total distance of a path"""
        total = 0
        for i in range(len(path) - 1):
            edge = self.get_edge(path[i], path[i+1])
            if edge:
                total += edge["distance_km"]
        return total
    
    def get_junctions(self) -> List[str]:
        """Get all junction nodes"""
        return [code for code, node in self.nodes.items() 
                if node["type"] == NodeType.JUNCTION]
    
    def get_crossings_on_route(self, route_id: str) -> List[dict]:
        """Get all diamond crossings on a route"""
        return [c for c in CROSSINGS if route_id in c["routes"]]
    
    def get_loops(self) -> List[dict]:
        """Get all loops/sidings in the network"""
        return LOOPS
    
    def get_loop_at_station(self, station_code: str) -> Optional[dict]:
        """Get loop at a specific station"""
        for loop in LOOPS:
            if loop["location"] == station_code:
                return loop
        return None
    
    def get_loops_on_line(self, line: str) -> List[dict]:
        """Get all loops on a specific line"""
        return [l for l in LOOPS if l["line"] == line]
    
    def get_edges_on_line(self, line: str) -> List[dict]:
        """Get all edges on a specific line"""
        return [e for e in EDGES if e.get("line") == line]
    
    def get_stations_on_line(self, line: str) -> List[str]:
        """Get all stations on a specific line"""
        return [code for code, node in self.nodes.items() 
                if line in node.get("routes", [])]


# Singleton instance
network = RailwayNetwork()

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

def get_node(code: str) -> Optional[dict]:
    return NODES.get(code)

def get_all_routes() -> List[str]:
    routes = set()
    for node in NODES.values():
        routes.update(node["routes"])
    return list(routes)

def print_network_summary():
    print("\n" + "="*60)
    print("DELHI SECTION RAILWAY NETWORK")
    print("="*60)
    print(f"Nodes (Stations): {len(NODES)}")
    print(f"Edges (Track Segments): {len(EDGES)}")
    print(f"Loops/Sidings: {len(LOOPS)}")
    print(f"Routes: {get_all_routes()}")
    print(f"Junctions: {network.get_junctions()}")
    
    print("\n--- LINES ---")
    for line in ["NORTH_LINE", "SOUTH_LINE", "EAST_LINE", "WEST_LINE"]:
        stations = network.get_stations_on_line(line)
        print(f"  {line}: {' → '.join(stations)}")
    
    print("\n--- LOOPS ---")
    for loop in LOOPS:
        print(f"  {loop['name']} at {loop['location']} ({loop['type']})")
    
    print("="*60)


if __name__ == "__main__":
    print_network_summary()
    
    # Test pathfinding - Delhi routes
    print("\n--- PATH TESTS ---")
    
    # North to South
    path = network.find_path("SNP", "MTJ")
    if path:
        print(f"Sonipat → Mathura: {' → '.join(path)}")
        print(f"Distance: {network.get_route_distance(path)} km")
    
    # East to West
    path = network.find_path("GZB", "DEC")
    if path:
        print(f"Ghaziabad → Delhi Cantt: {' → '.join(path)}")
        print(f"Distance: {network.get_route_distance(path)} km")
