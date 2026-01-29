# Demo Scenarios for NeuralRail - Delhi Section
# 
# 5 COMPREHENSIVE DEMO SCENARIOS:
# 1. Energy Sustainability - Head-on collision, energy-based decision
# 2. Priority Conflict - High-priority vs High-priority train conflict
# 3. Multi-Train Cascade - 4 trains creating chain reaction conflicts
# 4. Loop Utilization - Using holding/overtaking loops for resolution
# 5. Emergency Response - Track blockage with rerouting
#
# Delhi Section Routes (from delhi_junction.svg):
# - NORTH: Sonipat → Narela → Old Delhi → New Delhi
# - SOUTH: New Delhi → Nizamuddin → Faridabad → Palwal → Mathura
# - EAST: New Delhi → Anand Vihar → Ghaziabad
# - WEST: New Delhi → Sadar Bazar → Sarai Rohilla → Delhi Cantt

# Import all scenarios from the comprehensive scenarios file
try:
    from scenarios.all_scenarios import (
        ALL_SCENARIOS,
        SCENARIO_1_ENERGY,
        SCENARIO_2_PRIORITY,
        SCENARIO_3_CASCADE,
        SCENARIO_4_LOOP,
        SCENARIO_5_EMERGENCY,
        TRAIN_TYPES,
        DELHI_STATIONS,
        INFRASTRUCTURE,
        get_scenario_by_index,
        get_scenario_by_id,
        get_all_scenarios,
        get_scenario_summary,
        list_scenarios
    )
except ImportError:
    from all_scenarios import (
        ALL_SCENARIOS,
        SCENARIO_1_ENERGY,
        SCENARIO_2_PRIORITY,
        SCENARIO_3_CASCADE,
        SCENARIO_4_LOOP,
        SCENARIO_5_EMERGENCY,
        TRAIN_TYPES,
        DELHI_STATIONS,
        INFRASTRUCTURE,
        get_scenario_by_index,
        get_scenario_by_id,
        get_all_scenarios,
        get_scenario_summary,
        list_scenarios
    )

# For backward compatibility with existing code
def get_delhi_hub_scenario():
    """Get the primary Energy Sustainability scenario (Scenario 1)"""
    return SCENARIO_1_ENERGY


if __name__ == "__main__":
    print(f"Loaded {len(ALL_SCENARIOS)} scenarios")
    list_scenarios()
