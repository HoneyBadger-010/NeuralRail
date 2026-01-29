"""
Energy Calculation Engine for Train Operations
This module calculates energy consumption for different train operations.

All energy values are in Joules (J) and can be converted to kWh for display.
1 kWh = 3,600,000 Joules
"""

import math

# Physical constants
GRAVITY = 9.81  # m/s^2
AIR_DENSITY = 1.225  # kg/m^3
DRAG_COEFFICIENT = 0.6  # Typical for trains
FRONTAL_AREA = 10  # m^2 (approximate for Indian trains)
ROLLING_RESISTANCE_COEFF = 0.002  # Dimensionless


def kmh_to_mps(speed_kmh):
    """Convert km/h to m/s"""
    return speed_kmh / 3.6


def mps_to_kmh(speed_mps):
    """Convert m/s to km/h"""
    return speed_mps * 3.6


def joules_to_kwh(joules):
    """Convert Joules to kilowatt-hours for easier reading"""
    return joules / 3_600_000


class EnergyCalculator:
    """
    Calculates energy consumption for various train operations.
    This is the core of our energy-saving system.
    """

    @staticmethod
    def kinetic_energy(mass_kg, speed_kmh):
        """
        Calculate kinetic energy of a moving train.
        
        Formula: KE = 0.5 × m × v²
        
        Args:
            mass_kg: Mass of train in kilograms
            speed_kmh: Speed in km/h
            
        Returns:
            Energy in Joules
        """
        speed_mps = kmh_to_mps(speed_kmh)
        return 0.5 * mass_kg * (speed_mps ** 2)

    @staticmethod
    def braking_energy_loss(mass_kg, initial_speed_kmh, final_speed_kmh):
        """
        Calculate energy lost when braking.
        This energy is wasted as heat (unless regenerative braking).
        
        Formula: E_loss = KE_initial - KE_final
        
        For electric trains with regenerative braking, we can recover ~30% of this.
        
        Args:
            mass_kg: Mass of train
            initial_speed_kmh: Speed before braking
            final_speed_kmh: Speed after braking (0 for complete stop)
            
        Returns:
            Energy lost in Joules
        """
        ke_initial = EnergyCalculator.kinetic_energy(mass_kg, initial_speed_kmh)
        ke_final = EnergyCalculator.kinetic_energy(mass_kg, final_speed_kmh)
        energy_loss = ke_initial - ke_final
        
        return energy_loss

    @staticmethod
    def acceleration_energy(mass_kg, initial_speed_kmh, final_speed_kmh, 
                          distance_km, efficiency=0.85):
        """
        Calculate energy required to accelerate a train.
        
        Formula: E = ΔKE / efficiency + Work against resistance
        
        Args:
            mass_kg: Mass of train
            initial_speed_kmh: Starting speed
            final_speed_kmh: Target speed
            distance_km: Distance over which acceleration happens
            efficiency: Motor efficiency (typically 0.85 for electric trains)
            
        Returns:
            Energy consumed in Joules
        """
        # Change in kinetic energy
        delta_ke = (EnergyCalculator.kinetic_energy(mass_kg, final_speed_kmh) - 
                   EnergyCalculator.kinetic_energy(mass_kg, initial_speed_kmh))
        
        # Work against rolling resistance
        distance_m = distance_km * 1000
        avg_speed_mps = kmh_to_mps((initial_speed_kmh + final_speed_kmh) / 2)
        rolling_resistance_force = ROLLING_RESISTANCE_COEFF * mass_kg * GRAVITY
        work_against_resistance = rolling_resistance_force * distance_m
        
        # Total energy accounting for motor efficiency
        total_energy = (delta_ke + work_against_resistance) / efficiency
        
        return total_energy

    @staticmethod
    def idle_energy(power_kw, time_seconds):
        """
        Calculate energy consumed while train is stationary (idle).
        
        Formula: E = Power × Time
        
        Args:
            power_kw: Idle power consumption in kilowatts
            time_seconds: Time spent idle
            
        Returns:
            Energy in Joules
        """
        return power_kw * 1000 * time_seconds  # Convert kW to W, then to Joules

    @staticmethod
    def uphill_energy(mass_kg, elevation_gain_m, distance_km, speed_kmh):
        """
        Calculate EXTRA energy needed to climb uphill (potential energy).
        This is critical for ghat sections!
        
        Formula: E_potential = m × g × h
        Plus energy to overcome increased resistance
        
        Args:
            mass_kg: Mass of train
            elevation_gain_m: Height climbed in meters
            distance_km: Distance traveled
            speed_kmh: Average speed
            
        Returns:
            Extra energy needed in Joules
        """
        # Potential energy gained
        potential_energy = mass_kg * GRAVITY * elevation_gain_m
        
        # Additional rolling resistance on slope
        gradient = elevation_gain_m / (distance_km * 1000)  # Rise over run
        slope_angle = math.atan(gradient)
        extra_resistance_force = mass_kg * GRAVITY * math.sin(slope_angle)
        distance_m = distance_km * 1000
        extra_work = extra_resistance_force * distance_m
        
        total_extra_energy = potential_energy + extra_work
        
        return total_extra_energy

    @staticmethod
    def downhill_energy_recovery(mass_kg, elevation_loss_m, has_regenerative=True):
        """
        Calculate energy that can be recovered going downhill.
        Electric trains can recover energy through regenerative braking!
        
        Formula: E_recovered = m × g × h × recovery_efficiency
        
        Args:
            mass_kg: Mass of train
            elevation_loss_m: Height descended in meters
            has_regenerative: Whether train has regenerative braking
            
        Returns:
            Energy recovered in Joules (negative = energy gained back)
        """
        potential_energy_released = mass_kg * GRAVITY * elevation_loss_m
        
        if has_regenerative:
            # Electric trains can recover ~30% of potential energy
            recovery_efficiency = 0.30
            energy_recovered = potential_energy_released * recovery_efficiency
            return -energy_recovered  # Negative because we're gaining energy
        else:
            # Diesel trains waste this as heat in brakes
            return 0

    @staticmethod
    def calculate_braking_distance(initial_speed_kmh, braking_rate_mps2):
        """
        Calculate how much distance is needed to stop.
        Important for conflict detection!
        
        Formula: d = v² / (2 × a)
        
        Args:
            initial_speed_kmh: Current speed
            braking_rate_mps2: Braking deceleration rate
            
        Returns:
            Distance in meters
        """
        speed_mps = kmh_to_mps(initial_speed_kmh)
        distance_m = (speed_mps ** 2) / (2 * braking_rate_mps2)
        return distance_m

    @staticmethod
    def calculate_braking_time(initial_speed_kmh, braking_rate_mps2):
        """
        Calculate how much time is needed to stop.
        
        Formula: t = v / a
        
        Args:
            initial_speed_kmh: Current speed
            braking_rate_mps2: Braking deceleration rate
            
        Returns:
            Time in seconds
        """
        speed_mps = kmh_to_mps(initial_speed_kmh)
        time_s = speed_mps / braking_rate_mps2
        return time_s


# Utility function for quick energy comparisons
def compare_energy_scenarios(scenario_a, scenario_b):
    """
    Compare two scenarios and return which one saves more energy.
    
    Args:
        scenario_a: dict with 'name' and 'energy_joules'
        scenario_b: dict with 'name' and 'energy_joules'
        
    Returns:
        dict with comparison results
    """
    energy_diff = abs(scenario_a['energy_joules'] - scenario_b['energy_joules'])
    energy_diff_kwh = joules_to_kwh(energy_diff)
    
    if scenario_a['energy_joules'] < scenario_b['energy_joules']:
        better_scenario = scenario_a['name']
        savings_percent = (energy_diff / scenario_b['energy_joules']) * 100
    else:
        better_scenario = scenario_b['name']
        savings_percent = (energy_diff / scenario_a['energy_joules']) * 100
    
    return {
        'better_scenario': better_scenario,
        'energy_saved_joules': energy_diff,
        'energy_saved_kwh': energy_diff_kwh,
        'savings_percent': savings_percent
    }
