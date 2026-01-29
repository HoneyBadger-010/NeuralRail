"""
Test the LLM explainer with mock data (no API keys needed for testing)
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from llm_explainer import LLMExplainer
from optimizer.conflict_resolver import ConflictResolver
from simulation.railway_simulator import RailwaySimulator


def test_fallback_explanations():
    """Test fallback explanations (no API keys needed)"""
    
    print("="*70)
    print("TESTING AI EXPLANATIONS (Fallback Mode)")
    print("="*70)
    print("Note: Using template-based explanations (no API keys required)\n")
    
    # Initialize explainer without API keys
    explainer = LLMExplainer()
    
    # Setup simulation
    sim = RailwaySimulator()
    
    rajdhani = sim.add_train(
        "12001_Rajdhani", "rajdhani", 10, 120, 192, "forward"
    )
    
    freight = sim.add_train(
        "FREIGHT_001", "freight_heavy", 80, 60, 0, "backward"
    )
    
    # Run until conflict
    for i in range(10):
        sim.step()
        if sim.conflicts_detected:
            break
    
    conflict = sim.conflicts_detected[0]
    
    # Get solutions
    resolver = ConflictResolver()
    solutions = resolver.analyze_conflict(conflict, rajdhani, freight)
    
    best_solution = solutions[0]
    
    # Prepare train info for explainer
    train_a_info = {
        'name': rajdhani.name,
        'priority': rajdhani.priority,
        'mass_tons': rajdhani.mass_kg / 1000,
        'position_km': rajdhani.position_km,
        'speed_kmh': rajdhani.speed_kmh
    }
    
    train_b_info = {
        'name': freight.name,
        'priority': freight.priority,
        'mass_tons': freight.mass_kg / 1000,
        'position_km': freight.position_km,
        'speed_kmh': freight.speed_kmh
    }
    
    # Test 1: Explain best solution
    print("TEST 1: EXPLAIN BEST SOLUTION")
    print("-" * 70)
    print(f"Technical: {best_solution['action']}")
    print(f"Energy: {best_solution['energy_kwh']:.1f} kWh")
    print(f"Delay: {best_solution['delay_minutes']:.1f} min")
    print(f"\nAI Explanation:")
    print("-" * 70)
    
    explanation = explainer.explain_decision(
        best_solution, conflict, train_a_info, train_b_info,
        alternative_solutions=solutions[1:3]
    )
    print(explanation)
    
    # Test 2: Compare two solutions
    print("\n" + "="*70)
    print("TEST 2: COMPARE TWO SOLUTIONS")
    print("-" * 70)
    
    if len(solutions) >= 2:
        print(f"Option A: {solutions[0]['action']}")
        print(f"  Energy: {solutions[0]['energy_kwh']:.1f} kWh, Delay: {solutions[0]['delay_minutes']:.1f} min")
        print(f"\nOption B: {solutions[1]['action']}")
        print(f"  Energy: {solutions[1]['energy_kwh']:.1f} kWh, Delay: {solutions[1]['delay_minutes']:.1f} min")
        print(f"\nAI Comparison:")
        print("-" * 70)
        
        comparison = explainer.explain_comparison(solutions[0], solutions[1])
        print(comparison)
    
    # Test 3: Executive summary
    print("\n" + "="*70)
    print("TEST 3: EXECUTIVE SUMMARY")
    print("-" * 70)
    
    energy_saved = solutions[-1]['energy_kwh'] - solutions[0]['energy_kwh']
    summary = explainer.generate_summary(conflict, best_solution, energy_saved)
    print(summary)


def test_multi_step_explanation():
    """Test explanation for multi-step solution"""
    
    print("\n" + "="*70)
    print("TEST 4: MULTI-STEP SOLUTION EXPLANATION")
    print("="*70)
    
    explainer = LLMExplainer()
    
    sim = RailwaySimulator()
    
    rajdhani = sim.add_train(
        "12001_Rajdhani", "rajdhani", 10, 120, 192, "forward"
    )
    
    express = sim.add_train(
        "EXP_001", "express_passenger", 80, 100, 0, "backward"
    )
    
    # Run until conflict
    for i in range(10):
        sim.step()
        if sim.conflicts_detected:
            break
    
    conflict = sim.conflicts_detected[0]
    
    # Get solutions with parallel tracks
    track_info = {
        'parallel_tracks_available': True,
        'num_parallel_tracks': 4
    }
    
    resolver = ConflictResolver()
    solutions = resolver.analyze_conflict(conflict, rajdhani, express, track_info=track_info)
    
    # Find multi-step solution
    multi_step_sol = None
    for sol in solutions:
        if sol.get('is_multi_step', False):
            multi_step_sol = sol
            break
    
    if multi_step_sol:
        train_a_info = {
            'name': rajdhani.name,
            'priority': rajdhani.priority,
            'mass_tons': rajdhani.mass_kg / 1000,
            'position_km': rajdhani.position_km,
            'speed_kmh': rajdhani.speed_kmh
        }
        
        train_b_info = {
            'name': express.name,
            'priority': express.priority,
            'mass_tons': express.mass_kg / 1000,
            'position_km': express.position_km,
            'speed_kmh': express.speed_kmh
        }
        
        print(f"Technical: {multi_step_sol['action']}")
        print(f"Type: {multi_step_sol['type']}")
        print(f"Energy: {multi_step_sol['energy_kwh']:.1f} kWh")
        print(f"\nAI Explanation:")
        print("-" * 70)
        
        explanation = explainer.explain_decision(
            multi_step_sol, conflict, train_a_info, train_b_info
        )
        print(explanation)
        
        print(f"\nExecution Steps:")
        for step in multi_step_sol['steps']:
            print(f"  {step['step']}. {step['action']} ({step['time']})")


def test_ghat_scenario_explanation():
    """Test explanation for ghat scenario with uphill penalty"""
    
    print("\n" + "="*70)
    print("TEST 5: GHAT SCENARIO EXPLANATION")
    print("="*70)
    
    explainer = LLMExplainer()
    
    sim = RailwaySimulator()
    
    freight = sim.add_train(
        "FREIGHT_COAL", "freight_heavy", 60, 50, 106, "forward"
    )
    
    express = sim.add_train(
        "EXP_DOWN", "express_passenger", 100, 90, 54, "backward"
    )
    
    # Simulate conflict
    for i in range(20):
        sim.step()
        if sim.conflicts_detected:
            break
    
    conflict = sim.conflicts_detected[0]
    
    # Get solutions with gradient info
    track_info = {
        f'{freight.train_id}_gradient': 'uphill',
        f'{express.train_id}_gradient': 'downhill'
    }
    
    resolver = ConflictResolver()
    solutions = resolver.analyze_conflict(conflict, freight, express, track_info=track_info)
    
    best_solution = solutions[0]
    
    # Find the "stop freight" solution to show why it's bad
    stop_freight_sol = None
    for sol in solutions:
        if sol['train_affected'] == freight.train_id and sol['type'] == 'stop':
            stop_freight_sol = sol
            break
    
    train_a_info = {
        'name': freight.name,
        'priority': freight.priority,
        'mass_tons': freight.mass_kg / 1000,
        'position_km': freight.position_km,
        'speed_kmh': freight.speed_kmh
    }
    
    train_b_info = {
        'name': express.name,
        'priority': express.priority,
        'mass_tons': express.mass_kg / 1000,
        'position_km': express.position_km,
        'speed_kmh': express.speed_kmh
    }
    
    print(f"Situation: Freight climbing ghat, Express descending")
    print(f"\nRecommended: {best_solution['action']}")
    print(f"Energy: {best_solution['energy_kwh']:.1f} kWh")
    print(f"\nAI Explanation:")
    print("-" * 70)
    
    explanation = explainer.explain_decision(
        best_solution, conflict, train_a_info, train_b_info,
        alternative_solutions=[stop_freight_sol] if stop_freight_sol else None
    )
    print(explanation)
    
    if stop_freight_sol and stop_freight_sol.get('has_gradient_penalty', False):
        print(f"\nWhy NOT stop freight:")
        print(f"  Stopping freight on uphill: {stop_freight_sol['energy_kwh']:.1f} kWh")
        if 'gradient_penalty_kwh' in stop_freight_sol['breakdown']:
            print(f"  Includes {stop_freight_sol['breakdown']['gradient_penalty_kwh']} kWh uphill restart penalty!")
        print(f"  This is {stop_freight_sol['energy_kwh'] / best_solution['energy_kwh']:.1f}x more energy!")


def demo_for_judges():
    """Demo scenario for SIH judges"""
    
    print("\n" + "="*70)
    print("DEMO FOR SIH JUDGES: AI-POWERED EXPLANATIONS")
    print("="*70)
    
    explainer = LLMExplainer()
    
    print("\n📋 SCENARIO: Rajdhani Express vs Heavy Freight Conflict")
    print("-" * 70)
    
    sim = RailwaySimulator()
    
    rajdhani = sim.add_train(
        "12001_Rajdhani", "rajdhani", 10, 120, 192, "forward"
    )
    
    freight = sim.add_train(
        "FREIGHT_001", "freight_heavy", 80, 60, 0, "backward"
    )
    
    print(f"Train A: {rajdhani.name} (Priority {rajdhani.priority})")
    print(f"  Position: {rajdhani.position_km:.1f} km, Speed: {rajdhani.speed_kmh} km/h")
    print(f"  Mass: {rajdhani.mass_kg/1000:.0f} tons")
    
    print(f"\nTrain B: {freight.name} (Priority {freight.priority})")
    print(f"  Position: {freight.position_km:.1f} km, Speed: {freight.speed_kmh} km/h")
    print(f"  Mass: {freight.mass_kg/1000:.0f} tons")
    
    # Run simulation
    for i in range(10):
        sim.step()
        if sim.conflicts_detected:
            break
    
    conflict = sim.conflicts_detected[0]
    
    print(f"\n⚠️  CONFLICT DETECTED:")
    print(f"  Location: {conflict['conflict_position_km']:.1f} km")
    print(f"  Time: {conflict['time_to_conflict_minutes']:.1f} minutes")
    print(f"  Severity: {conflict['severity'].upper()}")
    
    # Get AI recommendations
    resolver = ConflictResolver()
    solutions = resolver.analyze_conflict(conflict, rajdhani, freight)
    
    print(f"\n🤖 AI ANALYSIS:")
    print(f"  Generated {len(solutions)} possible solutions")
    print(f"  Analyzed: Safety, Priority, Energy, Time")
    
    best = solutions[0]
    
    print(f"\n✅ RECOMMENDED SOLUTION:")
    print(f"  Action: {best['action']}")
    print(f"  Energy: {best['energy_kwh']:.1f} kWh")
    print(f"  Delay: {best['delay_minutes']:.1f} minutes")
    print(f"  Priority: {'Respected ✓' if not best['priority_violation'] else 'Override ⚠️'}")
    
    # AI Explanation
    train_a_info = {
        'name': rajdhani.name,
        'priority': rajdhani.priority,
        'mass_tons': rajdhani.mass_kg / 1000,
        'position_km': rajdhani.position_km,
        'speed_kmh': rajdhani.speed_kmh
    }
    
    train_b_info = {
        'name': freight.name,
        'priority': freight.priority,
        'mass_tons': freight.mass_kg / 1000,
        'position_km': freight.position_km,
        'speed_kmh': freight.speed_kmh
    }
    
    print(f"\n💬 AI EXPLANATION FOR SECTION CONTROLLER:")
    print("-" * 70)
    explanation = explainer.explain_decision(
        best, conflict, train_a_info, train_b_info,
        alternative_solutions=solutions[1:3]
    )
    print(explanation)
    
    # Show energy savings
    worst = solutions[-1]
    energy_saved = worst['energy_kwh'] - best['energy_kwh']
    
    print(f"\n💡 ENERGY IMPACT:")
    print(f"  Best solution: {best['energy_kwh']:.1f} kWh")
    print(f"  Worst solution: {worst['energy_kwh']:.1f} kWh")
    print(f"  Energy saved: {energy_saved:.1f} kWh ({(energy_saved/worst['energy_kwh']*100):.1f}% reduction)")
    print(f"  Equivalent to powering {energy_saved/5:.0f} homes for 1 hour")


if __name__ == "__main__":
    test_fallback_explanations()
    test_multi_step_explanation()
    test_ghat_scenario_explanation()
    demo_for_judges()
    
    print("\n" + "="*70)
    print("✓ All AI explanation tests completed!")
    print("="*70)
    print("\nNOTE: These tests use fallback explanations.")
    print("To use actual LLM APIs (Groq/Gemini), set environment variables:")
    print("  export GROQ_API_KEY='your_key_here'")
    print("  export GEMINI_API_KEY='your_key_here'")
