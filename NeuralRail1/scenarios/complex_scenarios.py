"""
NeuralRail v2.0 - Complex Scenarios
Multi-train, multi-junction, multi-route conflict scenarios
"""

# =============================================================================
# SCENARIO 4: JUNCTION DEADLOCK
# =============================================================================
# 4 trains blocking each other at Mathura Junction (Triangle Junction)
# - Train A wants Platform 1 → Platform 3
# - Train B wants Platform 3 → Platform 1  
# - Train C wants Platform 2 → Platform 3
# - Train D wants Platform 3 → Platform 2
# Classic deadlock - AI must sequence them correctly

SCENARIO_4_JUNCTION_DEADLOCK = {
    "id": "s4",
    "name": "Junction Deadlock - Mathura Triangle",
    "description": "4 trains deadlocked at Mathura Junction - AI must resolve sequencing",
    "complexity": "HIGH",
    "network_type": "junction",
    
    "trains": [
        {
            "train_id": "RAJ_A",
            "train_type": "rajdhani",
            "route": "MAIN_SOUTH",
            "origin": "NDLS",
            "destination": "JHS",
            "current_node": "MTJ",
            "current_platform": 1,
            "target_platform": 3,  # Needs to cross to Platform 3
            "status": "waiting",
            "priority": 2,
            "color": "#FF4444"
        },
        {
            "train_id": "SHT_B",
            "train_type": "shatabdi",
            "route": "AGRA_BRANCH",
            "origin": "AGC",
            "destination": "NDLS",
            "current_node": "MTJ",
            "current_platform": 3,
            "target_platform": 1,  # Needs Platform 1 (blocked by RAJ_A)
            "status": "waiting",
            "priority": 2,
            "color": "#FFD700"
        },

        {
            "train_id": "EXP_C",
            "train_type": "express_passenger",
            "route": "WEST_LINE",
            "origin": "NDLS",
            "destination": "AGC",
            "current_node": "MTJ",
            "current_platform": 2,
            "target_platform": 3,  # Needs Platform 3 (blocked by SHT_B)
            "status": "waiting",
            "priority": 3,
            "color": "#32CD32"
        },
        {
            "train_id": "FRT_D",
            "train_type": "freight_heavy",
            "route": "MAIN_SOUTH",
            "origin": "JHS",
            "destination": "NDLS",
            "current_node": "MTJ",
            "current_platform": 3,
            "target_platform": 2,  # Needs Platform 2 (blocked by EXP_C)
            "status": "waiting",
            "priority": 6,
            "color": "#8B4513"
        }
    ],
    
    "junction_info": {
        "node": "MTJ",
        "name": "Mathura Junction",
        "type": "triangle",
        "platforms": 8,
        "routes_meeting": ["MAIN_SOUTH", "AGRA_BRANCH", "WEST_LINE"],
        "current_state": "DEADLOCK",
        "deadlock_chain": "RAJ_A → SHT_B → EXP_C → FRT_D → RAJ_A (circular)"
    },
    
    "conflict_type": "DEADLOCK",
    "ai_challenge": "Break deadlock by sequencing train movements based on priority",
    
    "optimal_solution": {
        "sequence": ["FRT_D", "EXP_C", "SHT_B", "RAJ_A"],
        "explanation": [
            "1. Move FRT_D (lowest priority) to holding line",
            "2. EXP_C moves to Platform 3 (now free)",
            "3. SHT_B moves to Platform 2 (now free)",
            "4. RAJ_A moves to Platform 3",
            "5. FRT_D returns to Platform 2"
        ],
        "total_delay_minutes": 12,
        "energy_cost_kwh": 85
    },
    
    "judge_points": [
        "Deadlock detection algorithm",
        "Priority-based sequencing",
        "Holding line utilization",
        "Minimum delay solution",
        "Multi-train coordination"
    ]
}


# =============================================================================
# SCENARIO 5: DIAMOND CROSSING CONFLICT
# =============================================================================
# Two trains approaching Aligarh Junction diamond crossing from perpendicular routes
# Must time the crossing to avoid collision

