"""
Test script to demonstrate energy calculations with real examples.
Run this to see how much energy different operations consume.
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from physics.energy_calculator import EnergyCalculator, joules_to_kwh, compare_energy_scenarios
from data.railway_network import TRAIN_TYPES

def test_basic_calculations():
    """Test basic energy calculations with a Rajdhani Express"""
    
    print("=" * 70)
    print("ENERGY CALCULATION DEMONSTRATIONS")
    print("=" * 70)
    
    # Get Rajdhani train data
    rajdhani = TRAIN_TYPES['rajdhani']
    mass = rajdhani['mass_kg']
    
    print(f"\nTrain: {rajdhani['name']}")
    print(f"Mass: {mass / 1000} tons")
    print("-" * 70)
    
    # Test 1: Kinetic Energy at different speeds
    print("\n1. KINETIC ENERGY AT DIFFERENT SPEEDS:")
    for speed in [60, 100, 130]:
        ke = EnergyCalculator.kinetic_energy(mass, speed)
        ke_kwh = joules_to_kwh(ke)
        print(f"   At {speed} km/h: {ke:,.0f} J = {ke_kwh:.2f} kWh")
    
    # Test 2: Emergency Braking Energy Loss
    print("\n2. EMERGENCY BRAKING (130 km/h → 0 km/h):")
    braking_loss = EnergyCalculator.braking_energy_loss(mass, 130, 0)
    braking_loss_kwh = joules_to_kwh(braking_loss)
    print(f"   Energy wasted: {braking_loss:,.0f} J = {braking_loss_kwh:.2f} kWh")
    print(f"   (This could power a home for {braking_loss_kwh / 5:.1f} hours!)")
    
    # With regenerative braking recovery
    recovery = braking_loss * 0.30
    recovery_kwh = joules_to_kwh(recovery)
    print(f"   Energy recovered (30%): {recovery:,.0f} J = {recovery_kwh:.2f} kWh")
    print(f"   Net loss: {braking_loss - recovery:,.0f} J = {braking_loss_kwh - recovery_kwh:.2f} kWh")
    
    # Test 3: Braking distance and time
    print("\n3. BRAKING DISTANCE & TIME (130 km/h):")
    braking_dist = EnergyCalculator.calculate_braking_distance(130, rajdhani['braking_rate_mps2'])
    braking_time = EnergyCalculator.calculate_braking_time(130, rajdhani['braking_rate_mps2'])
    print(f"   Distance needed: {braking_dist:.0f} meters")
    print(f"   Time needed: {braking_time:.1f} seconds")
    
    # Test 4: Idle Energy Waste
    print("\n4. IDLE ENERGY WASTE (waiting at station):")
    for minutes in [5, 10, 30]:
        idle_energy = EnergyCalculator.idle_energy(rajdhani['idle_power_kw'], minutes * 60)
        idle_kwh = joules_to_kwh(idle_energy)
        print(f"   {minutes} minutes idle: {idle_energy:,.0f} J = {idle_kwh:.2f} kWh")
    
    # Test 5: Uphill Energy (Delhi to Aligarh section)
    print("\n5. CLIMBING GRADIENT (Delhi → Aligarh):")
    print("   Distance: 52 km, Elevation gain: 583 meters")
    uphill_energy = EnergyCalculator.uphill_energy(mass, 583, 52, 50)
    uphill_kwh = joules_to_kwh(uphill_energy)
    print(f"   Extra energy needed: {uphill_energy:,.0f} J = {uphill_kwh:.2f} kWh")
    print(f"   (This is {uphill_kwh / braking_loss_kwh:.1f}x the energy of emergency braking!)")


def test_scenario_comparison():
    """Compare two conflict resolution scenarios"""
    
    print("\n" + "=" * 70)
    print("SCENARIO COMPARISON: Which decision saves more energy?")
    print("=" * 70)
    
    print("\nSITUATION: Two trains approaching same track section")
    print("- Train A: Rajdhani (600 tons) at 110 km/h")
    print("- Train B: Freight (800 tons) at 50 km/h")
    print("\nOPTIONS:")
    print("  Option 1: Stop Rajdhani")
    print("  Option 2: Stop Freight")
    
    rajdhani_mass = TRAIN_TYPES['rajdhani']['mass_kg']
    freight_mass = TRAIN_TYPES['freight_heavy']['mass_kg']
    
    # Scenario A: Stop Rajdhani
    rajdhani_brake = EnergyCalculator.braking_energy_loss(rajdhani_mass, 110, 0)
    rajdhani_restart = EnergyCalculator.acceleration_energy(rajdhani_mass, 0, 110, 2)
    scenario_a_energy = rajdhani_brake + rajdhani_restart
    
    # Scenario B: Stop Freight
    freight_brake = EnergyCalculator.braking_energy_loss(freight_mass, 50, 0)
    freight_restart = EnergyCalculator.acceleration_energy(freight_mass, 0, 50, 2)
    scenario_b_energy = freight_brake + freight_restart
    
    scenario_a = {
        'name': 'Stop Rajdhani',
        'energy_joules': scenario_a_energy
    }
    
    scenario_b = {
        'name': 'Stop Freight',
        'energy_joules': scenario_b_energy
    }
    
    comparison = compare_energy_scenarios(scenario_a, scenario_b)
    
    print(f"\nRESULTS:")
    print(f"  Option 1 energy: {joules_to_kwh(scenario_a_energy):.2f} kWh")
    print(f"  Option 2 energy: {joules_to_kwh(scenario_b_energy):.2f} kWh")
    print(f"\n  ✓ BETTER CHOICE: {comparison['better_scenario']}")
    print(f"  Energy saved: {comparison['energy_saved_kwh']:.2f} kWh ({comparison['savings_percent']:.1f}% savings)")
    print(f"\n  BUT WAIT! Rajdhani has priority=1, Freight has priority=5")
    print(f"  → Final decision: Stop Freight (respects priority + saves energy)")


def test_ghat_scenario():
    """Test the special ghat scenario with heavy freight"""
    
    print("\n" + "=" * 70)
    print("GHAT SCENARIO: Why stopping uphill trains is expensive")
    print("=" * 70)
    
    print("\nSITUATION: Freight train climbing gradient at 40 km/h")
    print("Already climbed 300m elevation, has momentum")
    
    freight_mass = TRAIN_TYPES['freight_heavy']['mass_kg']
    
    # Option 1: Stop the freight on the slope
    print("\nOPTION 1: Stop the freight train")
    brake_energy = EnergyCalculator.braking_energy_loss(freight_mass, 40, 0)
    # Restarting on slope requires overcoming gravity + acceleration
    restart_energy = EnergyCalculator.acceleration_energy(freight_mass, 0, 40, 1)
    # Extra energy to overcome slope from standstill
    slope_penalty = EnergyCalculator.uphill_energy(freight_mass, 50, 1, 40)
    
    total_stop_energy = brake_energy + restart_energy + slope_penalty
    
    print(f"  - Braking loss: {joules_to_kwh(brake_energy):.2f} kWh")
    print(f"  - Restart energy: {joules_to_kwh(restart_energy):.2f} kWh")
    print(f"  - Slope penalty: {joules_to_kwh(slope_penalty):.2f} kWh")
    print(f"  TOTAL: {joules_to_kwh(total_stop_energy):.2f} kWh")
    
    # Option 2: Let it continue, stop the other train
    print("\nOPTION 2: Let freight continue, stop passenger train on flat section")
    passenger_mass = TRAIN_TYPES['express_passenger']['mass_kg']
    passenger_brake = EnergyCalculator.braking_energy_loss(passenger_mass, 80, 0)
    passenger_restart = EnergyCalculator.acceleration_energy(passenger_mass, 0, 80, 2)
    total_passenger_energy = passenger_brake + passenger_restart
    
    print(f"  - Passenger brake: {joules_to_kwh(passenger_brake):.2f} kWh")
    print(f"  - Passenger restart: {joules_to_kwh(passenger_restart):.2f} kWh")
    print(f"  TOTAL: {joules_to_kwh(total_passenger_energy):.2f} kWh")
    
    savings = total_stop_energy - total_passenger_energy
    print(f"\n  ✓ ENERGY SAVED: {joules_to_kwh(savings):.2f} kWh")
    print(f"  ({(savings / total_stop_energy) * 100:.1f}% reduction)")
    print(f"\n  AI Explanation: 'Stopping 800-ton freight on steep gradient")
    print(f"  requires {joules_to_kwh(slope_penalty):.0f} kWh extra to restart.")
    print(f"  Stopping lighter passenger train on flat section saves")
    print(f"  {joules_to_kwh(savings):.0f} kWh while maintaining safety.'")


if __name__ == "__main__":
    test_basic_calculations()
    test_scenario_comparison()
    test_ghat_scenario()
    
    print("\n" + "=" * 70)
    print("These calculations will power our AI decision-making system!")
    print("=" * 70)
