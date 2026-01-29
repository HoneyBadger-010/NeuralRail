"""
NeuralRail - Complete Backend Scenarios
========================================
5 Comprehensive Demo Scenarios for Delhi Section

Scenario 1: Energy Sustainability - Head-on collision, energy-based decision
Scenario 2: Priority Conflict - High-priority vs High-priority train conflict  
Scenario 3: Multi-Train Cascade - 4 trains creating chain reaction conflicts
Scenario 4: Loop Utilization - Using holding/overtaking loops for resolution
Scenario 5: Emergency Response - Track blockage with rerouting

Delhi Section Routes (from delhi_junction.svg):
- NORTH: Sonipat (42km) → Narela (32km) → Old Delhi (7km) → New Delhi (0km)
- SOUTH: New Delhi (0km) → Nizamuddin (5km) → Faridabad (25km) → Palwal (60km) → Mathura (141km)
- EAST: New Delhi (0km) → Anand Vihar (12km) → Ghaziabad (25km)
- WEST: New Delhi (0km) → Sadar Bazar (4km) → Sarai Rohilla (10km) → Delhi Cantt (15km)

Key Infrastructure:
- Ghaziabad Overtaking Loop (EAST)
- Sadar Bazar Holding Loop (WEST)
- Nizamuddin Passing Loop (SOUTH)
- DSB-DLI Crossover (WEST-NORTH connection)
"""

# =============================================================================
# SCENARIO 1: ENERGY SUSTAINABILITY DEMO
# =============================================================================
# Head-on collision on West Line between Rajdhani and Heavy Freight
# Demonstrates: 60% Priority + 40% Energy decision formula
# Conflict happens in the MIDDLE of West Line (around DEE/Sarai Rohilla area)

