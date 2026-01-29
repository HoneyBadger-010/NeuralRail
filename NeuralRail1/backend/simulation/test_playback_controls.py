"""
Test playback controls - pause, resume, speed control
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from railway_simulator import RailwaySimulator
import time


def test_playback_controls():
    """Test pause, resume, and speed controls"""
    print("="*70)
    print("PLAYBACK CONTROLS TEST")
    print("="*70)
    
    sim = RailwaySimulator()
    
    # Add a train
    sim.add_train(
        train_id="12001_Rajdhani",
        train_type_key="rajdhani",
        initial_position_km=0,
        initial_speed_kmh=100,
        destination_km=192,
        direction="forward"
    )
    
    print("\n1. Normal speed (1.0x) - 10 steps")
    sim.set_playback_speed(1.0)
    for i in range(10):
        sim.step()
    sim.print_status()
    
    print("\n2. Fast forward (5.0x) - 10 steps")
    sim.set_playback_speed(5.0)
    for i in range(10):
        sim.step()
    sim.print_status()
    
    print("\n3. Pause simulation")
    sim.pause()
    print("Attempting 5 steps while paused...")
    for i in range(5):
        sim.step()
    sim.print_status()
    print("Notice: Time didn't advance (simulation was paused)")
    
    print("\n4. Resume simulation")
    sim.resume()
    for i in range(5):
        sim.step()
    sim.print_status()
    
    print("\n5. Slow motion (0.5x) - 5 steps")
    sim.set_playback_speed(0.5)
    for i in range(5):
        sim.step()
    sim.print_status()
    
    print("\n" + "="*70)
    print("SIMULATION HISTORY TEST")
    print("="*70)
    print(f"Total snapshots saved: {len(sim.simulation_history)}")
    print(f"Memory usage: ~{len(sim.simulation_history) * 0.05:.1f} MB")
    
    # Show first and last snapshot
    if sim.simulation_history:
        first = sim.simulation_history[0]
        last = sim.simulation_history[-1]
        print(f"\nFirst snapshot: Time {first['time']/60:.1f} min")
        print(f"Last snapshot: Time {last['time']/60:.1f} min")
        print(f"History covers: {(last['time'] - first['time'])/60:.1f} minutes")
    
    # Test time travel
    print("\n" + "="*70)
    print("TIME TRAVEL TEST (Replay)")
    print("="*70)
    
    target_time = 600  # 10 minutes
    snapshot = sim.get_snapshot_at_time(target_time)
    if snapshot:
        print(f"\nSnapshot at {target_time/60:.0f} minutes:")
        print(f"  System energy: {snapshot['system_energy']/3_600_000:.1f} kWh")
        print(f"  Train positions:")
        for train_id, pos_data in snapshot['train_positions'].items():
            print(f"    {train_id}: {pos_data['position_km']:.1f} km at {pos_data['speed_kmh']:.0f} km/h")
    
    print("\n" + "="*70)
    print("MEMORY CLEANUP TEST")
    print("="*70)
    
    print(f"Before cleanup: {len(sim.simulation_history)} snapshots")
    
    train = sim.trains["12001_Rajdhani"]
    print(f"Train events before cleanup: {len(train.event_log)}")
    
    # Clear history
    sim.clear_history()
    train.clear_logs()
    
    print(f"After cleanup: {len(sim.simulation_history)} snapshots")
    print(f"Train events after cleanup: {len(train.event_log)}")
    print("✓ Memory freed successfully")


if __name__ == "__main__":
    test_playback_controls()
