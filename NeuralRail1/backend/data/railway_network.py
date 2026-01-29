"""
Railway Network Data for Delhi Section
This file contains the railway infrastructure data for the Delhi Hub scenario.

Routes:
- NORTH: Sonipat → Narela → Old Delhi → New Delhi
- SOUTH: New Delhi → Nizamuddin → Faridabad → Palwal → Mathura
- EAST: New Delhi → Anand Vihar → Ghaziabad
- WEST: New Delhi → Sadar Bazar → Sarai Rohilla → Delhi Cantt
"""

# Station data: Delhi Section stations
STATIONS = {
    # === NEW DELHI HUB (Central - Reference Point km=0) ===
    "NDLS": {
        "name": "New Delhi",
        "km": 0,
        "elevation": 216,
        "platforms": 16,
        "type": "hub",
        "line": "ALL"
    },
    
    # === NORTH LINE (Punjab/Haryana) ===
    "SNP": {
        "name": "Sonipat",
        "km": 42,
        "elevation": 220,
        "platforms": 4,
        "type": "station",
        "line": "NORTH"
    },
    "NRL": {
        "name": "Narela",
        "km": 32,
        "elevation": 218,
        "platforms": 3,
        "type": "station",
        "line": "NORTH"
    },
    "DLI": {
        "name": "Old Delhi Junction",
        "km": 7,
        "elevation": 216,
        "platforms": 16,
        "type": "junction",
        "line": "NORTH"
    },
    
    # === SOUTH LINE (Agra) ===
    "NZM": {
        "name": "Hazrat Nizamuddin",
        "km": 5,
        "elevation": 214,
        "platforms": 7,
        "type": "major_station",
        "line": "SOUTH"
    },
    "FDB": {
        "name": "Faridabad",
        "km": 25,
        "elevation": 210,
        "platforms": 4,
        "type": "station",
        "line": "SOUTH"
    },
    "PWL": {
        "name": "Palwal",
        "km": 60,
        "elevation": 200,
        "platforms": 4,
        "type": "station",
        "line": "SOUTH"
    },
    "MTJ": {
        "name": "Mathura Junction",
        "km": 141,
        "elevation": 174,
        "platforms": 8,
        "type": "junction",
        "line": "SOUTH"
    },
    
    # === EAST LINE (Lucknow/Kanpur) ===
    "ANVT": {
        "name": "Anand Vihar Terminal",
        "km": 12,
        "elevation": 212,
        "platforms": 7,
        "type": "terminal",
        "line": "EAST"
    },
    "GZB": {
        "name": "Ghaziabad Junction",
        "km": 25,
        "elevation": 210,
        "platforms": 6,
        "type": "junction",
        "line": "EAST"
    },
    
    # === WEST LINE (Jaipur) ===
    "DSB": {
        "name": "Sadar Bazar",
        "km": 4,
        "elevation": 216,
        "platforms": 2,
        "type": "junction",
        "line": "WEST"
    },
    "DEE": {
        "name": "Sarai Rohilla",
        "km": 10,
        "elevation": 218,
        "platforms": 10,
        "type": "terminal",
        "line": "WEST"
    },
    "DEC": {
        "name": "Delhi Cantt",
        "km": 15,
        "elevation": 220,
        "platforms": 4,
        "type": "station",
        "line": "WEST"
    }
}

