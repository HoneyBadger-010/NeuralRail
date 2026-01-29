"""
Railway Simulator - Main simulation engine.
Manages multiple trains, detects conflicts, and tracks system state.
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from simulation.train import Train
from data.railway_network import STATIONS, TRACK_SEGMENTS, TRAIN_TYPES
from physics.energy_calculator import joules_to_kwh


class RailwaySimulator:
    """
    Main simulation engine for the railway network.
    Manages multiple trains and detects conflicts.
    """
    
    def __init__(self):
        self.trains = {}  # Dictionary of train_id -> Train object
        self.current_time = 0  # Simulation time in seconds
        self.time_step = 10  # Simulation step size (10 seconds)
        self.conflicts_detected = []
        self.total_system_energy_joules = 0
        
        # Playback controls for SC interface
        self.playback_speed = 1.0  # 1.0 = real-time, 2.0 = 2x speed, 0.5 = half speed
        self.is_paused = False
        self.simulation_history = []  # Store snapshots for replay
        
    def add_train(self, train_id, train_type_key, initial_position_km, 
                  initial_speed_kmh, destination_km, direction="forward"):
        """
        Add a train to the simulation.
        
        Args:
            train_id: Unique identifier
            train_type_key: Key from TRAIN_TYPES (e.g., 'rajdhani')
            initial_position_km: Starting position
            initial_speed_kmh: Starting speed
            destination_km: Destination
            direction: "forward" or "backward"
        """
        if train_type_key not in TRAIN_TYPES:
            raise ValueError(f"Unknown train type: {train_type_key}")
            
        train_type_data = TRAIN_TYPES[train_type_key]
        train = Train(
            train_id,
            train_type_data,
            initial_position_km,
            initial_speed_kmh,
            destination_km,
            direction
        )
        self.trains[train_id] = train
        print(f"✓ Added {train_id}: {train.name} at {initial_position_km} km")
        return train
        
    def step(self, train_commands=None, save_snapshot=True):
        """
        Execute one simulation step (default 10 seconds).
        
        Args:
            train_commands: Dict of {train_id: {'target_speed': speed, 'elevation_change': meters}}
            save_snapshot: Whether to save this state to history (for replay)
        """
        if self.is_paused:
            return {}
            
        if train_commands is None:
            train_commands = {}
            
        step_results = {}
        
        for train_id, train in self.trains.items():
            # Get commands for this train
            command = train_commands.get(train_id, {})
            target_speed = command.get('target_speed', train.speed_kmh)
            elevation_change = command.get('elevation_change', 0)
            
            # Move the train
            result = train.move(
                self.time_step,
                target_speed_kmh=target_speed,
                elevation_change_m=elevation_change
            )
            
            step_results[train_id] = result
            self.total_system_energy_joules += result['energy_consumed_joules']
            
        self.current_time += self.time_step
        
        # Check for conflicts after movement
        self.detect_conflicts()
        
        # Save snapshot for replay (limit to last 1000 steps to avoid memory issues)
        if save_snapshot:
            snapshot = {
                'time': self.current_time,
                'train_positions': self.get_all_train_positions(),
                'conflicts': self.conflicts_detected.copy(),
                'system_energy': self.total_system_energy_joules
            }
            self.simulation_history.append(snapshot)
            
            # Keep only last 1000 snapshots (about 2.7 hours of simulation)
            if len(self.simulation_history) > 1000:
                self.simulation_history.pop(0)
        
        return step_results
    
    def set_playback_speed(self, speed):
        """
        Set simulation playback speed for SC interface.
        
        Args:
            speed: Multiplier (0.5 = half speed, 1.0 = normal, 2.0 = 2x, 5.0 = 5x, 10.0 = 10x)
        """
        self.playback_speed = max(0.1, min(speed, 20.0))  # Limit between 0.1x and 20x
        print(f"Playback speed set to {self.playback_speed}x")
        
    def pause(self):
        """Pause the simulation"""
        self.is_paused = True
        print("⏸️  Simulation paused")
        
    def resume(self):
        """Resume the simulation"""
        self.is_paused = False
        print("▶️  Simulation resumed")
        
    def get_snapshot_at_time(self, time_seconds):
        """Get simulation state at a specific time (for replay)"""
        for snapshot in self.simulation_history:
            if abs(snapshot['time'] - time_seconds) < self.time_step:
                return snapshot
        return None
        
    def clear_history(self):
        """Clear simulation history to free memory"""
        self.simulation_history = []
        print("✓ Simulation history cleared")
        
    def detect_conflicts(self, lookahead_seconds=1800):
        """
        Detect potential conflicts (collisions) between trains.
        
        Args:
            lookahead_seconds: How far ahead to predict (default 30 minutes)
            
        Returns:
            List of conflicts detected
        """
        conflicts = []
        train_list = list(self.trains.values())
        
        # Check each pair of trains
        for i, train_a in enumerate(train_list):
            for train_b in train_list[i+1:]:
                # Only check if trains are moving in same direction or towards each other
                conflict = self._check_train_pair_conflict(
                    train_a, train_b, lookahead_seconds
                )
                if conflict:
                    conflicts.append(conflict)
                    
        self.conflicts_detected = conflicts
        return conflicts
        
    def _check_train_pair_conflict(self, train_a, train_b, lookahead_seconds):
        """Check if two trains will conflict"""
        # Predict future positions
        time_steps = lookahead_seconds // self.time_step
        
        pos_a = train_a.position_km
        pos_b = train_b.position_km
        speed_a = train_a.speed_kmh
        speed_b = train_b.speed_kmh
        
        # Simple linear prediction
        for step in range(time_steps):
            time_delta_hours = (step * self.time_step) / 3600
            
            if train_a.direction == "forward":
                future_pos_a = pos_a + (speed_a * time_delta_hours)
            else:
                future_pos_a = pos_a - (speed_a * time_delta_hours)
                
            if train_b.direction == "forward":
                future_pos_b = pos_b + (speed_b * time_delta_hours)
            else:
                future_pos_b = pos_b - (speed_b * time_delta_hours)
                
            # Check if trains are within 8 km of each other (conflict zone)
            # Increased from 2km to 8km for earlier detection and longer demo time
            distance_between = abs(future_pos_a - future_pos_b)
            
            if distance_between < 8.0:  # Within 8 km = potential conflict (early warning)
                time_to_conflict = step * self.time_step
                return {
                    'train_a': train_a.train_id,
                    'train_b': train_b.train_id,
                    'conflict_position_km': (future_pos_a + future_pos_b) / 2,
                    'time_to_conflict_seconds': time_to_conflict,
                    'time_to_conflict_minutes': round(time_to_conflict / 60, 1),
                    'severity': 'critical' if time_to_conflict < 600 else 'warning',
                    'current_time': self.current_time
                }
                
        return None
        
    def get_system_status(self):
        """Get overall system status"""
        total_trains = len(self.trains)
        moving_trains = sum(1 for t in self.trains.values() if t.state == "moving")
        stopped_trains = sum(1 for t in self.trains.values() if t.state == "stopped")
        
        return {
            'simulation_time_min': round(self.current_time / 60, 1),
            'total_trains': total_trains,
            'moving_trains': moving_trains,
            'stopped_trains': stopped_trains,
            'conflicts_detected': len(self.conflicts_detected),
            'total_system_energy_kwh': round(joules_to_kwh(self.total_system_energy_joules), 2)
        }
        
    def get_all_train_positions(self):
        """Get positions of all trains for visualization"""
        return {
            train_id: {
                'position_km': train.position_km,
                'speed_kmh': train.speed_kmh,
                'state': train.state,
                'name': train.name
            }
            for train_id, train in self.trains.items()
        }
        
    def print_status(self):
        """Print current simulation status"""
        print(f"\n{'='*70}")
        print(f"SIMULATION TIME: {self.current_time / 60:.1f} minutes")
        print(f"{'='*70}")
        
        for train_id, train in self.trains.items():
            status = train.get_status()
            print(f"{train_id:20s} | Pos: {status['position_km']:6.1f} km | "
                  f"Speed: {status['speed_kmh']:5.1f} km/h | "
                  f"State: {status['state']:12s} | "
                  f"Energy: {status['total_energy_kwh']:6.1f} kWh")
                  
        if self.conflicts_detected:
            print(f"\n⚠️  CONFLICTS DETECTED: {len(self.conflicts_detected)}")
            for conflict in self.conflicts_detected:
                print(f"   {conflict['train_a']} ↔ {conflict['train_b']}: "
                      f"Conflict in {conflict['time_to_conflict_minutes']} min "
                      f"at {conflict['conflict_position_km']:.1f} km "
                      f"[{conflict['severity'].upper()}]")
        else:
            print(f"\n✓ No conflicts detected")
            
        system_status = self.get_system_status()
        print(f"\nTotal System Energy: {system_status['total_system_energy_kwh']} kWh")
