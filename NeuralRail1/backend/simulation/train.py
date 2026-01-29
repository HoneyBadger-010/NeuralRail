"""
Train class - Represents a single train in the simulation.
Each train has position, speed, destination, and tracks its energy consumption.
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from physics.energy_calculator import EnergyCalculator, joules_to_kwh


class Train:
    """
    Represents a single train in the railway network.
    Tracks position, speed, energy consumption, and state.
    """
    
    def __init__(self, train_id, train_type_data, initial_position_km, 
                 initial_speed_kmh, destination_km, direction="forward"):
        """
        Initialize a train.
        
        Args:
            train_id: Unique identifier (e.g., "12001_Rajdhani")
            train_type_data: Dictionary with train specifications from TRAIN_TYPES
            initial_position_km: Starting position in km
            initial_speed_kmh: Starting speed
            destination_km: Target position
            direction: "forward" or "backward"
        """
        self.train_id = train_id
        self.name = train_type_data['name']
        self.train_type = train_type_data
        
        # Physical characteristics
        self.mass_kg = train_type_data['mass_kg']
        self.max_speed_kmh = train_type_data['max_speed_kmh']
        self.acceleration_mps2 = train_type_data['acceleration_mps2']
        self.braking_rate_mps2 = train_type_data['braking_rate_mps2']
        self.idle_power_kw = train_type_data['idle_power_kw']
        self.priority = train_type_data['priority']
        self.schedule_importance = train_type_data['schedule_importance']
        
        # Current state
        self.position_km = initial_position_km
        self.speed_kmh = initial_speed_kmh
        self.destination_km = destination_km
        self.direction = direction
        
        # State tracking
        self.state = "moving"  # moving, stopped, idle, braking, accelerating
        self.total_energy_consumed_joules = 0
        self.total_distance_traveled_km = 0
        self.total_time_seconds = 0
        self.idle_time_seconds = 0
        self.delay_seconds = 0
        
        # Energy breakdown
        self.energy_breakdown = {
            'kinetic': 0,
            'braking_loss': 0,
            'acceleration': 0,
            'idle': 0,
            'uphill': 0,
            'downhill_recovery': 0
        }
        
        # Event log for AI explanation (limited to last 100 events)
        self.event_log = []
        self.max_log_size = 100  # Keep only last 100 events to avoid memory issues
        
    def log_event(self, event_type, description, energy_joules=0):
        """
        Log an event for later AI analysis.
        Automatically keeps only the last 100 events to prevent memory overflow.
        """
        event = {
            'time': self.total_time_seconds,
            'position_km': self.position_km,
            'speed_kmh': self.speed_kmh,
            'event_type': event_type,
            'description': description,
            'energy_joules': energy_joules,
            'energy_kwh': joules_to_kwh(energy_joules)
        }
        self.event_log.append(event)
        
        # Keep only last 100 events to avoid memory issues
        if len(self.event_log) > self.max_log_size:
            self.event_log.pop(0)  # Remove oldest event
    
    def clear_logs(self):
        """Clear event logs to free memory"""
        self.event_log = []
        
    def get_recent_events(self, count=10):
        """Get the most recent N events"""
        return self.event_log[-count:]
        
    def move(self, time_step_seconds, target_speed_kmh=None, elevation_change_m=0):
        """
        Simulate train movement for a time step.
        
        Args:
            time_step_seconds: Duration of this simulation step
            target_speed_kmh: Desired speed (None = maintain current)
            elevation_change_m: Elevation change during this step (+ = uphill)
            
        Returns:
            dict with movement results and energy consumed
        """
        if target_speed_kmh is None:
            target_speed_kmh = self.speed_kmh
            
        initial_speed = self.speed_kmh
        distance_traveled_km = 0
        energy_consumed = 0
        
        # Case 1: Train is stopped (idle)
        if self.state == "stopped" or self.state == "idle":
            idle_energy = EnergyCalculator.idle_energy(
                self.idle_power_kw, 
                time_step_seconds
            )
            energy_consumed += idle_energy
            self.energy_breakdown['idle'] += idle_energy
            self.idle_time_seconds += time_step_seconds
            self.log_event("idle", f"Train idle at {self.position_km:.1f} km", idle_energy)
            
        # Case 2: Braking
        elif target_speed_kmh < self.speed_kmh:
            self.state = "braking"
            
            # Calculate how much we can brake in this time step
            max_speed_reduction = self.braking_rate_mps2 * time_step_seconds * 3.6  # Convert to km/h
            actual_final_speed = max(target_speed_kmh, self.speed_kmh - max_speed_reduction)
            
            # Energy lost in braking
            braking_energy = EnergyCalculator.braking_energy_loss(
                self.mass_kg,
                self.speed_kmh,
                actual_final_speed
            )
            
            # Regenerative braking recovery (30% for electric trains)
            if self.train_type['traction_type'] == 'electric':
                recovery = braking_energy * 0.30
                self.energy_breakdown['downhill_recovery'] += recovery
                braking_energy -= recovery
            
            energy_consumed += braking_energy
            self.energy_breakdown['braking_loss'] += braking_energy
            
            # Distance traveled during braking
            avg_speed_kmh = (self.speed_kmh + actual_final_speed) / 2
            distance_traveled_km = (avg_speed_kmh * time_step_seconds) / 3600
            
            self.speed_kmh = actual_final_speed
            self.log_event("braking", 
                          f"Braked from {initial_speed:.1f} to {actual_final_speed:.1f} km/h",
                          braking_energy)
            
            if self.speed_kmh == 0:
                self.state = "stopped"
                
        # Case 3: Accelerating
        elif target_speed_kmh > self.speed_kmh:
            self.state = "accelerating"
            
            # Calculate how much we can accelerate in this time step
            max_speed_increase = self.acceleration_mps2 * time_step_seconds * 3.6
            actual_final_speed = min(target_speed_kmh, self.speed_kmh + max_speed_increase)
            actual_final_speed = min(actual_final_speed, self.max_speed_kmh)  # Don't exceed max
            
            # Distance traveled during acceleration
            avg_speed_kmh = (self.speed_kmh + actual_final_speed) / 2
            distance_traveled_km = (avg_speed_kmh * time_step_seconds) / 3600
            
            # Energy for acceleration
            accel_energy = EnergyCalculator.acceleration_energy(
                self.mass_kg,
                self.speed_kmh,
                actual_final_speed,
                distance_traveled_km
            )
            energy_consumed += accel_energy
            self.energy_breakdown['acceleration'] += accel_energy
            
            self.speed_kmh = actual_final_speed
            self.log_event("acceleration",
                          f"Accelerated from {initial_speed:.1f} to {actual_final_speed:.1f} km/h",
                          accel_energy)
            
        # Case 4: Constant speed (cruising)
        else:
            self.state = "moving"
            distance_traveled_km = (self.speed_kmh * time_step_seconds) / 3600
            
            # Energy for maintaining speed (rolling resistance)
            # Simplified: small constant power
            cruising_energy = self.mass_kg * 9.81 * 0.002 * distance_traveled_km * 1000
            energy_consumed += cruising_energy
            self.energy_breakdown['kinetic'] += cruising_energy
            
        # Handle elevation changes (uphill/downhill)
        if elevation_change_m != 0 and distance_traveled_km > 0:
            if elevation_change_m > 0:  # Uphill
                uphill_energy = EnergyCalculator.uphill_energy(
                    self.mass_kg,
                    elevation_change_m,
                    distance_traveled_km,
                    self.speed_kmh
                )
                energy_consumed += uphill_energy
                self.energy_breakdown['uphill'] += uphill_energy
                self.log_event("uphill", 
                              f"Climbed {elevation_change_m:.0f}m elevation",
                              uphill_energy)
            else:  # Downhill
                downhill_recovery = EnergyCalculator.downhill_energy_recovery(
                    self.mass_kg,
                    abs(elevation_change_m),
                    has_regenerative=(self.train_type['traction_type'] == 'electric')
                )
                energy_consumed += downhill_recovery  # Negative value
                self.energy_breakdown['downhill_recovery'] += abs(downhill_recovery)
                
        # Update position
        if self.direction == "forward":
            self.position_km += distance_traveled_km
        else:
            self.position_km -= distance_traveled_km
        
        # Clamp position to valid route bounds (0 to 192 km)
        # This prevents trains from "flying off" the track at high playback speeds
        if self.position_km < 0:
            self.position_km = 0
            self.speed_kmh = 0
            self.state = "stopped"
        elif self.position_km > 192:
            self.position_km = 192
            self.speed_kmh = 0
            self.state = "stopped"
        
        # Stop train when it reaches destination
        if self.direction == "forward" and self.position_km >= self.destination_km:
            self.position_km = self.destination_km
            self.speed_kmh = 0
            self.state = "stopped"
        elif self.direction == "backward" and self.position_km <= self.destination_km:
            self.position_km = self.destination_km
            self.speed_kmh = 0
            self.state = "stopped"
            
        # Update totals
        self.total_distance_traveled_km += distance_traveled_km
        self.total_energy_consumed_joules += energy_consumed
        self.total_time_seconds += time_step_seconds
        
        return {
            'distance_traveled_km': distance_traveled_km,
            'energy_consumed_joules': energy_consumed,
            'energy_consumed_kwh': joules_to_kwh(energy_consumed),
            'final_speed_kmh': self.speed_kmh,
            'final_position_km': self.position_km,
            'state': self.state
        }
        
    def emergency_stop(self):
        """Execute emergency braking"""
        self.log_event("emergency_stop", "Emergency stop initiated!", 0)
        braking_time = EnergyCalculator.calculate_braking_time(
            self.speed_kmh,
            self.braking_rate_mps2
        )
        # Simulate emergency braking
        result = self.move(braking_time, target_speed_kmh=0)
        self.state = "stopped"
        return result
        
    def get_status(self):
        """Get current train status"""
        return {
            'train_id': self.train_id,
            'name': self.name,
            'position_km': round(self.position_km, 2),
            'speed_kmh': round(self.speed_kmh, 1),
            'state': self.state,
            'destination_km': self.destination_km,
            'priority': self.priority,
            'total_energy_kwh': round(joules_to_kwh(self.total_energy_consumed_joules), 2),
            'distance_traveled_km': round(self.total_distance_traveled_km, 2),
            'time_elapsed_min': round(self.total_time_seconds / 60, 1),
            'delay_min': round(self.delay_seconds / 60, 1)
        }
        
    def get_energy_report(self):
        """Get detailed energy consumption breakdown"""
        total_kwh = joules_to_kwh(self.total_energy_consumed_joules)
        
        breakdown_kwh = {
            key: round(joules_to_kwh(value), 2) 
            for key, value in self.energy_breakdown.items()
        }
        
        return {
            'train_id': self.train_id,
            'total_energy_kwh': round(total_kwh, 2),
            'breakdown_kwh': breakdown_kwh,
            'energy_per_km': round(total_kwh / max(self.total_distance_traveled_km, 0.1), 2),
            'idle_time_min': round(self.idle_time_seconds / 60, 1),
            'current_power_kw': self.get_current_power_kw()
        }
    
    def get_current_power_kw(self):
        """
        Calculate current instantaneous power consumption in kW.
        
        Formula based on train physics:
        - Rolling resistance: P_roll = m * g * Crr * v (where Crr ≈ 0.002 for trains)
        - Air resistance: P_air = 0.5 * Cd * A * ρ * v³ (simplified)
        - Gradient resistance: P_grade = m * g * sin(θ) * v
        
        For simplicity, we use an empirical formula:
        P (kW) = (mass_tons * speed_kmh * 0.05) + (speed_kmh² * 0.01) + idle_power
        
        This gives realistic values:
        - Rajdhani (850t) at 110 km/h: ~4700 + 121 + 50 = ~4871 kW
        - Freight (4200t) at 50 km/h: ~10500 + 25 + 30 = ~10555 kW
        """
        if self.state == "stopped" or self.state == "idle":
            return round(self.idle_power_kw, 1)
        
        mass_tons = self.mass_kg / 1000
        speed = self.speed_kmh
        
        # Rolling resistance component (proportional to mass and speed)
        rolling_power = mass_tons * speed * 0.05
        
        # Air resistance component (proportional to speed squared)
        air_power = (speed ** 2) * 0.01
        
        # Add idle power (auxiliary systems)
        total_power = rolling_power + air_power + self.idle_power_kw
        
        # If accelerating, add extra power for acceleration
        if self.state == "accelerating":
            # P = F * v = m * a * v
            accel_power = (self.mass_kg * self.acceleration_mps2 * (speed / 3.6)) / 1000
            total_power += accel_power
        
        return round(total_power, 1)
        
    def __repr__(self):
        return (f"Train({self.train_id}, pos={self.position_km:.1f}km, "
                f"speed={self.speed_kmh:.0f}km/h, state={self.state})")