# Track segments: Delhi Section
TRACK_SEGMENTS = [
    # === NORTH LINE ===
    {
        "id": "SEG_N1",
        "from": "SNP",
        "to": "NRL",
        "distance_km": 10,
        "tracks": 2,
        "max_speed_kmh": 110,
        "gradient": "flat",
        "electrified": True,
        "line": "NORTH"
    },
    {
        "id": "SEG_N2",
        "from": "NRL",
        "to": "DLI",
        "distance_km": 25,
        "tracks": 2,
        "max_speed_kmh": 100,
        "gradient": "flat",
        "electrified": True,
        "line": "NORTH"
    },
    {
        "id": "SEG_N3",
        "from": "DLI",
        "to": "NDLS",
        "distance_km": 7,
        "tracks": 2,
        "max_speed_kmh": 60,
        "gradient": "flat",
        "electrified": True,
        "line": "NORTH"
    },
    
    # === SOUTH LINE ===
    {
        "id": "SEG_S1",
        "from": "NDLS",
        "to": "NZM",
        "distance_km": 5,
        "tracks": 3,
        "max_speed_kmh": 80,
        "gradient": "flat",
        "electrified": True,
        "line": "SOUTH",
        "has_loop": True
    },
    {
        "id": "SEG_S2",
        "from": "NZM",
        "to": "FDB",
        "distance_km": 20,
        "tracks": 3,
        "max_speed_kmh": 110,
        "gradient": "flat",
        "electrified": True,
        "line": "SOUTH"
    },
    {
        "id": "SEG_S3",
        "from": "FDB",
        "to": "PWL",
        "distance_km": 35,
        "tracks": 2,
        "max_speed_kmh": 130,
        "gradient": "flat",
        "electrified": True,
        "line": "SOUTH"
    },
    {
        "id": "SEG_S4",
        "from": "PWL",
        "to": "MTJ",
        "distance_km": 81,
        "tracks": 2,
        "max_speed_kmh": 130,
        "gradient": "flat",
        "electrified": True,
        "line": "SOUTH"
    },
    
    # === EAST LINE ===
    {
        "id": "SEG_E1",
        "from": "NDLS",
        "to": "ANVT",
        "distance_km": 12,
        "tracks": 2,
        "max_speed_kmh": 80,
        "gradient": "flat",
        "electrified": True,
        "line": "EAST"
    },
    {
        "id": "SEG_E2",
        "from": "ANVT",
        "to": "GZB",
        "distance_km": 13,
        "tracks": 2,
        "max_speed_kmh": 100,
        "gradient": "flat",
        "electrified": True,
        "line": "EAST",
        "has_loop": True
    },
    
    # === WEST LINE ===
    {
        "id": "SEG_W1",
        "from": "NDLS",
        "to": "DSB",
        "distance_km": 4,
        "tracks": 2,
        "max_speed_kmh": 60,
        "gradient": "flat",
        "electrified": True,
        "line": "WEST",
        "has_loop": True
    },
    {
        "id": "SEG_W2",
        "from": "DSB",
        "to": "DEE",
        "distance_km": 6,
        "tracks": 2,
        "max_speed_kmh": 80,
        "gradient": "flat",
        "electrified": True,
        "line": "WEST"
    },
    {
        "id": "SEG_W3",
        "from": "DEE",
        "to": "DEC",
        "distance_km": 5,
        "tracks": 2,
        "max_speed_kmh": 100,
        "gradient": "flat",
        "electrified": True,
        "line": "WEST"
    },
    
    # === CROSSOVER ===
    {
        "id": "SEG_X1",
        "from": "DSB",
        "to": "DLI",
        "distance_km": 4,
        "tracks": 2,
        "max_speed_kmh": 60,
        "gradient": "flat",
        "electrified": True,
        "line": "CROSSOVER"
    }
]


