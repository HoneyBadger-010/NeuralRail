"""
Scenario Runner - Executes pre-configured demo scenarios
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'backend'))

from simulation.railway_simulator import RailwaySimulator
from optimizer.conflict_resolver import ConflictResolver
from ai_agent.llm_explainer import LLMExplainer
from scenario_definitions import get_scenario_by_index, list_scenarios, ALL_SCENARIOS
import time


class ScenarioRunner:
    """
    Runs pre-configured demo scenarios for SIH presentation.
    """
    
    def __init__(self):
        self.explainer = LLMExplainer()
        
    def run_scenario(self, scenario, verbose=True, simulate_steps=20):
        """
        Run a complete scenario from start to finish.
        
        Args:
            scenario: Scenario dict from scenario_definitions
            verbose: Print detailed output
            simulate_steps: Number of simulation steps to run
            
        Returns:
            Results dict with all metrics
        """
        
        if verbose:
            self._print_scenario_header(scenario)
        
        # Initialize simulator
        sim = RailwaySimulator()
        
        # Add trains
        trains = {}
        for train_config in scenario['trains']:
            train = sim.add_train(
                train_id=train_config['train_id'],
                train_type_key=train_config['train_type'],
                initial_position_km=train_config['initial_position_km'],
                initial_speed_kmh=train_config['initial_speed_kmh'],
                destination_km=train_config['destination_km'],
                direction=train_config['direction']
            )
            trains[train_config['train_id']] = train
        
        if verbose:
            print(f"\n✓ Initialized {len(trains)} trains")
            self._print_train_details(trains)
        
        # Run simulation until conflict detected
        if verbose:
            print(f"\n⏳ Running simulation...")
        
        for step in range(simulate_steps):
            sim.step()
            if sim.conflicts_detected:
                if verbose:
                    print(f"✓ Conflict detected at step {step} ({sim.current_time/60:.1f} min)")
                break
        
        # Check if conflict was detected
        if not sim.conflicts_detected:
            if verbose:
                print("⚠️  No conflict detected in this scenario")
            return None
        
        conflict = sim.conflicts_detected[0]
        
        if verbose:
            self._print_conflict_details(conflict)
        
        # Get train objects for resolver
        train_ids = list(trains.keys())
        train_a = trains[train_ids[0]]
        train_b = trains[train_ids[1]] if len(train_ids) > 1 else train_a
        
        # Initialize resolver with appropriate mode
        operational_mode = scenario.get('operational_mode', 'balanced')
        resolver = ConflictResolver(config={'mode': operational_mode})
        
        if verbose and operational_mode != 'balanced':
            print(f"\n🔧 Using {operational_mode.upper()} mode")
        
        # Prepare track_info with train-specific gradient data
        track_info = scenario.get('track_info', {}).copy()
        
        # Add train-specific gradient information from scenario
        for train_config in scenario['trains']:
            if train_config['train_id'] == train_a.train_id:
                track_info[f'{train_a.train_id}_gradient'] = train_config.get('gradient_status', 'flat')
            if train_config['train_id'] == train_b.train_id:
                track_info[f'{train_b.train_id}_gradient'] = train_config.get('gradient_status', 'flat')
        
        # Analyze conflict
        solutions = resolver.analyze_conflict(
            conflict, train_a, train_b,
            track_info=track_info
        )
        
        best_solution = solutions[0]
        
        if verbose:
            self._print_solution_summary(best_solution, solutions)
        
        # Generate AI explanation
        train_a_info = {
            'name': train_a.name,
            'priority': train_a.priority,
            'mass_tons': train_a.mass_kg / 1000,
            'position_km': train_a.position_km,
            'speed_kmh': train_a.speed_kmh
        }
        
        train_b_info = {
            'name': train_b.name,
            'priority': train_b.priority,
            'mass_tons': train_b.mass_kg / 1000,
            'position_km': train_b.position_km,
            'speed_kmh': train_b.speed_kmh
        }
        
        explanation = self.explainer.explain_decision(
            best_solution, conflict, train_a_info, train_b_info,
            alternative_solutions=solutions[1:3]
        )
        
        if verbose:
            self._print_ai_explanation(explanation)
        
        # Calculate energy savings (worst = highest energy consumption)
        worst_solution = max(solutions, key=lambda x: x['energy_kwh'])
        energy_saved = worst_solution['energy_kwh'] - best_solution['energy_kwh']
        
        if verbose:
            self._print_energy_impact(best_solution, worst_solution, energy_saved)
        
        # Compare with expected outcome (if provided)
        if verbose and 'expected_outcome' in scenario:
            self._print_expected_vs_actual(scenario['expected_outcome'], {
                'energy_saved_kwh': energy_saved,
                'recommended_solution': best_solution['action'],
                'time_to_conflict_min': conflict['time_to_conflict_minutes']
            })
        
        # Return results
        return {
            'scenario': scenario,
            'conflict': conflict,
            'solutions': solutions,
            'best_solution': best_solution,
            'explanation': explanation,
            'energy_saved_kwh': energy_saved,
            'trains': trains,
            'simulator': sim
        }
    
    def _print_scenario_header(self, scenario):
        """Print scenario header"""
        print("\n" + "="*70)
        print(f"SCENARIO: {scenario['name']}")
        print("="*70)
        print(f"Description: {scenario['description']}")
        if 'difficulty' in scenario:
            print(f"Difficulty: {scenario['difficulty']}")
        if 'key_features' in scenario:
            print(f"Key Features: {', '.join(scenario['key_features'])}")
        print("="*70)
    
    def _print_train_details(self, trains):
        """Print train details"""
        print("\n📊 TRAIN DETAILS:")
        print("-" * 70)
        for train_id, train in trains.items():
            print(f"\n{train_id}:")
            print(f"  Type: {train.name}")
            print(f"  Priority: {train.priority}")
            print(f"  Mass: {train.mass_kg/1000:.0f} tons")
            print(f"  Position: {train.position_km:.1f} km")
            print(f"  Speed: {train.speed_kmh:.0f} km/h")
            print(f"  Destination: {train.destination_km:.0f} km")
    
    def _print_conflict_details(self, conflict):
        """Print conflict details"""
        print("\n⚠️  CONFLICT DETECTED:")
        print("-" * 70)
        print(f"  Trains: {conflict['train_a']} ↔ {conflict['train_b']}")
        print(f"  Location: {conflict['conflict_position_km']:.1f} km")
        print(f"  Time to conflict: {conflict['time_to_conflict_minutes']:.1f} minutes")
        print(f"  Severity: {conflict['severity'].upper()}")
    
    def _print_solution_summary(self, best_solution, all_solutions):
        """Print solution summary"""
        print("\n🤖 AI ANALYSIS:")
        print("-" * 70)
        print(f"  Generated {len(all_solutions)} possible solutions")
        print(f"  Analyzed: Safety, Priority, Energy, Time")
        
        print("\n✅ RECOMMENDED SOLUTION:")
        print("-" * 70)
        print(f"  Action: {best_solution['action']}")
        print(f"  Type: {best_solution['type']}")
        print(f"  Energy: {best_solution['energy_kwh']:.1f} kWh")
        print(f"  Delay: {best_solution['delay_minutes']:.1f} minutes")
        print(f"  Priority: {'Respected ✓' if not best_solution['priority_violation'] else 'Override ⚠️'}")
        print(f"  Score: {best_solution['score']:.1f} (lower is better)")
        
        # Show multi-step details if applicable
        if best_solution.get('is_multi_step', False):
            print("\n  📋 EXECUTION PLAN:")
            for step in best_solution['steps']:
                print(f"    {step['step']}. {step['action']} ({step['time']})")
                print(f"       → {step['reason']}")
    
    def _print_ai_explanation(self, explanation):
        """Print AI explanation"""
        print("\n💬 AI EXPLANATION FOR SECTION CONTROLLER:")
        print("-" * 70)
        print(explanation)
    
    def _print_energy_impact(self, best_solution, worst_solution, energy_saved):
        """Print energy impact"""
        print("\n⚡ ENERGY IMPACT:")
        print("-" * 70)
        print(f"  Best solution: {best_solution['energy_kwh']:.1f} kWh")
        print(f"  Worst solution: {worst_solution['energy_kwh']:.1f} kWh")
        print(f"  Energy saved: {energy_saved:.1f} kWh ({(energy_saved/worst_solution['energy_kwh']*100):.1f}% reduction)")
        print(f"  Equivalent to: Powering {energy_saved/5:.0f} homes for 1 hour")
        
        # Special callout for massive savings
        if energy_saved > 1000:
            print(f"\n  🔥 MASSIVE ENERGY SAVINGS! This is a killer feature for renewable energy domain!")
    
    def _print_expected_vs_actual(self, expected, actual):
        """Print expected vs actual comparison"""
        print("\n📈 EXPECTED vs ACTUAL:")
        print("-" * 70)
        print(f"  Expected energy savings: {expected['energy_saved_kwh']} kWh")
        print(f"  Actual energy savings: {actual['energy_saved_kwh']:.1f} kWh")
        
        if abs(actual['energy_saved_kwh'] - expected['energy_saved_kwh']) < 50:
            print(f"  ✓ Close match!")
        
        print(f"\n  Expected solution: {expected['recommended_solution']}")
        print(f"  Actual solution: {actual['recommended_solution']}")
    
    def run_interactive_demo(self):
        """Run interactive demo menu"""
        print("\n" + "="*70)
        print("NEURALRAIL - INTERACTIVE DEMO")
        print("="*70)
        print("AI-Assisted Railway Traffic Management System")
        print("For SIH 2024 - Renewable Energy Domain")
        print("="*70)
        
        while True:
            list_scenarios()
            
            print("\nOptions:")
            print("  1-5: Run specific scenario")
            print("  A: Run all scenarios")
            print("  R: Run recommended demo sequence")
            print("  Q: Quit")
            
            choice = input("\nYour choice: ").strip().upper()
            
            if choice == 'Q':
                print("\n👋 Thank you for using NeuralRail!")
                break
            
            elif choice == 'A':
                self.run_all_scenarios()
            
            elif choice == 'R':
                self.run_recommended_sequence()
            
            elif choice.isdigit() and 1 <= int(choice) <= 5:
                scenario = get_scenario_by_index(int(choice))
                self.run_scenario(scenario, verbose=True)
                input("\nPress Enter to continue...")
            
            else:
                print("⚠️  Invalid choice. Please try again.")
    
    def run_all_scenarios(self):
        """Run all scenarios in sequence"""
        print("\n" + "="*70)
        print("RUNNING ALL SCENARIOS")
        print("="*70)
        
        for i, scenario in enumerate(ALL_SCENARIOS, 1):
            print(f"\n\n{'='*70}")
            print(f"SCENARIO {i}/{len(ALL_SCENARIOS)}")
            print(f"{'='*70}")
            
            self.run_scenario(scenario, verbose=True)
            
            if i < len(ALL_SCENARIOS):
                input("\nPress Enter for next scenario...")
        
        print("\n" + "="*70)
        print("✓ ALL SCENARIOS COMPLETED!")
        print("="*70)
    
    def run_recommended_sequence(self):
        """Run scenarios in recommended demo order"""
        print("\n" + "="*70)
        print("RECOMMENDED DEMO SEQUENCE FOR JUDGES")
        print("="*70)
        print("Order: Simple → WOW → Multi-train → Flexibility → Complex")
        print("="*70)
        
        # Recommended order: 1, 3, 2, 5, 4
        recommended_order = [0, 2, 1, 4, 3]  # 0-indexed
        
        for i, scenario_index in enumerate(recommended_order, 1):
            scenario = ALL_SCENARIOS[scenario_index]
            
            print(f"\n\n{'='*70}")
            print(f"DEMO STEP {i}/{len(recommended_order)}: {scenario['name']}")
            print(f"{'='*70}")
            
            # Print judge talking points (if available)
            if 'judge_talking_points' in scenario:
                print("\n🎯 JUDGE TALKING POINTS:")
                for point in scenario['judge_talking_points']:
                    print(f"  • {point}")
            elif 'judge_points' in scenario:
                print("\n🎯 JUDGE POINTS:")
                for point in scenario['judge_points']:
                    print(f"  • {point}")
            
            input("\nPress Enter to run this scenario...")
            
            self.run_scenario(scenario, verbose=True)
            
            if i < len(recommended_order):
                input("\nPress Enter for next scenario...")
        
        print("\n" + "="*70)
        print("✓ DEMO SEQUENCE COMPLETED!")
        print("="*70)
        print("\nYou've demonstrated:")
        print("  ✓ Priority-based decision making")
        print("  ✓ Massive energy savings (1365 kWh!)")
        print("  ✓ Multi-train coordination")
        print("  ✓ System flexibility (different modes)")
        print("  ✓ Complex cascade prevention")
        print("\n🏆 Ready to impress the judges!")


if __name__ == "__main__":
    runner = ScenarioRunner()
    runner.run_interactive_demo()