SCENARIO_5_DIAMOND_CROSSING = {
    "id": "s5",
    "name": "Diamond Crossing - Aligarh Junction",
    "description": "Two trains approaching grade crossing - timing critical",
    "complexity": "MEDIUM",
    "network_type": "crossing",
    
    "trains": [
        {
            "train_id": "VB_NORTH",
            "train_type": "vande_bharat",
            "route": "MAIN_SOUTH",
            "origin": "NDLS",
            "destination": "AGC",
            "current_position_km": 115,  # 11 km from crossing
            "speed_kmh": 130,
            "direction": "south",
            "priority": 2,
            "color": "#FFD700",
            "eta_crossing_sec": 305  # ~5 min to crossing
        },
        {
            "train_id": "EXP_CROSS",
            "train_type": "express_passenger",
            "route": "CROSS_LINE",
            "origin": "LKO",
            "destination": "MTJ",
            "current_position_km": 118,  # 8 km from crossing (on cross line)
            "speed_kmh": 100,
            "direction": "west",
            "priority": 3,
            "color": "#32CD32",
            "eta_crossing_sec": 288  # ~4.8 min to crossing
        }
    ],
    
    "crossing_info": {
        "crossing_id": "ALJN_CROSS",
        "location": "ALJN",
        "location_km": 126,
        "type": "diamond",
        "clearance_time_sec": 45,
        "routes": ["MAIN_SOUTH", "CROSS_LINE"],
        "current_state": "CONFLICT_IMMINENT"
    },
    
    "conflict_type": "CROSSING_TIMING",
    "conflict_calculation": {
        "vb_eta": 305,
        "exp_eta": 288,
        "time_gap": 17,  # Only 17 seconds apart!
        "required_gap": 45,  # Need 45 sec clearance
        "collision_risk": "HIGH"
    },
    
    "ai_solutions": [
        {
            "action": "Slow VB_NORTH by 15%",
            "new_eta": 359,
            "gap_achieved": 71,
            "energy_cost": 45,
            "delay_min": 0.9
        },
        {
            "action": "Slow EXP_CROSS by 20%",
            "new_eta": 360,
            "gap_achieved": 55,
            "energy_cost": 38,
            "delay_min": 1.2
        },
        {
            "action": "Stop EXP_CROSS at signal",
            "new_eta": 420,
            "gap_achieved": 115,
            "energy_cost": 65,
            "delay_min": 2.2
        }
    ],
    
    "judge_points": [
        "Diamond crossing physics",
        "Timing-based conflict resolution",
        "Clearance time calculation",
        "Priority consideration (VB > EXP)",
        "Minimum intervention solution"
    ]
}


# =============================================================================
# SCENARIO 6: SINGLE LINE MEET
# =============================================================================
# Two trains on single-line section (Jhansi-Kanpur) must meet at crossing loop

SCENARIO_6_SINGLE_LINE = {
    "id": "s6",
    "name": "Single Line Meet - Jhansi-Kanpur Branch",
    "description": "Two trains on single track - must coordinate at crossing loop",
    "complexity": "MEDIUM",
    "network_type": "single_line",
    
    "trains": [
        {
            "train_id": "RAJ_UP",
            "train_type": "rajdhani",
            "route": "JHS-CNB",
            "origin": "JHS",
            "destination": "CNB",
            "current_position_km": 20,  # 20 km from Jhansi
            "speed_kmh": 75,  # Single line max
            "direction": "up",
            "priority": 2,
            "color": "#FF4444"
        },
        {
            "train_id": "FRT_DN",
            "train_type": "freight_heavy",
            "route": "JHS-CNB",
            "origin": "CNB",
            "destination": "JHS",
            "current_position_km": 120,  # 60 km from Kanpur (180-120)
            "speed_kmh": 60,
            "direction": "down",
            "priority": 6,
            "color": "#8B4513"
        }
    ],
    
    "section_info": {
        "edge_id": "JHS-CNB",
        "total_distance_km": 180,
        "track_type": "SINGLE",
        "signal_type": "ABSOLUTE",  # Token system
        "crossing_loops": [
            {"km": 45, "name": "Loop A", "capacity": 1, "length_m": 700},
            {"km": 90, "name": "Loop B", "capacity": 1, "length_m": 800},
            {"km": 135, "name": "Loop C", "capacity": 1, "length_m": 750}
        ]
    },
    
    "conflict_type": "SINGLE_LINE_MEET",
    "conflict_calculation": {
        "raj_position": 20,
        "frt_position": 120,
        "distance_between": 100,
        "closing_speed": 135,  # 75 + 60
        "time_to_meet_min": 44.4,
        "meet_point_km": 53.3,  # Between Loop A (45) and Loop B (90)
        "nearest_loop": "Loop B at 90 km"
    },
    
    "ai_solutions": [
        {
            "action": "RAJ waits at Loop A (45 km)",
            "description": "RAJ proceeds to Loop A, waits for FRT to pass",
            "raj_wait_min": 25,
            "frt_delay_min": 0,
            "priority_violated": True,
            "energy_cost": 120
        },
        {
            "action": "FRT waits at Loop B (90 km)",
            "description": "FRT proceeds to Loop B, waits for RAJ to pass",
            "raj_delay_min": 0,
            "frt_wait_min": 35,
            "priority_violated": False,
            "energy_cost": 85
        },
        {
            "action": "Meet at Loop B",
            "description": "Both proceed, FRT enters Loop B, RAJ passes on main",
            "raj_delay_min": 5,
            "frt_wait_min": 15,
            "priority_violated": False,
            "energy_cost": 95
        }
    ],
    
    "optimal_solution": "FRT waits at Loop B - respects priority, lower energy",
    
    "judge_points": [
        "Single line operations",
        "Crossing loop utilization",
        "Token/tablet system simulation",
        "Priority-based meet planning",
        "Freight vs Passenger priority"
    ]
}


# =============================================================================
# SCENARIO 7: CASCADE DELAY (Most Complex)
# =============================================================================
# One delayed train causes ripple effect on 6+ other trains

