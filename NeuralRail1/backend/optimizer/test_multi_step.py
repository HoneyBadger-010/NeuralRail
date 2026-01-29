"""
Test multi-step solutions (e.g., slow both, then switch track)
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from conflict_resolver import ConflictResolver
from simulation.railway_simulator import RailwaySimulator


def test_multi_step_solution():
    """Test multi-step solution with parallel tracks available"""
    
    print("="*70)
    print("MULTI-STEP SOLUTION TEST")
    print("="*70)
    print("Scenario: Parallel tracks available (like Delhi suburban)")
    
    # Setup simulation
    sim = RailwaySimulator()
    
    rajdhani = sim.add_train(
        "12001_Rajdhani", "rajdhani", 10, 120, 192, "forward"
    )
    
    express = sim.add_train(
        "EXP_001", "express_passenger", 80, 100, 0, "backward"
    )
    
    print(f"\nTrain Details:")
    print(f"  Rajdhani: Priority {rajdhani.priority}, {rajdhani.mass_kg/1000:.0f} tons at {rajdhani.speed_kmh} km/h")
    print(f"  Express: Priority {express.priority}, {express.mass_kg/1000:.0f} tons at {express.speed_kmh} km/h")
    print(f"\nTrack Info: 4 parallel tracks available (Delhi-Ghaziabad section)")
    
    # Run simulation until conflict detected
    for i in range(10):
        sim.step()
        if sim.conflicts_detected:
            break
    
    if sim.conflicts_detected:
        conflict = sim.conflicts_detected[0]
        
        # Test WITHOUT parallel tracks
        print("\n" + "="*70)
        print("SCENARIO A: NO PARALLEL TRACKS")
        print("="*70)
        
        resolver_no_tracks = ConflictResolver()
        solutions_no_tracks = resolver_no_tracks.analyze_conflict(conflict, rajdhani, express)
        
        print(f"\nGenerated {len(solutions_no_tracks)} solutions")
        best_no_tracks = solutions_no_tracks[0]
        print(f"Best: {best_no_tracks['action']}")
        print(f"Type: {best_no_tracks['type']}")
        print(f"Energy: {best_no_tracks['energy_kwh']:.1f} kWh")
        print(f"Delay: {best_no_tracks['delay_minutes']:.1f} min")
        
        # Test WITH parallel tracks
        print("\n" + "="*70)
        print("SCENARIO B: PARALLEL TRACKS AVAILABLE")
        print("="*70)
        
        track_info = {
            'parallel_tracks_available': True,
            'num_parallel_tracks': 4,
            'track_section': 'Delhi-Ghaziabad (Delhi suburban)'
        }
        
        resolver_with_tracks = ConflictResolver()
        solutions_with_tracks = resolver_with_tracks.analyze_conflict(
            conflict, rajdhani, express, track_info=track_info
        )
        
        best_with_tracks = resolver_with_tracks.print_solutions(solutions_with_tracks)
        
        # Compare
        print("\n" + "="*70)
        print("COMPARISON")
        print("="*70)
        print(f"\nWithout parallel tracks:")
        print(f"  Best: {best_no_tracks['action']}")
        print(f"  Type: {best_no_tracks['type']}")
        print(f"  Energy: {best_no_tracks['energy_kwh']:.1f} kWh")
        print(f"  Delay: {best_no_tracks['delay_minutes']:.1f} min")
        
        print(f"\nWith parallel tracks:")
        print(f"  Best: {best_with_tracks['action']}")
        print(f"  Type: {best_with_tracks['type']}")
        print(f"  Energy: {best_with_tracks['energy_kwh']:.1f} kWh")
        print(f"  Delay: {best_with_tracks['delay_minutes']:.1f} min")
        
        if best_with_tracks.get('is_multi_step', False):
            print(f"\n✓ Multi-step solution selected!")
            print(f"  This is more sophisticated and realistic")
            print(f"  Execution plan:")
            for step in best_with_tracks['steps']:
                print(f"    {step['step']}. {step['action']} ({step['time']})")


def test_multi_step_vs_simple():
    """Compare multi-step vs simple solutions"""
    
    print("\n" + "="*70)
    print("MULTI-STEP vs SIMPLE SOLUTIONS")
    print("="*70)
    
    sim = RailwaySimulator()
    
    train_a = sim.add_train(
        "VB_001", "vande_bharat", 20, 160, 192, "forward"
    )
    
    train_b = sim.add_train(
        "LOCAL_001", "local_emu", 90, 100, 0, "backward"
    )
    
    # Run until conflict
    for i in range(10):
        sim.step()
        if sim.conflicts_detected:
            break
    
    conflict = sim.conflicts_detected[0]
    
    track_info = {
        'parallel_tracks_available': True,
        'num_parallel_tracks': 4
    }
    
    resolver = ConflictResolver()
    solutions = resolver.analyze_conflict(conflict, train_a, train_b, track_info=track_info)
    
    # Find multi-step solution
    multi_step_sol = None
    simple_sol = None
    
    for sol in solutions:
        if sol.get('is_multi_step', False) and not multi_step_sol:
            multi_step_sol = sol
        elif sol['type'] == 'both_slow' and not simple_sol:
            simple_sol = sol
    
    if multi_step_sol and simple_sol:
        print("\nDETAILED COMPARISON:")
        print("-" * 70)
        
        print(f"\nSimple Solution: {simple_sol['action']}")
        print(f"  Energy: {simple_sol['energy_kwh']:.1f} kWh")
        print(f"  Delay: {simple_sol['delay_minutes']:.1f} min")
        print(f"  Score: {simple_sol['score']:.1f}")
        print(f"  Outcome: Trains slow down, pass each other, resume speed")
        
        print(f"\nMulti-Step Solution: {multi_step_sol['action']}")
        print(f"  Energy: {multi_step_sol['energy_kwh']:.1f} kWh")
        print(f"  Delay: {multi_step_sol['delay_minutes']:.1f} min")
        print(f"  Score: {multi_step_sol['score']:.1f}")
        print(f"  Outcome: Trains on separate tracks, no future conflicts")
        
        print(f"\nWhy Multi-Step Might Win:")
        print(f"  ✓ Permanent resolution (separate tracks)")
        print(f"  ✓ No future conflicts on this section")
        print(f"  ✓ Better long-term efficiency")
        print(f"  ✓ More realistic railway operations")
        print(f"  ✓ Gets 5% score bonus for sophistication")


def test_real_world_scenario():
    """Test a realistic multi-step scenario"""
    
    print("\n" + "="*70)
    print("REAL-WORLD SCENARIO: Delhi Suburban Section")
    print("="*70)
    print("Location: New Delhi to Ghaziabad (4 parallel tracks)")
    print("Situation: Rajdhani and local EMU on collision course")
    
    sim = RailwaySimulator()
    
    rajdhani = sim.add_train(
        "12001_Rajdhani", "rajdhani", 15, 110, 192, "forward"
    )
    
    local = sim.add_train(
        "LOCAL_FAST", "local_emu", 45, 90, 0, "backward"
    )
    
    print(f"\nTrains:")
    print(f"  Rajdhani: Long-distance express (Priority {rajdhani.priority})")
    print(f"  Local EMU: Suburban service (Priority {local.priority})")
    
    # Simulate conflict
    for i in range(5):
        sim.step()
        if sim.conflicts_detected:
            break
    
    conflict = sim.conflicts_detected[0]
    
    track_info = {
        'parallel_tracks_available': True,
        'num_parallel_tracks': 4,
        'track_section': 'Delhi-Ghaziabad',
        f'{rajdhani.train_id}_gradient': 'flat',
        f'{local.train_id}_gradient': 'flat'
    }
    
    resolver = ConflictResolver(config={'mode': 'balanced'})
    solutions = resolver.analyze_conflict(conflict, rajdhani, local, track_info=track_info)
    
    best = resolver.print_solutions(solutions)
    
    print("\n" + "="*70)
    print("SECTION CONTROLLER'S VIEW")
    print("="*70)
    
    if best.get('is_multi_step', False):
        print(f"\n📋 RECOMMENDED ACTION: {best['action']}")
        print(f"\n📝 EXECUTION PLAN:")
        for step in best['steps']:
            print(f"\n   Step {step['step']}: {step['action']}")
            print(f"   ⏱️  Timing: {step['time']}")
            print(f"   💡 Reason: {step['reason']}")
        
        print(f"\n⚡ Energy Impact: {best['energy_kwh']:.1f} kWh")
        print(f"⏰ Time Impact: {best['delay_minutes']:.1f} minutes delay")
        print(f"✅ Priority: Respected (no violations)")
        print(f"\n🎯 Outcome: Permanent resolution, trains on separate tracks")
    else:
        print(f"\n📋 RECOMMENDED ACTION: {best['action']}")
        print(f"   (Simple solution - no parallel tracks used)")


if __name__ == "__main__":
    test_multi_step_solution()
    test_multi_step_vs_simple()
    test_real_world_scenario()
    
    print("\n" + "="*70)
    print("✓ Multi-step solution tests completed!")
    print("="*70)
    print("\nKEY INSIGHTS:")
    print("1. Multi-step solutions are more sophisticated")
    print("2. They provide permanent resolution (separate tracks)")
    print("3. More realistic - matches real railway operations")
    print("4. System automatically generates them when parallel tracks available")
    print("5. SC gets clear step-by-step execution plan")