TRAIN_TYPES = {
    # Priority 2 - Superfast Express
    "rajdhani": {
        "name": "Rajdhani Express",
        "mass_kg": 850000,
        "max_speed_kmh": 140,
        "acceleration_mps2": 0.38,
        "braking_rate_mps2": 0.72,
        "idle_power_kw": 180,
        "traction_type": "electric",
        "passenger_capacity": 1122,
        "priority": 2,
        "schedule_importance": "critical",
        "priority_class": "Superfast Express"
    },
    "shatabdi": {
        "name": "Shatabdi Express",
        "mass_kg": 720000,
        "max_speed_kmh": 150,
        "acceleration_mps2": 0.42,
        "braking_rate_mps2": 0.75,
        "idle_power_kw": 150,
        "traction_type": "electric",
        "passenger_capacity": 1050,
        "priority": 2,
        "schedule_importance": "critical",
        "priority_class": "Superfast Express"
    },
    "vande_bharat": {
        "name": "Vande Bharat Express",
        "mass_kg": 430000,
        "max_speed_kmh": 160,
        "acceleration_mps2": 1.05,
        "braking_rate_mps2": 1.2,
        "idle_power_kw": 95,
        "traction_type": "electric",
        "passenger_capacity": 1128,
        "priority": 2,
        "schedule_importance": "critical",
        "priority_class": "Superfast Express"
    },
    "duronto": {
        "name": "Duronto Express",
        "mass_kg": 780000,
        "max_speed_kmh": 130,
        "acceleration_mps2": 0.40,
        "braking_rate_mps2": 0.70,
        "idle_power_kw": 160,
        "traction_type": "electric",
        "passenger_capacity": 850,
        "priority": 2,
        "schedule_importance": "critical",
        "priority_class": "Superfast Express"
    },
    
    # Priority 3 - Mail/Express
    "express_passenger": {
        "name": "Express/Mail",
        "mass_kg": 520000,
        "max_speed_kmh": 110,
        "acceleration_mps2": 0.42,
        "braking_rate_mps2": 0.65,
        "idle_power_kw": 65,
        "traction_type": "electric",
        "passenger_capacity": 1430,
        "priority": 3,
        "schedule_importance": "high",
        "priority_class": "Mail/Express"
    },
    
    # Priority 4 - Passenger Trains
    "memu": {
        "name": "MEMU",
        "mass_kg": 350000,
        "max_speed_kmh": 100,
        "acceleration_mps2": 0.70,
        "braking_rate_mps2": 0.90,
        "idle_power_kw": 50,
        "traction_type": "electric",
        "passenger_capacity": 2000,
        "priority": 4,
        "schedule_importance": "medium",
        "priority_class": "Passenger"
    },
    
    # Priority 5 - Suburban Trains
    "local_emu": {
        "name": "Local EMU",
        "mass_kg": 380000,
        "max_speed_kmh": 105,
        "acceleration_mps2": 0.85,
        "braking_rate_mps2": 1.1,
        "idle_power_kw": 55,
        "traction_type": "electric",
        "passenger_capacity": 3600,
        "priority": 5,
        "schedule_importance": "medium",
        "priority_class": "Suburban"
    },
    
    # Priority 6 - Freight Trains
    "freight_heavy": {
        "name": "Heavy Freight",
        "mass_kg": 4200000,
        "max_speed_kmh": 75,
        "acceleration_mps2": 0.12,
        "braking_rate_mps2": 0.28,
        "idle_power_kw": 45,
        "traction_type": "electric",
        "passenger_capacity": 0,
        "priority": 6,
        "schedule_importance": "low",
        "priority_class": "Freight"
    }
}

# Priority class descriptions
PRIORITY_CLASSES = {
    1: {"name": "Special", "description": "VVIP, Military, Accident Relief"},
    2: {"name": "Superfast Express", "description": "Rajdhani, Shatabdi, Vande Bharat"},
    3: {"name": "Mail/Express", "description": "Regular Express trains"},
    4: {"name": "Passenger", "description": "MEMU, DEMU"},
    5: {"name": "Suburban", "description": "Local EMU, Metro"},
    6: {"name": "Freight", "description": "Goods, Container - Lowest Priority"}
}

# =============================================================================
# HELPER FUNCTIONS
# =============================================================================

def get_station_info(station_code):
    """Get information about a specific station"""
    return STATIONS.get(station_code, None)

def get_track_segment(segment_id):
    """Get information about a specific track segment"""
    for segment in TRACK_SEGMENTS:
        if segment["id"] == segment_id:
            return segment
    return None

def get_distance_between_stations(from_station, to_station):
    """Calculate distance between two stations"""
    if from_station in STATIONS and to_station in STATIONS:
        return abs(STATIONS[to_station]["km"] - STATIONS[from_station]["km"])
    return None

def get_elevation_change(from_station, to_station):
    """Calculate elevation change between two stations"""
    if from_station in STATIONS and to_station in STATIONS:
        return STATIONS[to_station]["elevation"] - STATIONS[from_station]["elevation"]
    return 0

def get_stations_on_line(line):
    """Get all stations on a specific line"""
    return {code: data for code, data in STATIONS.items() 
            if data.get("line") == line or data.get("line") == "ALL"}

def print_network_summary():
    """Print network summary"""
    print("\n" + "="*60)
    print("DELHI SECTION RAILWAY NETWORK")
    print("="*60)
    print(f"Stations: {len(STATIONS)}")
    print(f"Track Segments: {len(TRACK_SEGMENTS)}")
    print(f"Train Types: {len(TRAIN_TYPES)}")
    
    print("\n--- LINES ---")
    for line in ["NORTH", "SOUTH", "EAST", "WEST"]:
        stations = get_stations_on_line(line)
        print(f"  {line}: {', '.join(stations.keys())}")
    
    print("="*60)

if __name__ == "__main__":
    print_network_summary()