SCENARIO_7_CASCADE = {
    "id": "s7",
    "name": "Cascade Delay - Delhi Hub Chaos",
    "description": "Rajdhani 2hr late - affects 6 connecting trains at Delhi",
    "complexity": "VERY_HIGH",
    "network_type": "hub",
    
    "trigger_event": {
        "train_id": "RAJ_LATE",
        "original_arrival": "14:00",
        "actual_arrival": "16:00",
        "delay_hours": 2,
        "cause": "Fog in Kanpur section",
        "platform_blocked": 5
    },
    
    "affected_trains": [
        {
            "train_id": "SHT_1",
            "train_type": "shatabdi",
            "scheduled_departure": "14:30",
            "platform": 5,  # Same platform as late RAJ!
            "status": "WAITING_PLATFORM",
            "current_delay_min": 0,
            "passengers_affected": 1050,
            "connection_type": "platform_conflict"
        },
        {
            "train_id": "VB_2",
            "train_type": "vande_bharat",
            "scheduled_departure": "15:00",
            "platform": 6,
            "status": "WAITING_CREW",  # Crew was on RAJ_LATE!
            "current_delay_min": 0,
            "passengers_affected": 1128,
            "connection_type": "crew_connection"
        },
        {
            "train_id": "EXP_3",
            "train_type": "express_passenger",
            "scheduled_departure": "14:45",
            "platform": 4,
            "status": "WAITING_RAKE",  # Rake comes from RAJ_LATE
            "current_delay_min": 0,
            "passengers_affected": 1430,
            "connection_type": "rake_turnaround"
        },
        {
            "train_id": "LOC_4",
            "train_type": "local_emu",
            "scheduled_departure": "14:15",
            "platform": 3,
            "status": "BLOCKED_PATH",  # Path blocked by RAJ_LATE approach
            "current_delay_min": 15,
            "passengers_affected": 3600,
            "connection_type": "path_conflict"
        },
        {
            "train_id": "FRT_5",
            "train_type": "freight_heavy",
            "scheduled_departure": "13:00",
            "platform": "Yard",
            "status": "HELD_IN_YARD",  # Held to clear path for RAJ
            "current_delay_min": 180,
            "passengers_affected": 0,
            "connection_type": "priority_hold"
        },
        {
            "train_id": "RAJ_6",
            "train_type": "rajdhani",
            "scheduled_departure": "16:30",
            "platform": 5,
            "status": "PLATFORM_DELAYED",  # Platform 5 still occupied
            "current_delay_min": 0,
            "passengers_affected": 1122,
            "connection_type": "platform_chain"
        }
    ],
    
    "total_impact": {
        "trains_affected": 6,
        "passengers_affected": 8330,
        "total_delay_minutes": 195,
        "platforms_blocked": [3, 4, 5, 6],
        "yard_congestion": "HIGH"
    },
    
    "ai_challenge": "Minimize total system delay while respecting priorities",
    
    "ai_solutions": [
        {
            "strategy": "Priority Cascade",
            "description": "Clear highest priority trains first",
            "sequence": ["RAJ_LATE→P5", "SHT_1→P7", "VB_2→P6", "RAJ_6→P5"],
            "total_delay_saved_min": 45,
            "complexity": "Medium"
        },
        {
            "strategy": "Platform Shuffle",
            "description": "Reassign platforms to minimize conflicts",
            "reassignments": {"SHT_1": "P7", "RAJ_6": "P8", "EXP_3": "P2"},
            "total_delay_saved_min": 65,
            "complexity": "High"
        },
        {
            "strategy": "Crew Swap",
            "description": "Use standby crew for VB_2",
            "actions": ["Standby crew for VB_2", "Original crew rests"],
            "total_delay_saved_min": 90,
            "complexity": "Medium"
        }
    ],
    
    "judge_points": [
        "Cascade effect modeling",
        "Multi-resource conflicts (platform, crew, rake)",
        "System-wide optimization",
        "Passenger impact consideration",
        "Real-time replanning"
    ]
}


# =============================================================================
# ALL COMPLEX SCENARIOS
# =============================================================================

COMPLEX_SCENARIOS = [
    SCENARIO_4_JUNCTION_DEADLOCK,
    SCENARIO_5_DIAMOND_CROSSING,
    SCENARIO_6_SINGLE_LINE,
    SCENARIO_7_CASCADE
]

def get_complex_scenario(scenario_id: str):
    """Get scenario by ID (s4, s5, s6, s7)"""
    for s in COMPLEX_SCENARIOS:
        if s["id"] == scenario_id:
            return s
    return None

def list_complex_scenarios():
    print("\n" + "="*70)
    print("COMPLEX SCENARIOS (v2.0)")
    print("="*70)
    for s in COMPLEX_SCENARIOS:
        print(f"\n{s['id'].upper()}: {s['name']}")
        print(f"   Complexity: {s['complexity']}")
        print(f"   Type: {s['network_type']}")
        print(f"   {s['description']}")
    print("="*70)

if __name__ == "__main__":
    list_complex_scenarios()