SCENARIO_1_ENERGY = {
    "id": "energy_sustainability",
    "name": "1. Energy Sustainability Demo",
    "description": "HEAD-ON COLLISION: Rajdhani vs Heavy Freight on West Line",
    "complexity": "MEDIUM",
    "demo_focus": "ENERGY OPTIMIZATION + PRIORITY",
    "route": "WEST",
    
    # Track configuration for this scenario
    "track_info": {
        "route": "WEST",
        "total_length_km": 25,
        "conflict_zone": "Middle of West Line near DEE (Sarai Rohilla)"
    },
    
    "trains": [
        # Rajdhani coming FROM Delhi Cantt direction
        # Heading TO New Delhi - Moderate speed for longer demo time
        {
            "train_id": "RAJ_JPR",
            "train_type": "rajdhani",
            "name": "Jaipur Rajdhani (Jaipur → Delhi)",
            "origin": "DEC",  # From Delhi Cantt
            "destination": "NDLS",
            "destination_km": 0,
            "route": "WEST",
            "initial_position_km": 15,  # At Delhi Cantt (DEC)
            "initial_speed_kmh": 50,  # Slower speed for longer demo time to explain to judges
            "direction": "backward",  # Moving towards NDLS (decreasing km)
            "initial_track": 1,
            "priority": 2,
            "color": "#FF4444",
            "passengers": 1100,
            "mass_kg": 850000,
            "power_kw": 4500,
            "braking_rate_mps2": 0.8,
            "scheduled_arrival": "14:30"
        },
        # Heavy Freight starting FROM New Delhi
        # Heading TO Delhi Cantt - SLOW freight just departing
        # MUST BE BEFORE DSB (4km) when conflict detected so both solutions are valid
        {
            "train_id": "FRT_JPR",
            "train_type": "freight_heavy",
            "name": "Jaipur Freight (Delhi → Jaipur)",
            "origin": "NDLS",
            "destination": "DEC",
            "destination_km": 15,
            "route": "WEST",
            "initial_position_km": 0,  # At New Delhi station
            "initial_speed_kmh": 15,  # Very slow - heavy freight just starting
            "direction": "forward",  # Moving towards DEC (increasing km)
            "initial_track": 1,
            "priority": 6,
            "color": "#8B4513",
            "passengers": 0,
            "cargo_tons": 4200,
            "mass_kg": 4200000,
            "power_kw": 3000,
            "braking_rate_mps2": 0.3,
            "cargo_type": "Coal"
        }
    ],
    
    "conflicts": [{
        "conflict_id": "C1",
        "type": "HEAD_ON_COLLISION",
        "trains": ["RAJ_JPR", "FRT_JPR"],
        "location": "West Line single track section (NDLS-DSB)",
        "conflict_zone_km": {"from": 2, "to": 6},  # Between NDLS (0km) and DEE (10km)
        "time_to_collision_min": 14,  # ~14 min at combined 65 km/h closing speed over 15 km
        "severity": "CRITICAL",
        "description": "Both trains approaching each other on single track section. DSB holding loop at 4km is the resolution point."
    }],
    
    # Energy calculations for judges
    "energy_calculations": {
        "rajdhani": {
            "mass_tons": 850,
            "speed_kmh": 50,  # Slower speed for longer demo
            "kinetic_energy_kwh": 182,  # KE = 0.5 × 850000 × (50/3.6)² / 3600000
            "restart_energy_kwh": 380,
            "total_stop_cost_kwh": 562
        },
        "freight": {
            "mass_tons": 4200,
            "speed_kmh": 15,  # Very slow freight just departing
            "kinetic_energy_kwh": 48,  # KE = 0.5 × 4200000 × (15/3.6)² / 3600000
            "restart_energy_kwh": 850,  # Heavy train, high restart cost
            "total_stop_cost_kwh": 898
        },
        "comparison": {
            "stop_rajdhani_cost": 562,
            "stop_freight_cost": 898,
            "energy_difference_kwh": 336,
            "energy_favors": "Stop Rajdhani (saves 336 kWh)",
            "note": "Energy favors stopping Rajdhani, but priority overrides"
        }
    },
    
    # Priority calculations for judges
    "priority_calculations": {
        "rajdhani": {
            "priority_level": 2,
            "priority_score": 85,  # P2 = 85 points
            "priority_name": "Superfast Express"
        },
        "freight": {
            "priority_level": 6,
            "priority_score": 15,  # P6 = 15 points
            "priority_name": "Heavy Freight"
        },
        "comparison": {
            "priority_difference": 70,  # 85 - 15
            "priority_favors": "Rajdhani (much higher priority)"
        }
    },
    
    # Combined decision using 60% Priority + 40% Energy
    "ai_decision": {
        "formula": "Final Score = (Priority × 0.6) + (Energy × 0.4)",
        "rajdhani_score": {
            "priority_component": 85 * 0.6,  # 51
            "energy_component": 45 * 0.4,    # 18 (higher energy cost = lower score)
            "final_score": 69,
            "explanation": "P2 priority (85pts) gives strong base, energy cost moderate"
        },
        "freight_score": {
            "priority_component": 15 * 0.6,  # 9
            "energy_component": 55 * 0.4,    # 22 (lower energy cost = higher score)
            "final_score": 31,
            "explanation": "P6 priority (15pts) gives weak base despite better energy"
        },
        "decision": "STOP FREIGHT",
        "reason": "Freight has lower final score (31 vs 69). Priority (60%) dominates over energy (40%).",
        "energy_saved_kwh": 89,
        "delay_to_freight_min": 8
    },
    
    "ai_solution": {
        "recommended": "STOP_FREIGHT",
        "action_train": "FRT_JPR",
        "action_type": "STOP",
        "stop_location": "DSB (Sadar Bazar) - 4 km from NDLS",
        "stop_location_km": 4,
        "reason": "Priority 60% + Energy 40% = Stop lower priority freight at DSB loop",
        "steps": [
            {"step": 1, "action": "Detect head-on collision course on West Line single track"},
            {"step": 2, "action": "Calculate: Rajdhani P2 (score 85) vs Freight P6 (score 37)"},
            {"step": 3, "action": "Decision: Stop Freight at DSB (lower score)"},
            {"step": 4, "action": "Freight diverts to DSB Holding Loop and stops"},
            {"step": 5, "action": "Rajdhani passes DSB at full speed (110 km/h)"},
            {"step": 6, "action": "Freight resumes journey to Jaipur after Rajdhani clears"}
        ],
        "delay_to_rajdhani_min": 0,
        "delay_to_freight_min": 7,
        "energy_saved_kwh": 75
    },
    
    "judge_points": [
        "Head-on collision on West Line single track section",
        "AI uses 60% Priority + 40% Energy formula",
        "Rajdhani (P2, score 69) vs Freight (P6, score 31)",
        "Decision: Stop Freight at DSB Holding Loop (4km)",
        "Priority dominates because P2 >> P6 (85 vs 15 base points)",
        "Energy actually favors stopping Rajdhani by 89 kWh",
        "But priority override ensures passenger train proceeds",
        "Real-world: Indian Railways always prioritizes passenger trains"
    ],
    
    # For cross-questioning
    "cross_question_answers": {
        "why_not_stop_rajdhani": "Rajdhani is Priority 2 (Superfast Express) with 1100 passengers. Freight is Priority 6 with no passengers. Even though stopping Rajdhani saves 89 kWh, priority rules dominate (60% weight).",
        "what_if_energy_was_huge": "If stopping Freight cost >1500 kWh more than Rajdhani, energy override would trigger. But here difference is only 89 kWh - not enough to override priority.",
        "why_60_40_split": "60% priority ensures passenger safety and schedule adherence. 40% energy promotes sustainability without compromising service. This balance reflects Indian Railways' real priorities.",
        "what_about_freight_delay": "Freight has flexible schedule. 8-minute delay at DSB Holding Loop is acceptable. Rajdhani passengers paid premium for on-time arrival.",
        "why_dsb_loop": "DSB (Sadar Bazar) at 4km has a holding loop specifically designed for this purpose. Both trains are before DSB when conflict is detected, so either can use the loop."
    }
}


