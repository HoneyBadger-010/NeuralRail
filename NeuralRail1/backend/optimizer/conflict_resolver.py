"""
Conflict Resolver - The "Brain" of NeuralRail
Analyzes conflicts and generates optimal solutions considering:
1. Safety (Priority #1)
2. Train Priority (Rajdhani > Express > Local > Freight)
3. Energy Efficiency
4. Time/Schedule Impact
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from physics.energy_calculator import EnergyCalculator, joules_to_kwh
import copy


class ConflictResolver:
    """
    Generates and ranks solutions for train conflicts.
    This is the core decision-making engine.
    
    Configurable for different operational scenarios.
    """
    
    def __init__(self, config=None):
        """
        Initialize resolver with optional configuration.
        
        Args:
            config: Dict with weights and parameters
                {
                    'energy_weight': 0.4,      # How much energy matters
                    'delay_weight': 0.2,       # How much delay matters
                    'priority_weight': 0.4,    # How much priority matters
                    'priority_penalty': 500,   # Penalty for priority violation
                    'mode': 'balanced'         # 'balanced', 'energy_priority', 'time_priority'
                }
        """
        self.solution_history = []
        
        # Default configuration
        self.config = {
            'energy_weight': 0.4,
            'delay_weight': 0.2,
            'priority_weight': 0.4,
            'priority_penalty': 500,
            'mode': 'balanced',
            'consider_gradient': True,
            'consider_station_proximity': True,
            'emergency_threshold_minutes': 5
        }
        
        # Override with user config
        if config:
            self.config.update(config)
            
        # Apply mode presets
        self._apply_mode_preset()
        
    def _apply_mode_preset(self):
        """Apply preset configurations based on mode"""
        mode = self.config['mode']
        
        if mode == 'energy_priority':
            # Maximize energy savings (for low power situations)
            self.config['energy_weight'] = 0.6
            self.config['delay_weight'] = 0.1
            self.config['priority_weight'] = 0.3
            
        elif mode == 'time_priority':
            # Minimize delays (for rush hour)
            self.config['energy_weight'] = 0.2
            self.config['delay_weight'] = 0.5
            self.config['priority_weight'] = 0.3
            
        elif mode == 'strict_priority':
            # Strictly follow train priorities
            self.config['energy_weight'] = 0.2
            self.config['delay_weight'] = 0.2
            self.config['priority_weight'] = 0.6
            self.config['priority_penalty'] = 1000  # Even higher penalty
    
    def analyze_conflict(self, conflict, train_a, train_b, track_info=None):
        """
        Analyze a conflict and generate possible solutions.
        
        DECISION PRIORITY:
        1. Train Priority FIRST (Rajdhani > Express > Local > Freight)
        2. Energy calculations SECOND
        3. Exception: Ignore priority only if energy difference is MASSIVE (>1000 kWh)
        
        Args:
            conflict: Conflict dict from simulator
            train_a: First train object
            train_b: Second train object
            track_info: Optional track information (elevation, gradient)
                {
                    'train_a_gradient': 'uphill'/'downhill'/'flat',
                    'train_b_gradient': 'uphill'/'downhill'/'flat',
                    'parallel_tracks_available': bool,
                    'switching_possible': bool,
                    'tracks': int
                }
            
        Returns:
            List of solutions ranked by optimality
        """
        print(f"\n{'='*70}")
        print(f"ANALYZING CONFLICT [{self.config['mode'].upper()} MODE]")
        print(f"{'='*70}")
        print(f"Trains: {train_a.train_id} ↔ {train_b.train_id}")
        print(f"Time to conflict: {conflict['time_to_conflict_minutes']} minutes")
        print(f"Conflict location: {conflict['conflict_position_km']:.1f} km")
        print(f"Severity: {conflict['severity'].upper()}")
        
        # Check for emergency situation
        is_emergency = conflict['time_to_conflict_minutes'] < self.config['emergency_threshold_minutes']
        if is_emergency:
            print(f"⚠️  EMERGENCY: Less than {self.config['emergency_threshold_minutes']} minutes to collision!")
        
        # Generate all possible solutions
        solutions = []
        
        # Emergency mode: Only generate stop solutions
        if is_emergency:
            print("Emergency mode: Generating only stop solutions for safety")
            
            sol_1 = self._generate_stop_solution(
                train_to_stop=train_a, other_train=train_b,
                conflict=conflict, solution_id="EMERGENCY_A", track_info=track_info
            )
            solutions.append(sol_1)
            
            sol_2 = self._generate_stop_solution(
                train_to_stop=train_b, other_train=train_a,
                conflict=conflict, solution_id="EMERGENCY_B", track_info=track_info
            )
            solutions.append(sol_2)
            
        else:
            # Normal mode: Generate all solution types
            
            # Solution 1: Stop Train A
            sol_1 = self._generate_stop_solution(
                train_to_stop=train_a, other_train=train_b,
                conflict=conflict, solution_id="A1", track_info=track_info
            )
            solutions.append(sol_1)
            
            # Solution 2: Stop Train B
            sol_2 = self._generate_stop_solution(
                train_to_stop=train_b, other_train=train_a,
                conflict=conflict, solution_id="A2", track_info=track_info
            )
            solutions.append(sol_2)
            
            # Solution 3: Slow Train A
            sol_3 = self._generate_slow_solution(
                train_to_slow=train_a, other_train=train_b,
                conflict=conflict, solution_id="B1"
            )
            solutions.append(sol_3)
            
            # Solution 4: Slow Train B
            sol_4 = self._generate_slow_solution(
                train_to_slow=train_b, other_train=train_a,
                conflict=conflict, solution_id="B2"
            )
            solutions.append(sol_4)
            
            # Solution 5: Both trains slow down slightly
            sol_5 = self._generate_both_slow_solution(
                train_a, train_b, conflict, solution_id="C1"
            )
            solutions.append(sol_5)
            
            # Solution 6: Slow high-priority train + Stop low-priority train
            # This is a smart combination: slow RAJ to give FRT time to reach junction
            if train_a.priority < train_b.priority:
                # train_a is higher priority (lower number)
                sol_6 = self._generate_slow_and_stop_solution(
                    train_to_slow=train_a, train_to_stop=train_b,
                    conflict=conflict, solution_id="D1"
                )
            else:
                sol_6 = self._generate_slow_and_stop_solution(
                    train_to_slow=train_b, train_to_stop=train_a,
                    conflict=conflict, solution_id="D1"
                )
            solutions.append(sol_6)
            
            # Solution 7: Multi-step - Slow both, then switch track (only if parallel tracks available)
            if track_info and track_info.get('parallel_tracks_available', False):
                sol_7 = self._generate_multi_step_solution(
                    train_a, train_b, conflict, solution_id="E1", track_info=track_info
                )
                solutions.append(sol_7)
        
        # CRITICAL: Filter out invalid solutions based on track constraints
        solutions = self._filter_invalid_solutions(solutions, track_info)
        
        # Rank solutions by ENERGY FIRST (renewable energy domain)
        ranked_solutions = self._rank_solutions_by_priority(solutions, train_a, train_b)
        
        # Store in history
        self.solution_history.append({
            'conflict': conflict,
            'solutions': ranked_solutions,
            'recommended': ranked_solutions[0]
        })
        
        return ranked_solutions
        
    def _generate_stop_solution(self, train_to_stop, other_train, conflict, solution_id, track_info=None):
        """Generate a solution where one train stops completely"""
        
        # Calculate energy cost of stopping
        braking_energy = EnergyCalculator.braking_energy_loss(
            train_to_stop.mass_kg,
            train_to_stop.speed_kmh,
            0  # Complete stop
        )
        
        # Account for regenerative braking (30% recovery for electric trains)
        if train_to_stop.train_type['traction_type'] == 'electric':
            braking_energy *= 0.70  # Net loss after recovery
        
        # Calculate idle energy while waiting
        wait_time_seconds = conflict['time_to_conflict_seconds']
        idle_energy = EnergyCalculator.idle_energy(
            train_to_stop.idle_power_kw,
            wait_time_seconds
        )
        
        # Calculate restart energy
        restart_energy = EnergyCalculator.acceleration_energy(
            train_to_stop.mass_kg,
            0,
            train_to_stop.speed_kmh,
            2  # 2 km to accelerate back to speed
        )
        
        total_energy = braking_energy + idle_energy + restart_energy
        
        # Apply gradient penalty if configured - PHYSICS-BASED CALCULATION
        gradient_penalty_kwh = 0
        if track_info and self.config['consider_gradient']:
            train_gradient = track_info.get(f'{train_to_stop.train_id}_gradient', 'flat')
            
            if train_gradient == 'uphill':
                # CORRECTED: Physics-based gradient penalty calculation
                # Based on Bhor Ghat: 1.83% gradient, 1000m climb over ~55km
                # 
                # For a 4000-ton freight on 1.83% gradient:
                # 1. Potential Energy: m × g × h = 4,000,000 × 9.81 × 18.3m = 718 MJ = 199 kWh
                # 2. Acceleration Energy: 0.5 × m × v² / efficiency = 126 kWh
                # 3. Rolling Resistance: ~50 kWh
                # 4. Motor Inefficiency (~15% loss): ~56 kWh
                # Total ≈ 430-500 kWh (NOT 1200 kWh!)
                
                mass_tons = train_to_stop.mass_kg / 1000
                gradient_percent = 1.83  # Bhor Ghat gradient
                elevation_gain_m = 18.3  # Per km of track
                
                # Calculate based on train mass
                if mass_tons >= 4000:  # Heavy freight (4000+ tons)
                    gradient_penalty_kwh = 500  # Corrected from 1200
                elif mass_tons >= 1500:  # Express/Rajdhani (1500-4000 tons)
                    gradient_penalty_kwh = 300
                elif mass_tons >= 800:  # Local EMU (800-1500 tons)
                    gradient_penalty_kwh = 150
                else:  # Light trains (<800 tons)
                    gradient_penalty_kwh = 80
                
                total_energy += gradient_penalty_kwh * 3_600_000  # Convert to Joules
        
        # Calculate time delay
        braking_time = EnergyCalculator.calculate_braking_time(
            train_to_stop.speed_kmh,
            train_to_stop.braking_rate_mps2
        )
        restart_time = 120  # ~2 minutes to restart and accelerate
        total_delay = braking_time + wait_time_seconds + restart_time
        
        # Calculate score (lower is better)
        score = self._calculate_solution_score(
            energy_joules=total_energy,
            delay_seconds=total_delay,
            priority_violation=(train_to_stop.priority < other_train.priority),
            train_priority=train_to_stop.priority
        )
        
        breakdown = {
            'braking_kwh': joules_to_kwh(braking_energy),
            'idle_kwh': joules_to_kwh(idle_energy),
            'restart_kwh': joules_to_kwh(restart_energy)
        }
        
        # Generate clear, actionable description
        stop_location = f"{conflict['conflict_position_km']:.0f} km"
        
        # Try to find nearest station for clearer description
        station_names = {
            0: "New Delhi", 15: "Sadar Bazar", 30: "Ghaziabad Junction", 
            60: "Aligarh", 100: "Tundla Junction", 141: "Mathura Junction"
        }
        nearest_station = None
        min_dist = float('inf')
        for km, name in station_names.items():
            dist = abs(conflict['conflict_position_km'] - km)
            if dist < min_dist and dist < 10:  # Within 10km of station
                min_dist = dist
                nearest_station = name
                stop_location = f"{name} ({km} km)"
        
        description = f"Stop {train_to_stop.train_id} at {stop_location}, let {other_train.train_id} pass safely, then resume"
        
        if gradient_penalty_kwh > 0:
            breakdown['gradient_penalty_kwh'] = gradient_penalty_kwh
            description += f" (⚠️ +{gradient_penalty_kwh} kWh uphill restart penalty!)"
        
        # Add clear action text for frontend
        action_text = f"Stop {train_to_stop.train_id} → Let {other_train.train_id} pass"
        
        # Calculate safety score (10 = perfect)
        safety_score = 10
        if train_to_stop.priority < other_train.priority:
            safety_score -= 2  # Priority violation reduces safety perception
        if gradient_penalty_kwh > 0:
            safety_score -= 1  # Gradient penalty indicates risk
        
        # Determine platform for stopping
        # Odd/less-used platforms (7,9,11) for freight - keeps main platforms free
        # Even platforms (2,4,6) for passenger trains
        if train_to_stop.priority >= 4:  # Freight or low priority
            platform_number = 9  # Odd/less-used platform for freight
            platform_type = "odd (less used)"
        else:
            platform_number = 2  # Main platform for passenger
            platform_type = "even (main)"
        
        # Station-specific platform info
        platform_info = {
            'platform_number': platform_number,
            'platform_type': platform_type,
            'station_name': nearest_station or "Junction",
            'platform_description': f"Platform {platform_number} ({platform_type})"
        }
        
        return {
            'solution_id': solution_id,
            'type': 'stop',
            'action': action_text,
            'action_short': f"Stop {train_to_stop.train_id}",
            'train_affected': train_to_stop.train_id,
            'train_passing': other_train.train_id,
            'stop_location_km': conflict['conflict_position_km'],
            'stop_location_name': stop_location,
            'description': description,
            'energy_joules': total_energy,
            'energy_kwh': joules_to_kwh(total_energy),
            'delay_seconds': total_delay,
            'delay_minutes': round(total_delay / 60, 1),
            'priority_violation': train_to_stop.priority < other_train.priority,
            'score': score,
            'safety_score': safety_score,
            'breakdown': breakdown,
            'has_gradient_penalty': gradient_penalty_kwh > 0,
            'platform_info': platform_info
        }
        
    def _generate_slow_solution(self, train_to_slow, other_train, conflict, solution_id):
        """Generate a solution where one train slows down"""
        
        # Reduce speed by 20-30% to avoid conflict
        speed_reduction_percent = 0.25
        new_speed = train_to_slow.speed_kmh * (1 - speed_reduction_percent)
        
        # Calculate energy cost of slowing down
        braking_energy = EnergyCalculator.braking_energy_loss(
            train_to_slow.mass_kg,
            train_to_slow.speed_kmh,
            new_speed
        )
        
        if train_to_slow.train_type['traction_type'] == 'electric':
            braking_energy *= 0.70  # Net loss after regenerative recovery
        
        # Calculate time delay (traveling slower)
        distance_to_conflict = abs(conflict['conflict_position_km'] - train_to_slow.position_km)
        time_at_original_speed = (distance_to_conflict / train_to_slow.speed_kmh) * 3600
        time_at_new_speed = (distance_to_conflict / new_speed) * 3600
        delay = time_at_new_speed - time_at_original_speed
        
        # Calculate re-acceleration energy
        reaccel_energy = EnergyCalculator.acceleration_energy(
            train_to_slow.mass_kg,
            new_speed,
            train_to_slow.speed_kmh,
            1  # 1 km to accelerate back
        )
        
        total_energy = braking_energy + reaccel_energy
        
        score = self._calculate_solution_score(
            energy_joules=total_energy,
            delay_seconds=delay,
            priority_violation=(train_to_slow.priority < other_train.priority),
            train_priority=train_to_slow.priority
        )
        
        # Calculate safety score
        safety_score = 9  # Slowing is safer than stopping
        if train_to_slow.priority < other_train.priority:
            safety_score -= 1
        
        return {
            'solution_id': solution_id,
            'type': 'slow',
            'action': f"Slow {train_to_slow.train_id} by {speed_reduction_percent*100:.0f}%",
            'train_affected': train_to_slow.train_id,
            'description': f"Reduce {train_to_slow.train_id} speed from {train_to_slow.speed_kmh:.0f} to {new_speed:.0f} km/h, then resume normal speed",
            'energy_joules': total_energy,
            'energy_kwh': joules_to_kwh(total_energy),
            'delay_seconds': delay,
            'delay_minutes': round(delay / 60, 1),
            'priority_violation': train_to_slow.priority < other_train.priority,
            'score': score,
            'safety_score': safety_score,
            'breakdown': {
                'braking_kwh': joules_to_kwh(braking_energy),
                'reaccel_kwh': joules_to_kwh(reaccel_energy)
            }
        }
        
    def _generate_both_slow_solution(self, train_a, train_b, conflict, solution_id):
        """Generate a solution where both trains slow down slightly"""
        
        # Both trains reduce speed by 15%
        speed_reduction = 0.15
        
        # Calculate for train A
        new_speed_a = train_a.speed_kmh * (1 - speed_reduction)
        braking_a = EnergyCalculator.braking_energy_loss(
            train_a.mass_kg, train_a.speed_kmh, new_speed_a
        )
        if train_a.train_type['traction_type'] == 'electric':
            braking_a *= 0.70
            
        reaccel_a = EnergyCalculator.acceleration_energy(
            train_a.mass_kg, new_speed_a, train_a.speed_kmh, 1
        )
        
        # Calculate for train B
        new_speed_b = train_b.speed_kmh * (1 - speed_reduction)
        braking_b = EnergyCalculator.braking_energy_loss(
            train_b.mass_kg, train_b.speed_kmh, new_speed_b
        )
        if train_b.train_type['traction_type'] == 'electric':
            braking_b *= 0.70
            
        reaccel_b = EnergyCalculator.acceleration_energy(
            train_b.mass_kg, new_speed_b, train_b.speed_kmh, 1
        )
        
        total_energy = braking_a + reaccel_a + braking_b + reaccel_b
        
        # Minimal delay (both slow down proportionally)
        delay = 60  # ~1 minute delay
        
        score = self._calculate_solution_score(
            energy_joules=total_energy,
            delay_seconds=delay,
            priority_violation=False,  # No priority violation (both affected equally)
            train_priority=min(train_a.priority, train_b.priority)
        )
        
        return {
            'solution_id': solution_id,
            'type': 'both_slow',
            'action': f"Both trains slow by {speed_reduction*100:.0f}%",
            'train_affected': f"{train_a.train_id} & {train_b.train_id}",
            'description': f"Both trains reduce speed by {speed_reduction*100:.0f}% temporarily to increase separation",
            'energy_joules': total_energy,
            'energy_kwh': joules_to_kwh(total_energy),
            'delay_seconds': delay,
            'delay_minutes': round(delay / 60, 1),
            'priority_violation': False,
            'score': score,
            'safety_score': 10,  # Best safety - both trains cooperate
            'breakdown': {
                f'{train_a.train_id}_kwh': joules_to_kwh(braking_a + reaccel_a),
                f'{train_b.train_id}_kwh': joules_to_kwh(braking_b + reaccel_b)
            }
        }
    
    def _generate_slow_and_stop_solution(self, train_to_slow, train_to_stop, conflict, solution_id):
        """
        Generate a multi-step solution: Slow one train + Stop another.
        This is ideal for head-on conflicts where:
        - High priority train slows slightly (buys time)
        - Low priority train stops completely (resolves conflict)
        """
        
        # Step 1: Slow the high-priority train by 20%
        speed_reduction = 0.20
        new_speed_slow = train_to_slow.speed_kmh * (1 - speed_reduction)
        
        braking_slow = EnergyCalculator.braking_energy_loss(
            train_to_slow.mass_kg, train_to_slow.speed_kmh, new_speed_slow
        )
        if train_to_slow.train_type['traction_type'] == 'electric':
            braking_slow *= 0.70
        
        # Step 2: Stop the low-priority train completely
        braking_stop = EnergyCalculator.braking_energy_loss(
            train_to_stop.mass_kg, train_to_stop.speed_kmh, 0
        )
        if train_to_stop.train_type['traction_type'] == 'electric':
            braking_stop *= 0.70
        
        # Idle energy while stopped train waits
        wait_time_seconds = conflict['time_to_conflict_seconds'] + 60  # Extra buffer
        idle_energy = EnergyCalculator.idle_energy(
            train_to_stop.idle_power_kw, wait_time_seconds
        )
        
        # Restart energy for stopped train
        restart_energy = EnergyCalculator.acceleration_energy(
            train_to_stop.mass_kg, 0, train_to_stop.speed_kmh, 2
        )
        
        # Re-acceleration for slowed train
        reaccel_slow = EnergyCalculator.acceleration_energy(
            train_to_slow.mass_kg, new_speed_slow, train_to_slow.speed_kmh, 1
        )
        
        total_energy = braking_slow + braking_stop + idle_energy + restart_energy + reaccel_slow
        
        # Delay: slowed train has minimal delay, stopped train has full delay
        delay_slow = 30  # 30 seconds for slowing
        delay_stop = wait_time_seconds + 120  # Wait time + restart time
        total_delay = delay_stop  # Report the longer delay
        
        # This solution respects priority (high priority only slows, doesn't stop)
        priority_violation = False
        
        score = self._calculate_solution_score(
            energy_joules=total_energy,
            delay_seconds=total_delay,
            priority_violation=priority_violation,
            train_priority=train_to_stop.priority
        )
        
        # Bonus for being a smart multi-step solution
        score *= 0.90  # 10% bonus
        
        return {
            'solution_id': solution_id,
            'type': 'slow_and_stop',
            'action': f"Slow {train_to_slow.train_id} + Stop {train_to_stop.train_id}",
            'action_short': f"Slow {train_to_slow.train_id}, Stop {train_to_stop.train_id}",
            'train_affected': train_to_stop.train_id,
            'train_slowed': train_to_slow.train_id,
            'train_passing': train_to_slow.train_id,
            'description': f"Step 1: Slow {train_to_slow.train_id} by {speed_reduction*100:.0f}% (buys time). Step 2: Stop {train_to_stop.train_id} at junction. Step 3: {train_to_slow.train_id} passes, then {train_to_stop.train_id} resumes.",
            'energy_joules': total_energy,
            'energy_kwh': joules_to_kwh(total_energy),
            'delay_seconds': total_delay,
            'delay_minutes': round(total_delay / 60, 1),
            'priority_violation': priority_violation,
            'score': score,
            'safety_score': 10,
            'is_multi_step': True,
            'steps': [
                {'step': 1, 'action': f'Slow {train_to_slow.train_id} by {speed_reduction*100:.0f}%', 'reason': 'Buys time for safe resolution'},
                {'step': 2, 'action': f'Stop {train_to_stop.train_id} at junction', 'reason': 'Lower priority train waits'},
                {'step': 3, 'action': f'{train_to_slow.train_id} passes through', 'reason': 'Higher priority continues'},
                {'step': 4, 'action': f'{train_to_stop.train_id} resumes', 'reason': 'Conflict resolved'}
            ],
            'breakdown': {
                f'{train_to_slow.train_id}_slow_kwh': joules_to_kwh(braking_slow),
                f'{train_to_slow.train_id}_reaccel_kwh': joules_to_kwh(reaccel_slow),
                f'{train_to_stop.train_id}_stop_kwh': joules_to_kwh(braking_stop),
                f'{train_to_stop.train_id}_idle_kwh': joules_to_kwh(idle_energy),
                f'{train_to_stop.train_id}_restart_kwh': joules_to_kwh(restart_energy)
            }
        }
        
    def _filter_invalid_solutions(self, solutions, track_info):
        """
        Filter out solutions that violate track constraints.
        
        CRITICAL: This ensures we don't recommend impossible solutions!
        """
        if not track_info:
            return solutions
        
        valid_solutions = []
        parallel_tracks = track_info.get('parallel_tracks_available', False)
        switching_possible = track_info.get('switching_possible', False)
        
        for sol in solutions:
            # Check if solution is valid given track constraints
            
            # "both_slow" only works if parallel tracks are available for permanent resolution
            # If no parallel tracks, both_slow is just delaying the inevitable collision
            if sol['type'] == 'both_slow':
                if not parallel_tracks:
                    continue  # Skip - this is just a temporary fix, not a real solution
            
            # "multi_step" requires parallel tracks for switching
            if sol['type'] == 'multi_step':
                if not switching_possible:
                    continue  # Skip - track switching not possible
            
            valid_solutions.append(sol)
        
        return valid_solutions if valid_solutions else solutions  # Return original if all filtered
    
    def _rank_solutions_by_priority(self, solutions, train_a, train_b):
        """
        Rank solutions by CONFLICT RESOLUTION EFFECTIVENESS FIRST, then energy.
        
        CORRECTED DECISION LOGIC:
        1. Separate solutions by type: PERMANENT (stop/switch) vs TEMPORARY (slow)
        2. Prefer PERMANENT solutions (they actually resolve the conflict)
        3. Within permanent solutions: respect priority, then optimize energy
        4. Temporary solutions (slow) are fallback only
        
        PHILOSOPHY: Safety & Conflict Resolution FIRST, Energy optimization SECOND
        """
        
        # Separate solutions by effectiveness
        permanent_solutions = []  # stop, multi_step
        temporary_solutions = []  # slow, both_slow
        
        for sol in solutions:
            if sol['type'] in ['stop', 'multi_step']:
                permanent_solutions.append(sol)
            else:
                temporary_solutions.append(sol)
        
        # Within permanent solutions: priority-respecting first, then by energy
        priority_respecting_perm = [s for s in permanent_solutions if not s['priority_violation']]
        priority_violating_perm = [s for s in permanent_solutions if s['priority_violation']]
        
        priority_respecting_perm.sort(key=lambda x: x['energy_kwh'])
        priority_violating_perm.sort(key=lambda x: x['energy_kwh'])
        
        # Check if priority override is justified by MASSIVE energy savings (>1000 kWh)
        if priority_respecting_perm and priority_violating_perm:
            best_respecting = priority_respecting_perm[0]
            best_violating = priority_violating_perm[0]
            energy_diff = best_respecting['energy_kwh'] - best_violating['energy_kwh']
            
            if energy_diff > 1000:
                # EXCEPTIONAL: Massive energy savings (e.g., gradient penalty case)
                print(f"\n💡 EXCEPTIONAL: Saving {energy_diff:.0f} kWh justifies priority override")
                print(f"   Best solution: {best_violating['action']} ({best_violating['energy_kwh']:.0f} kWh)")
                print(f"   vs Priority-respecting: {best_respecting['action']} ({best_respecting['energy_kwh']:.0f} kWh)")
                return priority_violating_perm + priority_respecting_perm + temporary_solutions
        
        # Normal case: Priority-respecting permanent solutions first
        print(f"\n✅ CONFLICT RESOLUTION: Prioritizing permanent solutions that respect train priorities")
        return priority_respecting_perm + priority_violating_perm + temporary_solutions
    
    def _calculate_solution_score(self, energy_joules, delay_seconds, 
                                  priority_violation, train_priority):
        """
        Calculate a score for a solution (lower is better).
        
        Scoring factors:
        1. Safety: Always ensured (all solutions are safe)
        2. Priority violation: Heavy penalty
        3. Energy consumption: Moderate weight
        4. Time delay: Light weight
        5. Train priority: Higher priority trains get preference
        """
        
        # Base score from energy
        energy_kwh = joules_to_kwh(energy_joules)
        
        # FIXED: Don't cap energy score for gradient penalties (>1000 kWh)
        if energy_kwh > 1000:
            energy_score = energy_kwh / 5  # Higher weight, no cap
        else:
            energy_score = min(energy_kwh / 10, 100)  # Cap at 100 for normal cases
        
        # Delay score (normalized)
        delay_minutes = delay_seconds / 60
        delay_score = min(delay_minutes * 2, 50)  # Cap at 50
        
        # Priority violation penalty (huge!)
        priority_penalty = 500 if priority_violation else 0
        
        # Train priority factor (lower priority number = higher importance)
        # Priority 1 (Rajdhani) gets less penalty than Priority 5 (Freight)
        priority_factor = train_priority * 10
        
        # Total score (lower is better) - using configured weights
        total_score = (
            energy_score * self.config['energy_weight'] +
            delay_score * self.config['delay_weight'] +
            priority_penalty +
            priority_factor * self.config['priority_weight']
        )
        
        return round(total_score, 2)
        
    def _rank_solutions(self, solutions):
        """Rank solutions by score (lower is better)"""
        return sorted(solutions, key=lambda x: x['score'])
        
    def print_solutions(self, solutions):
        """Print all solutions in a readable format"""
        print(f"\n{'='*70}")
        print(f"GENERATED SOLUTIONS (Ranked by Optimality)")
        print(f"{'='*70}")
        
        for i, sol in enumerate(solutions, 1):
            print(f"\n{i}. {sol['action']} [Score: {sol['score']:.1f}]")
            print(f"   Type: {sol['type']}")
            print(f"   Description: {sol['description']}")
            print(f"   Energy: {sol['energy_kwh']:.1f} kWh")
            print(f"   Delay: {sol['delay_minutes']:.1f} minutes")
            print(f"   Priority Violation: {'YES ⚠️' if sol['priority_violation'] else 'NO ✓'}")
            
            # Show multi-step details
            if sol.get('is_multi_step', False):
                print(f"   Multi-Step Plan:")
                for step in sol['steps']:
                    print(f"     {step['step']}. {step['action']} ({step['time']})")
                    print(f"        → {step['reason']}")
            
            if i == 1:
                print(f"   >>> RECOMMENDED SOLUTION <<<")
                
        print(f"\n{'='*70}")
        print(f"RECOMMENDATION: {solutions[0]['action']}")
        if solutions[0].get('is_multi_step', False):
            print(f"Type: Multi-step solution (sophisticated approach)")
        print(f"Reason: Best balance of safety, priority, energy, and time")
        print(f"{'='*70}")
        
        return solutions[0]  # Return best solution
        
    def compare_solutions(self, solution_a, solution_b):
        """Compare two solutions and explain the difference"""
        energy_diff = abs(solution_a['energy_kwh'] - solution_b['energy_kwh'])
        delay_diff = abs(solution_a['delay_minutes'] - solution_b['delay_minutes'])
        
        if solution_a['score'] < solution_b['score']:
            better = solution_a
            worse = solution_b
        else:
            better = solution_b
            worse = solution_a
            
        comparison = {
            'better_solution': better['action'],
            'energy_saved_kwh': energy_diff,
            'time_saved_minutes': delay_diff,
            'score_difference': abs(solution_a['score'] - solution_b['score']),
            'reason': self._explain_why_better(better, worse)
        }
        
        return comparison
        
    def _generate_multi_step_solution(self, train_a, train_b, conflict, solution_id, track_info):
        """
        Generate a multi-step solution (e.g., slow both, then switch track).
        This is more realistic - buys time first, then permanent resolution.
        """
        
        # Step 1: Both slow by 15% (buys time)
        speed_reduction = 0.15
        new_speed_a = train_a.speed_kmh * (1 - speed_reduction)
        new_speed_b = train_b.speed_kmh * (1 - speed_reduction)
        
        # Energy for slowing
        braking_a = EnergyCalculator.braking_energy_loss(
            train_a.mass_kg, train_a.speed_kmh, new_speed_a
        )
        if train_a.train_type['traction_type'] == 'electric':
            braking_a *= 0.70
            
        braking_b = EnergyCalculator.braking_energy_loss(
            train_b.mass_kg, train_b.speed_kmh, new_speed_b
        )
        if train_b.train_type['traction_type'] == 'electric':
            braking_b *= 0.70
        
        # Step 2: Switch one train to parallel track (minimal energy)
        # Determine which train to switch (lower priority or lighter)
        if train_a.priority > train_b.priority:  # Higher number = lower priority
            train_to_switch = train_a
            train_staying = train_b
        elif train_a.priority < train_b.priority:
            train_to_switch = train_b
            train_staying = train_a
        else:
            # Same priority, switch lighter train
            train_to_switch = train_a if train_a.mass_kg < train_b.mass_kg else train_b
            train_staying = train_b if train_to_switch == train_a else train_a
        
        # Track switching energy (minimal - just lateral movement)
        switch_energy_kwh = 5  # Small energy cost for switching tracks
        
        # Re-acceleration energy (back to original speed)
        reaccel_a = EnergyCalculator.acceleration_energy(
            train_a.mass_kg, new_speed_a, train_a.speed_kmh, 1
        )
        reaccel_b = EnergyCalculator.acceleration_energy(
            train_b.mass_kg, new_speed_b, train_b.speed_kmh, 1
        )
        
        total_energy = braking_a + braking_b + reaccel_a + reaccel_b + (switch_energy_kwh * 3_600_000)
        
        # Delay: Minimal (just the slowing period + track switch)
        delay = 90  # ~1.5 minutes (slowing + switching)
        
        score = self._calculate_solution_score(
            energy_joules=total_energy,
            delay_seconds=delay,
            priority_violation=False,
            train_priority=min(train_a.priority, train_b.priority)
        )
        
        # Bonus: Multi-step solutions get a small score reduction (they're smarter!)
        score *= 0.95  # 5% bonus for being a sophisticated solution
        
        return {
            'solution_id': solution_id,
            'type': 'multi_step',
            'action': f"Slow both, then switch {train_to_switch.train_id} to parallel track",
            'train_affected': f"{train_a.train_id} & {train_b.train_id}",
            'description': f"Step 1: Both trains slow by {speed_reduction*100:.0f}% to buy time. Step 2: Switch {train_to_switch.train_id} to parallel track for permanent resolution. {train_staying.train_id} continues on main track.",
            'energy_joules': total_energy,
            'energy_kwh': joules_to_kwh(total_energy),
            'delay_seconds': delay,
            'delay_minutes': round(delay / 60, 1),
            'priority_violation': False,
            'score': score,
            'breakdown': {
                f'{train_a.train_id}_slow_kwh': joules_to_kwh(braking_a),
                f'{train_b.train_id}_slow_kwh': joules_to_kwh(braking_b),
                'track_switch_kwh': switch_energy_kwh,
                f'{train_a.train_id}_reaccel_kwh': joules_to_kwh(reaccel_a),
                f'{train_b.train_id}_reaccel_kwh': joules_to_kwh(reaccel_b)
            },
            'steps': [
                {
                    'step': 1,
                    'action': f"Slow both trains by {speed_reduction*100:.0f}%",
                    'time': '0-1 min',
                    'reason': 'Buy time and reduce relative closing speed'
                },
                {
                    'step': 2,
                    'action': f"Switch {train_to_switch.train_id} to parallel track",
                    'time': '1-1.5 min',
                    'reason': 'Permanent conflict resolution, trains on separate tracks'
                },
                {
                    'step': 3,
                    'action': 'Both trains resume normal speed',
                    'time': '1.5-2 min',
                    'reason': 'Return to schedule, no further conflict'
                }
            ],
            'is_multi_step': True
        }
    
    def _explain_why_better(self, better, worse):
        """Generate explanation for why one solution is better"""
        reasons = []
        
        if better['priority_violation'] != worse['priority_violation']:
            reasons.append("respects train priority")
            
        if better['energy_kwh'] < worse['energy_kwh']:
            savings = worse['energy_kwh'] - better['energy_kwh']
            reasons.append(f"saves {savings:.1f} kWh")
            
        if better['delay_minutes'] < worse['delay_minutes']:
            time_saved = worse['delay_minutes'] - better['delay_minutes']
            reasons.append(f"reduces delay by {time_saved:.1f} minutes")
        
        if better.get('is_multi_step', False):
            reasons.append("uses sophisticated multi-step approach")
            
        if not reasons:
            reasons.append("better overall balance")
            
        return " and ".join(reasons)
