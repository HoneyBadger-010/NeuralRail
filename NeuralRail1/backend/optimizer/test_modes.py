"""
Test different operational modes of the conflict resolver
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from conflict_resolver import ConflictResolver
from simulation.railway_simulator import RailwaySimulator


def test_different_modes():
    """Test how different modes affect decision-making"""
    
    print("="*70)
    print("TESTING DIFFERENT OPERATIONAL MODES")
    print("="*70)
    
    # Setup same conflict scenario
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
    
    # Test Mode 1: Balanced (Default)
    print("\n" + "="*70)
    print("MODE 1: BALANCED (Default)")
    print("Energy: 40%, Delay: 20%, Priority: 40%")
    print("="*70)
    
    resolver_balanced = ConflictResolver(config={'mode': 'balanced'})
    solutions_balanced = resolver_balanced.analyze_conflict(conflict, rajdhani, freight)
    best_balanced = solutions_balanced[0]
    print(f"\n>>> BEST: {best_balanced['action']}")
    print(f"    Energy: {best_balanced['energy_kwh']:.1f} kWh")
    print(f"    Delay: {best_balanced['delay_minutes']:.1f} min")
    print(f"    Score: {best_balanced['score']:.1f}")
    
    # Test Mode 2: Energy Priority
    print("\n" + "="*70)
    print("MODE 2: ENERGY PRIORITY")
    print("Energy: 60%, Delay: 10%, Priority: 30%")
    print("Use case: Low power supply, need to conserve energy")
    print("="*70)
    
    resolver_energy = ConflictResolver(config={'mode': 'energy_priority'})
    solutions_energy = resolver_energy.analyze_conflict(conflict, rajdhani, freight)
    best_energy = solutions_energy[0]
    print(f"\n>>> BEST: {best_energy['action']}")
    print(f"    Energy: {best_energy['energy_kwh']:.1f} kWh")
    print(f"    Delay: {best_energy['delay_minutes']:.1f} min")
    print(f"    Score: {best_energy['score']:.1f}")
    
    # Test Mode 3: Time Priority
    print("\n" + "="*70)
    print("MODE 3: TIME PRIORITY")
    print("Energy: 20%, Delay: 50%, Priority: 30%")
    print("Use case: Rush hour, minimize delays")
    print("="*70)
    
    resolver_time = ConflictResolver(config={'mode': 'time_priority'})
    solutions_time = resolver_time.analyze_conflict(conflict, rajdhani, freight)
    best_time = solutions_time[0]
    print(f"\n>>> BEST: {best_time['action']}")
    print(f"    Energy: {best_time['energy_kwh']:.1f} kWh")
    print(f"    Delay: {best_time['delay_minutes']:.1f} min")
    print(f"    Score: {best_time['score']:.1f}")
    
    # Test Mode 4: Strict Priority
    print("\n" + "="*70)
    print("MODE 4: STRICT PRIORITY")
    print("Energy: 20%, Delay: 20%, Priority: 60%")
    print("Use case: VIP trains, strictly follow priority rules")
    print("="*70)
    
    resolver_strict = ConflictResolver(config={'mode': 'strict_priority'})
    solutions_strict = resolver_strict.analyze_conflict(conflict, rajdhani, freight)
    best_strict = solutions_strict[0]
    print(f"\n>>> BEST: {best_strict['action']}")
    print(f"    Energy: {best_strict['energy_kwh']:.1f} kWh")
    print(f"    Delay: {best_strict['delay_minutes']:.1f} min")
    print(f"    Score: {best_strict['score']:.1f}")
    
    # Summary comparison
    print("\n" + "="*70)
    print("COMPARISON ACROSS MODES")
    print("="*70)
    print(f"{'Mode':<20} {'Best Solution':<30} {'Energy (kWh)':<15} {'Delay (min)':<12}")
    print("-"*70)
    print(f"{'Balanced':<20} {best_balanced['action']:<30} {best_balanced['energy_kwh']:<15.1f} {best_balanced['delay_minutes']:<12.1f}")
    print(f"{'Energy Priority':<20} {best_energy['action']:<30} {best_energy['energy_kwh']:<15.1f} {best_energy['delay_minutes']:<12.1f}")
    print(f"{'Time Priority':<20} {best_time['action']:<30} {best_time['energy_kwh']:<15.1f} {best_time['delay_minutes']:<12.1f}")
    print(f"{'Strict Priority':<20} {best_strict['action']:<30} {best_strict['energy_kwh']:<15.1f} {best_strict['delay_minutes']:<12.1f}")


def test_emergency_mode():
    """Test emergency conflict (< 5 minutes)"""
    
    print("\n" + "="*70)
    print("EMERGENCY SCENARIO TEST")
    print("="*70)
    
    sim = RailwaySimulator()
    
    # Trains very close - emergency!
    train_a = sim.add_train(
        "EXP_A", "express_passenger", 50, 110, 192, "forward"
    )
    
    train_b = sim.add_train(
        "EXP_B", "express_passenger", 58, 100, 0, "backward"
    )
    
    # Create artificial emergency conflict
    conflict = {
        'train_a': train_a.train_id,
        'train_b': train_b.train_id,
        'conflict_position_km': 54,
        'time_to_conflict_seconds': 240,  # 4 minutes!
        'time_to_conflict_minutes': 4.0,
        'severity': 'critical',
        'current_time': 0
    }
    
    resolver = ConflictResolver()
    solutions = resolver.analyze_conflict(conflict, train_a, train_b)
    
    print(f"\nGenerated {len(solutions)} solutions (emergency mode)")
    print("Note: Only stop solutions generated for safety")
    
    for i, sol in enumerate(solutions, 1):
        print(f"\n{i}. {sol['action']}")
        print(f"   Type: {sol['type']}")
        print(f"   Energy: {sol['energy_kwh']:.1f} kWh")
        print(f"   Delay: {sol['delay_minutes']:.1f} min")


def test_gradient_awareness():
    """Test gradient-aware decision making"""
    
    print("\n" + "="*70)
    print("GRADIENT-AWARE DECISION MAKING")
    print("="*70)
    
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
    
    # Test WITHOUT gradient awareness
    print("\nWITHOUT Gradient Awareness:")
    print("-" * 70)
    
    resolver_no_gradient = ConflictResolver(config={'consider_gradient': False})
    solutions_no_gradient = resolver_no_gradient.analyze_conflict(conflict, freight, express)
    best_no_gradient = solutions_no_gradient[0]
    print(f"Best: {best_no_gradient['action']}")
    print(f"Energy: {best_no_gradient['energy_kwh']:.1f} kWh")
    
    # Test WITH gradient awareness
    print("\nWITH Gradient Awareness:")
    print("-" * 70)
    
    track_info = {
        f'{freight.train_id}_gradient': 'uphill',  # Freight climbing
        f'{express.train_id}_gradient': 'downhill'  # Express descending
    }
    
    resolver_with_gradient = ConflictResolver(config={'consider_gradient': True})
    solutions_with_gradient = resolver_with_gradient.analyze_conflict(
        conflict, freight, express, track_info=track_info
    )
    best_with_gradient = solutions_with_gradient[0]
    print(f"Best: {best_with_gradient['action']}")
    print(f"Energy: {best_with_gradient['energy_kwh']:.1f} kWh")
    
    # Find the "stop freight" solution to show penalty
    for sol in solutions_with_gradient:
        if sol['train_affected'] == freight.train_id and sol['type'] == 'stop':
            print(f"\nNote: Stopping freight on uphill:")
            print(f"  Base energy: {sol['breakdown']['braking_kwh'] + sol['breakdown']['idle_kwh'] + sol['breakdown']['restart_kwh']:.1f} kWh")
            if 'gradient_penalty_kwh' in sol['breakdown']:
                print(f"  Gradient penalty: +{sol['breakdown']['gradient_penalty_kwh']} kWh")
            print(f"  Total: {sol['energy_kwh']:.1f} kWh")
            break


if __name__ == "__main__":
    test_different_modes()
    test_emergency_mode()
    test_gradient_awareness()
    
    print("\n" + "="*70)
    print("✓ All mode tests completed!")
    print("="*70)
    print("\nKEY INSIGHTS:")
    print("1. Different modes optimize for different goals")
    print("2. Emergency mode prioritizes safety (only stop solutions)")
    print("3. Gradient awareness prevents expensive uphill restarts")
    print("4. System is flexible and configurable for various scenarios")