# =============================================================================
# SCENARIO 2: PRIORITY CONFLICT
# =============================================================================
# Two high-priority trains (Rajdhani vs Vande Bharat) competing for same platform
# AI must decide based on schedule, passenger count, and energy

SCENARIO_2_PRIORITY = {
    "id": "priority_conflict",
    "name": "2. Priority Conflict Demo",
    "description": "PLATFORM CONFLICT: Rajdhani vs Vande Bharat - Both P2 priority, same platform",
    "complexity": "HIGH",
    "demo_focus": "PRIORITY RESOLUTION",
    "route": "MULTI",
    
    "trains": [
        {
            "train_id": "RAJ_AGR",
            "train_type": "rajdhani",
            "name": "Agra Rajdhani (Agra → Delhi)",
            "origin": "MTJ",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "SOUTH",
            "initial_position_km": 60,  # At Palwal
            "initial_speed_kmh": 130,
            "direction": "backward",
            "initial_track": 1,
            "target_platform": 1,
            "priority": 2,
            "color": "#FF4444",
            "passengers": 1100,
            "mass_kg": 850000,
            "scheduled_arrival": "14:30"
        },
        {
            "train_id": "VB_CHD",
            "train_type": "vande_bharat",
            "name": "Vande Bharat (Chandigarh → Delhi)",
            "origin": "SNP",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "NORTH",
            "initial_position_km": 32,  # At Narela
            "initial_speed_kmh": 110,
            "direction": "backward",
            "initial_track": 1,
            "target_platform": 1,  # Same platform!
            "priority": 2,  # Same priority!
            "color": "#FFD700",
            "passengers": 1128,
            "mass_kg": 430000,
            "scheduled_arrival": "14:32"
        },
        # Background train
        {
            "train_id": "EXP_LKO",
            "train_type": "express_passenger",
            "name": "Lucknow Express (Delhi → Lucknow)",
            "origin": "NDLS",
            "destination": "GZB",
            "destination_km": 25,
            "route": "EAST",
            "initial_position_km": 5,
            "initial_speed_kmh": 80,
            "direction": "forward",
            "initial_track": 2,
            "priority": 3,
            "color": "#32CD32",
            "passengers": 1400,
            "is_background": True
        }
    ],
    
    "conflicts": [{
        "conflict_id": "C1",
        "type": "PLATFORM_CONFLICT",
        "trains": ["RAJ_AGR", "VB_CHD"],
        "location": "New Delhi Platform 1",
        "time_to_conflict_min": 12,
        "severity": "HIGH"
    }],
    
    "ai_solution": {
        "recommended": "DIVERT_VB_TO_P3",
        "reason": "Rajdhani scheduled 2 min earlier, VB can use Platform 3",
        "alternative_platform": 3,
        "delay_min": 2
    },
    
    "judge_points": [
        "Both trains have P2 priority - tie-breaker needed",
        "AI uses schedule time as tie-breaker",
        "Vande Bharat diverted to Platform 3 with minimal delay"
    ]
}

