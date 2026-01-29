"""
Test the railway simulator with a conflict scenario.
This demonstrates how the simulation engine works.
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from railway_simulator import RailwaySimulator
from data.railway_network import STATIONS


def test_basic_movement():
    """Test basic train movement"""
    print("\n" + "="*70)
    print("TEST 1: BASIC TRAIN MOVEMENT")
    print("="*70)
    
    sim = RailwaySimulator()
    
    # Add a Rajdhani train
    sim.add_train(
        train_id="12001_Rajdhani",
        train_type_key="rajdhani",
        initial_position_km=0,  # Starting at New Delhi
        initial_speed_kmh=80,
        destination_km=141,  # Going to Mathura Junction
        direction="forward"
    )
    
    print("\nSimulating 5 minutes of travel...")
    
    # Simulate 30 steps (5 minutes)
    for i in range(30):
        sim.step()
        
    sim.print_status()
    
    train = sim.trains["12001_Rajdhani"]
    energy_report = train.get_energy_report()
    print(f"\nEnergy Report:")
    print(f"  Total: {energy_report['total_energy_kwh']} kWh")
    print(f"  Per km: {energy_report['energy_per_km']} kWh/km")


def test_conflict_detection():
    """Test conflict detection between two trains"""
    print("\n" + "="*70)
    print("TEST 2: CONFLICT DETECTION")
    print("="*70)
    print("Scenario: Two trains approaching each other on same track")
    
    sim = RailwaySimulator()
    
    # Train A: Rajdhani going forward (Delhi → Agra)
    sim.add_train(
        train_id="12001_Rajdhani",
        train_type_key="rajdhani",
        initial_position_km=10,
        initial_speed_kmh=120,
        destination_km=192,
        direction="forward"
    )
    
    # Train B: Freight going backward (Agra → Delhi) on same track
    sim.add_train(
        train_id="FREIGHT_001",
        train_type_key="freight_heavy",
        initial_position_km=80,
        initial_speed_kmh=60,
        destination_km=0,
        direction="backward"
    )
    
    print("\nInitial positions:")
    sim.print_status()
    
    print("\nSimulating 10 minutes...")
    
    # Simulate until conflict is detected
    for i in range(60):  # 10 minutes
        sim.step()
        
        if sim.conflicts_detected:
            print(f"\n⚠️  CONFLICT DETECTED at simulation time {sim.current_time/60:.1f} min!")
            break
            
    sim.print_status()


def test_ghat_scenario():
    """Test the famous ghat scenario - heavy freight climbing"""
    print("\n" + "="*70)
    print("TEST 3: GRADIENT SCENARIO - Heavy Freight Climbing")
    print("="*70)
    print("Scenario: 4200-ton freight climbing steep gradient")
    
    sim = RailwaySimulator()
    
    # Freight train starting climb from Ghaziabad
    freight = sim.add_train(
        train_id="FREIGHT_COAL",
        train_type_key="freight_heavy",
        initial_position_km=30,  # Ghaziabad
        initial_speed_kmh=50,
        destination_km=100,  # Tundla Junction
        direction="forward"
    )
    
    print(f"\nFreight mass: {freight.mass_kg / 1000} tons")
    print(f"Elevation to climb: 583 meters over 52 km")
    print(f"Average gradient: 1.12%")
    
    print("\nSimulating climb (will take ~60 minutes at 50 km/h)...")
    
    # Simulate the climb with elevation changes
    # Divide the 583m elevation gain across the journey
    elevation_per_step = 583 / 312  # 312 steps = 52 minutes at 10-second steps
    
    for i in range(312):  # Simulate ~52 minutes
        # Apply elevation change for this step
        commands = {
            "FREIGHT_COAL": {
                'target_speed': 50,  # Maintain 50 km/h
                'elevation_change': elevation_per_step
            }
        }
        sim.step(train_commands=commands)
        
        # Print progress every 10 minutes
        if i % 60 == 0:
            print(f"  Time: {sim.current_time/60:.0f} min | "
                  f"Position: {freight.position_km:.1f} km | "
                  f"Energy: {freight.get_energy_report()['total_energy_kwh']:.0f} kWh")
    
    sim.print_status()
    
    energy_report = freight.get_energy_report()
    print(f"\n{'='*70}")
    print(f"GHAT CLIMB ENERGY ANALYSIS")
    print(f"{'='*70}")
    print(f"Total Energy Consumed: {energy_report['total_energy_kwh']} kWh")
    print(f"Energy Breakdown:")
    for category, kwh in energy_report['breakdown_kwh'].items():
        if kwh > 0:
            print(f"  - {category.replace('_', ' ').title()}: {kwh} kWh")
    print(f"\nThis is equivalent to powering {energy_report['total_energy_kwh'] / 5:.0f} homes for 1 hour!")


def test_emergency_stop_comparison():
    """Compare energy cost of stopping different trains"""
    print("\n" + "="*70)
    print("TEST 4: EMERGENCY STOP ENERGY COMPARISON")
    print("="*70)
    
    # Test Rajdhani
    sim1 = RailwaySimulator()
    rajdhani = sim1.add_train(
        "12001_Rajdhani", "rajdhani", 50, 130, 192, "forward"
    )
    
    print(f"\nRajdhani at 130 km/h:")
    print(f"  Mass: {rajdhani.mass_kg / 1000} tons")
    initial_energy = rajdhani.total_energy_consumed_joules
    rajdhani.emergency_stop()
    stop_energy_rajdhani = rajdhani.total_energy_consumed_joules - initial_energy
    print(f"  Emergency stop energy: {stop_energy_rajdhani / 3_600_000:.1f} kWh")
    
    # Test Freight
    sim2 = RailwaySimulator()
    freight = sim2.add_train(
        "FREIGHT_001", "freight_heavy", 50, 60, 192, "forward"
    )
    
    print(f"\nFreight at 60 km/h:")
    print(f"  Mass: {freight.mass_kg / 1000} tons")
    initial_energy = freight.total_energy_consumed_joules
    freight.emergency_stop()
    stop_energy_freight = freight.total_energy_consumed_joules - initial_energy
    print(f"  Emergency stop energy: {stop_energy_freight / 3_600_000:.1f} kWh")
    
    # Test Vande Bharat
    sim3 = RailwaySimulator()
    vb = sim3.add_train(
        "VB_001", "vande_bharat", 50, 160, 192, "forward"
    )
    
    print(f"\nVande Bharat at 160 km/h:")
    print(f"  Mass: {vb.mass_kg / 1000} tons")
    initial_energy = vb.total_energy_consumed_joules
    vb.emergency_stop()
    stop_energy_vb = vb.total_energy_consumed_joules - initial_energy
    print(f"  Emergency stop energy: {stop_energy_vb / 3_600_000:.1f} kWh")
    
    print(f"\n{'='*70}")
    print("KEY INSIGHT: Even though freight is slower, it's much heavier!")
    print(f"Freight/Rajdhani ratio: {(freight.mass_kg / rajdhani.mass_kg):.1f}x heavier")


if __name__ == "__main__":
    test_basic_movement()
    test_conflict_detection()
    test_ghat_scenario()
    test_emergency_stop_comparison()
    
    print("\n" + "="*70)
    print("✓ All simulation tests completed!")
    print("="*70)