# =============================================================================
# SCENARIO 3: MULTI-TRAIN CASCADE
# =============================================================================
# 4 trains from 4 directions converging on New Delhi Hub simultaneously
# Creates chain reaction of conflicts that AI must resolve in sequence

SCENARIO_3_CASCADE = {
    "id": "multi_train_cascade",
    "name": "3. Multi-Train Cascade Demo",
    "description": "4 TRAINS CONVERGING: Chain reaction conflicts at Delhi Hub",
    "complexity": "VERY HIGH",
    "demo_focus": "CASCADE RESOLUTION",
    "route": "ALL",
    
    "trains": [
        # NORTH: Shatabdi approaching
        {
            "train_id": "SHT_AMB",
            "train_type": "shatabdi",
            "name": "Ambala Shatabdi (Ambala → Delhi)",
            "origin": "SNP",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "NORTH",
            "initial_position_km": 25,
            "initial_speed_kmh": 100,
            "direction": "backward",
            "initial_track": 1,
            "target_platform": 2,
            "priority": 2,
            "color": "#00CED1",
            "passengers": 980,
            "mass_kg": 720000
        },
        # SOUTH: Rajdhani approaching
        {
            "train_id": "RAJ_BPL",
            "train_type": "rajdhani",
            "name": "Bhopal Rajdhani (Bhopal → Delhi)",
            "origin": "MTJ",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "SOUTH",
            "initial_position_km": 45,
            "initial_speed_kmh": 120,
            "direction": "backward",
            "initial_track": 1,
            "target_platform": 1,
            "priority": 2,
            "color": "#FF4444",
            "passengers": 1122,
            "mass_kg": 850000
        },
        # EAST: Duronto approaching
        {
            "train_id": "DUR_LKO",
            "train_type": "duronto",
            "name": "Lucknow Duronto (Lucknow → Delhi)",
            "origin": "GZB",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "EAST",
            "initial_position_km": 20,
            "initial_speed_kmh": 90,
            "direction": "backward",
            "initial_track": 1,
            "target_platform": 4,
            "priority": 2,
            "color": "#9400D3",
            "passengers": 850,
            "mass_kg": 680000
        },
        # WEST: Freight departing (creates conflict)
        {
            "train_id": "FRT_JPR",
            "train_type": "freight_heavy",
            "name": "Jaipur Freight (Delhi → Jaipur)",
            "origin": "NDLS",
            "destination": "DEC",
            "destination_km": 15,
            "route": "WEST",
            "initial_position_km": 2,
            "initial_speed_kmh": 40,
            "direction": "forward",
            "initial_track": 1,
            "priority": 6,
            "color": "#8B4513",
            "passengers": 0,
            "cargo_tons": 3500,
            "mass_kg": 3500000
        }
    ],
    
    "conflicts": [
        {
            "conflict_id": "C1",
            "type": "HUB_CONVERGENCE",
            "trains": ["SHT_AMB", "RAJ_BPL", "DUR_LKO"],
            "location": "New Delhi Hub",
            "time_to_conflict_min": 10,
            "severity": "CRITICAL"
        },
        {
            "conflict_id": "C2",
            "type": "TRACK_CROSSING",
            "trains": ["FRT_JPR", "SHT_AMB"],
            "location": "NDLS West Junction",
            "time_to_conflict_min": 8,
            "severity": "HIGH"
        }
    ],
    
    "ai_solution": {
        "recommended": "SEQUENTIAL_ARRIVAL",
        "sequence": [
            {"train": "RAJ_BPL", "action": "ARRIVE_FIRST", "platform": 1},
            {"train": "SHT_AMB", "action": "SLOW_TO_80", "then": "ARRIVE", "platform": 2},
            {"train": "DUR_LKO", "action": "HOLD_AT_ANVT", "duration_min": 3},
            {"train": "FRT_JPR", "action": "HOLD_AT_DSB", "duration_min": 5}
        ],
        "total_delay_min": 8,
        "energy_saved_kwh": 1200
    },
    
    "judge_points": [
        "4 trains from 4 directions - complex coordination",
        "AI sequences arrivals to prevent cascade failure",
        "Freight held to let passenger trains pass first",
        "Total system delay minimized to 8 minutes"
    ]
}


# =============================================================================
# SCENARIO 4: LOOP UTILIZATION
# =============================================================================
# Fast train needs to overtake slow train on same track
# AI uses Ghaziabad Overtaking Loop for safe passing

SCENARIO_4_LOOP = {
    "id": "loop_utilization",
    "name": "4. Loop Utilization Demo",
    "description": "OVERTAKING: Rajdhani overtakes Freight using Ghaziabad Loop",
    "complexity": "MEDIUM",
    "demo_focus": "INFRASTRUCTURE USAGE",
    "route": "EAST",
    
    "trains": [
        # Fast train behind slow train
        {
            "train_id": "RAJ_LKO",
            "train_type": "rajdhani",
            "name": "Lucknow Rajdhani (Delhi → Lucknow)",
            "origin": "NDLS",
            "destination": "GZB",
            "destination_km": 25,
            "route": "EAST",
            "initial_position_km": 5,  # Just left NDLS
            "initial_speed_kmh": 110,
            "direction": "forward",
            "initial_track": 2,
            "priority": 2,
            "color": "#FF4444",
            "passengers": 1100,
            "mass_kg": 850000
        },
        # Slow freight ahead
        {
            "train_id": "FRT_KNP",
            "train_type": "freight_heavy",
            "name": "Kanpur Freight (Delhi → Kanpur)",
            "origin": "NDLS",
            "destination": "GZB",
            "destination_km": 25,
            "route": "EAST",
            "initial_position_km": 12,  # At Anand Vihar
            "initial_speed_kmh": 45,
            "direction": "forward",
            "initial_track": 2,  # Same track!
            "priority": 6,
            "color": "#8B4513",
            "passengers": 0,
            "cargo_tons": 4000,
            "mass_kg": 4000000
        },
        # Background: Train coming from opposite direction
        {
            "train_id": "EXP_GZB",
            "train_type": "express_passenger",
            "name": "Ghaziabad Express (Ghaziabad → Delhi)",
            "origin": "GZB",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "EAST",
            "initial_position_km": 22,
            "initial_speed_kmh": 75,
            "direction": "backward",
            "initial_track": 1,
            "priority": 3,
            "color": "#32CD32",
            "passengers": 1200,
            "is_background": True
        }
    ],
    
    "conflicts": [{
        "conflict_id": "C1",
        "type": "SAME_DIRECTION_CATCH",
        "trains": ["RAJ_LKO", "FRT_KNP"],
        "location": "East Line T2",
        "catch_point_km": 18,
        "time_to_catch_min": 6,
        "severity": "HIGH"
    }],
    
    "infrastructure_used": {
        "loop": "Ghaziabad Overtaking Loop",
        "location": "GZB",
        "type": "overtaking",
        "capacity": 1,
        "length_m": 800
    },
    
    "ai_solution": {
        "recommended": "USE_OVERTAKING_LOOP",
        "steps": [
            {"step": 1, "action": "FRT_KNP enters Ghaziabad Loop"},
            {"step": 2, "action": "RAJ_LKO passes on main line at full speed"},
            {"step": 3, "action": "FRT_KNP exits loop and continues"}
        ],
        "delay_to_freight_min": 4,
        "delay_to_rajdhani_min": 0,
        "energy_saved_kwh": 850
    },
    
    "judge_points": [
        "Fast train catching slow train on same track",
        "AI uses Ghaziabad Overtaking Loop",
        "Rajdhani passes without slowing down",
        "Freight delayed only 4 minutes"
    ]
}

# =============================================================================
# SCENARIO 5: EMERGENCY RESPONSE
# =============================================================================
# Track blocked due to incident, trains must be rerouted
# AI handles emergency with minimal disruption

SCENARIO_5_EMERGENCY = {
    "id": "emergency_response",
    "name": "5. Emergency Response Demo",
    "description": "TRACK BLOCKED: South Line incident - AI reroutes trains safely",
    "complexity": "VERY HIGH",
    "demo_focus": "EMERGENCY HANDLING",
    "route": "SOUTH",
    
    "track_incident": {
        "type": "TRACK_BLOCKED",
        "location": "NZM-FDB Section",
        "blocked_track": 1,
        "reason": "Signal failure",
        "duration_estimate_min": 30
    },
    
    "trains": [
        # Train approaching blocked section
        {
            "train_id": "RAJ_AGR",
            "train_type": "rajdhani",
            "name": "Agra Rajdhani (Agra → Delhi)",
            "origin": "MTJ",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "SOUTH",
            "initial_position_km": 40,  # Approaching blocked section
            "initial_speed_kmh": 120,
            "direction": "backward",
            "initial_track": 1,  # Blocked track!
            "priority": 2,
            "color": "#FF4444",
            "passengers": 1100,
            "mass_kg": 850000
        },
        # Train behind first train
        {
            "train_id": "VB_AGR",
            "train_type": "vande_bharat",
            "name": "Vande Bharat (Agra → Delhi)",
            "origin": "MTJ",
            "destination": "NDLS",
            "destination_km": 0,
            "route": "SOUTH",
            "initial_position_km": 70,  # Further back
            "initial_speed_kmh": 130,
            "direction": "backward",
            "initial_track": 1,
            "priority": 2,
            "color": "#FFD700",
            "passengers": 1128,
            "mass_kg": 430000
        },
        # Train departing on same line
        {
            "train_id": "SHT_AGR",
            "train_type": "shatabdi",
            "name": "Agra Shatabdi (Delhi → Agra)",
            "origin": "NDLS",
            "destination": "MTJ",
            "destination_km": 141,
            "route": "SOUTH",
            "initial_position_km": 3,
            "initial_speed_kmh": 90,
            "direction": "forward",
            "initial_track": 2,
            "priority": 2,
            "color": "#00CED1",
            "passengers": 980,
            "mass_kg": 720000
        }
    ],
    
    "conflicts": [
        {
            "conflict_id": "C1",
            "type": "BLOCKED_TRACK",
            "trains": ["RAJ_AGR"],
            "location": "NZM-FDB T1",
            "time_to_blocked_section_min": 5,
            "severity": "CRITICAL"
        },
        {
            "conflict_id": "C2",
            "type": "FOLLOWING_TRAIN",
            "trains": ["RAJ_AGR", "VB_AGR"],
            "location": "South Line",
            "severity": "HIGH"
        }
    ],
    
    "ai_solution": {
        "recommended": "EMERGENCY_REROUTE",
        "steps": [
            {"step": 1, "action": "ALERT", "details": "Detect track blockage at NZM-FDB"},
            {"step": 2, "action": "SLOW_RAJ", "details": "Rajdhani slows to 60 km/h"},
            {"step": 3, "action": "SWITCH_TRACK", "details": "Rajdhani switches to T2 at Faridabad"},
            {"step": 4, "action": "HOLD_VB", "details": "Vande Bharat holds at Palwal"},
            {"step": 5, "action": "COORDINATE", "details": "Shatabdi uses T3 to pass"},
            {"step": 6, "action": "RESUME", "details": "All trains resume after clearance"}
        ],
        "total_delay_min": 15,
        "safety_maintained": True
    },
    
    "judge_points": [
        "Real-time emergency detection",
        "AI immediately slows approaching trains",
        "Track switching to avoid blocked section",
        "Coordination of multiple affected trains",
        "Safety prioritized over schedule"
    ]
}


# =============================================================================
# MASTER SCENARIO LIST
# =============================================================================

ALL_SCENARIOS = [
    SCENARIO_1_ENERGY,
    SCENARIO_2_PRIORITY,
    SCENARIO_3_CASCADE,
    SCENARIO_4_LOOP,
    SCENARIO_5_EMERGENCY
]

# =============================================================================
# HELPER FUNCTIONS
# =============================================================================

def get_scenario_by_index(index):
    """Get scenario by index (1-based)"""
    if 1 <= index <= len(ALL_SCENARIOS):
        return ALL_SCENARIOS[index - 1]
    return None

def get_scenario_by_id(scenario_id):
    """Get scenario by ID string"""
    for scenario in ALL_SCENARIOS:
        if scenario["id"] == scenario_id:
            return scenario
    return None

def get_all_scenarios():
    """Get all scenarios"""
    return ALL_SCENARIOS

def get_scenario_summary():
    """Get summary of all scenarios for API"""
    return [
        {
            "id": s["id"],
            "index": i + 1,
            "name": s["name"],
            "description": s["description"],
            "complexity": s["complexity"],
            "demo_focus": s["demo_focus"],
            "train_count": len(s["trains"]),
            "conflict_count": len(s.get("conflicts", []))
        }
        for i, s in enumerate(ALL_SCENARIOS)
    ]

def list_scenarios():
    """Print all scenarios"""
    print("\n" + "=" * 70)
    print("NEURALRAIL - 5 DEMO SCENARIOS")
    print("=" * 70)
    
    for i, s in enumerate(ALL_SCENARIOS, 1):
        print(f"\n{i}. {s['name']}")
        print(f"   Focus: {s['demo_focus']}")
        print(f"   {s['description']}")
        print(f"   Trains: {len(s['trains'])} | Conflicts: {len(s.get('conflicts', []))}")
        print(f"   Complexity: {s['complexity']}")
        
        if s.get('judge_points'):
            print("   Judge Points:")
            for jp in s['judge_points'][:3]:
                print(f"     • {jp}")
    
    print("\n" + "=" * 70)

# =============================================================================
# TRAIN TYPE DEFINITIONS (for reference)
# =============================================================================

TRAIN_TYPES = {
    "rajdhani": {
        "name": "Rajdhani Express",
        "priority": 2,
        "max_speed_kmh": 140,
        "mass_kg": 850000,
        "color": "#FF4444"
    },
    "vande_bharat": {
        "name": "Vande Bharat Express",
        "priority": 2,
        "max_speed_kmh": 160,
        "mass_kg": 430000,
        "color": "#FFD700"
    },
    "shatabdi": {
        "name": "Shatabdi Express",
        "priority": 2,
        "max_speed_kmh": 150,
        "mass_kg": 720000,
        "color": "#00CED1"
    },
    "duronto": {
        "name": "Duronto Express",
        "priority": 2,
        "max_speed_kmh": 130,
        "mass_kg": 680000,
        "color": "#9400D3"
    },
    "express_passenger": {
        "name": "Express Passenger",
        "priority": 3,
        "max_speed_kmh": 110,
        "mass_kg": 750000,
        "color": "#32CD32"
    },
    "freight_heavy": {
        "name": "Heavy Freight",
        "priority": 6,
        "max_speed_kmh": 60,
        "mass_kg": 4000000,
        "color": "#8B4513"
    },
    "local_emu": {
        "name": "Local EMU",
        "priority": 5,
        "max_speed_kmh": 80,
        "mass_kg": 350000,
        "color": "#00FF88"
    }
}

# =============================================================================
# DELHI STATIONS (for reference)
# =============================================================================

DELHI_STATIONS = {
    # Hub
    "NDLS": {"name": "New Delhi", "km": 0, "line": "HUB"},
    # North Line
    "DLI": {"name": "Old Delhi Junction", "km": 7, "line": "NORTH"},
    "NRL": {"name": "Narela", "km": 32, "line": "NORTH"},
    "SNP": {"name": "Sonipat", "km": 42, "line": "NORTH"},
    # South Line
    "NZM": {"name": "Hazrat Nizamuddin", "km": 5, "line": "SOUTH"},
    "FDB": {"name": "Faridabad", "km": 25, "line": "SOUTH"},
    "PWL": {"name": "Palwal", "km": 60, "line": "SOUTH"},
    "MTJ": {"name": "Mathura Junction", "km": 141, "line": "SOUTH"},
    # East Line
    "ANVT": {"name": "Anand Vihar Terminal", "km": 12, "line": "EAST"},
    "GZB": {"name": "Ghaziabad Junction", "km": 25, "line": "EAST"},
    # West Line
    "DSB": {"name": "Sadar Bazar", "km": 4, "line": "WEST"},
    "DEE": {"name": "Sarai Rohilla", "km": 10, "line": "WEST"},
    "DEC": {"name": "Delhi Cantt", "km": 15, "line": "WEST"}
}

# =============================================================================
# INFRASTRUCTURE (Loops and Crossovers)
# =============================================================================

INFRASTRUCTURE = {
    "loops": [
        {
            "name": "Ghaziabad Overtaking Loop",
            "station": "GZB",
            "line": "EAST",
            "type": "overtaking",
            "capacity": 1,
            "length_m": 800
        },
        {
            "name": "Sadar Bazar Holding Loop",
            "station": "DSB",
            "line": "WEST",
            "type": "holding",
            "capacity": 1,
            "length_m": 600
        },
        {
            "name": "Nizamuddin Passing Loop",
            "station": "NZM",
            "line": "SOUTH",
            "type": "passing",
            "capacity": 1,
            "length_m": 700
        }
    ],
    "crossovers": [
        {
            "name": "DSB-DLI Crossover",
            "connects": ["WEST", "NORTH"],
            "location_km": 4
        }
    ]
}


if __name__ == "__main__":
    list_scenarios()
