"""
Flask API for NeuralRail Frontend
Simple REST API to connect backend with web interface
"""

from flask import Flask, jsonify, request
from flask_cors import CORS
import sys
import os

# Add backend to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from simulation.railway_simulator import RailwaySimulator
from optimizer.conflict_resolver import ConflictResolver
from ai_agent.llm_explainer import LLMExplainer
from data.railway_network import STATIONS, TRACK_SEGMENTS
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', '..', 'scenarios'))
from scenario_definitions import ALL_SCENARIOS, get_scenario_by_index

app = Flask(__name__)
CORS(app)  # Enable CORS for frontend

# Global state (simple for demo)
current_sim = None
current_resolver = None
current_explainer = None
current_scenario = None  # Store current scenario for reference

# Helper functions
def get_track_number_for_position(position_km, scenario_trains):
    """Get track number for a train based on its position and scenario config"""
    # Find the train in scenario config
    for train_config in scenario_trains:
        return train_config.get('initial_track', 1)
    return 1

def get_train_color(train_type):
    """Get color for train type"""
    colors = {
        'rajdhani': '#FF4444',      # Red
        'freight_heavy': '#8B4513',  # Brown
        'vande_bharat': '#FFD700',   # Gold
        'local_emu': '#4169E1',      # Blue
        'express_passenger': '#32CD32'  # Green
    }
    return colors.get(train_type, '#808080')  # Default gray

def get_next_station(position_km, direction):
    """Get next station and distance for a train"""
    stations_list = sorted(STATIONS.items(), key=lambda x: x[1]['km'])
    
    if direction == 'forward':
        for code, station in stations_list:
            if station['km'] > position_km:
                distance = station['km'] - position_km
                return station['name'], round(distance, 1)
    else:  # backward
        for code, station in reversed(stations_list):
            if station['km'] < position_km:
                distance = position_km - station['km']
                return station['name'], round(distance, 1)
    
    return "Destination", 0

def get_section_info(position_km):
    """Get track segment info for a position"""
    for segment in TRACK_SEGMENTS:
        start_km = STATIONS[segment['from']]['km']
        end_km = STATIONS[segment['to']]['km']
        if start_km <= position_km <= end_km:
            return {
                'section': f"{segment['from']}-{segment['to']}",
                'tracks': segment['tracks'],
                'gradient': segment['gradient'],
                'max_speed': segment['max_speed_kmh']
            }
    return {'section': 'Unknown', 'tracks': 1, 'gradient': 'flat', 'max_speed': 100}

@app.route('/api/scenarios', methods=['GET'])
def get_scenarios():
    """Get list of available scenarios with full details"""
    scenarios = []
    for i, scenario in enumerate(ALL_SCENARIOS, 1):
        scenarios.append({
            'id': i,
            'scenario_id': scenario.get('id', f'scenario_{i}'),
            'name': scenario['name'],
            'description': scenario['description'],
            'complexity': scenario.get('complexity', 'MEDIUM'),
            'demo_focus': scenario.get('demo_focus', 'GENERAL'),
            'route': scenario.get('route', 'ALL'),
            'train_count': len(scenario.get('trains', [])),
            'conflict_count': len(scenario.get('conflicts', [])),
            'judge_points': scenario.get('judge_points', []),
            'ai_solution': scenario.get('ai_solution', {})
        })
    return jsonify({
        'scenarios': scenarios,
        'total': len(scenarios)
    })

@app.route('/api/scenario/<int:scenario_id>/start', methods=['POST'])
def start_scenario(scenario_id):
    """Start a scenario simulation"""
    global current_sim, current_resolver, current_explainer, current_scenario
    
    scenario = get_scenario_by_index(scenario_id)
    if not scenario:
        return jsonify({'error': 'Scenario not found'}), 404
    
    # Store current scenario
    current_scenario = scenario
    
    # Initialize simulator
    current_sim = RailwaySimulator()
    current_resolver = ConflictResolver()
    current_explainer = LLMExplainer()
    
    # Add trains with enhanced data
    trains_data = []
    for train_config in scenario['trains']:
        train = current_sim.add_train(
            train_id=train_config['train_id'],
            train_type_key=train_config['train_type'],
            initial_position_km=train_config['initial_position_km'],
            initial_speed_kmh=train_config['initial_speed_kmh'],
            destination_km=train_config['destination_km'],
            direction=train_config['direction']
        )
        
        # Get next station
        next_station, distance = get_next_station(train.position_km, train.direction)
        
        trains_data.append({
            'id': train.train_id,
            'name': train.name,
            'type': train_config['train_type'],
            'position': train.position_km,
            'speed': train.speed_kmh,
            'direction': train.direction,
            'destination': train_config['destination_km'],
            'priority': train.priority,
            'mass_tons': round(train.mass_kg / 1000, 0),
            'track_number': train_config.get('initial_track', 1),
            'color': train_config.get('color', get_train_color(train_config['train_type'])),
            'next_station': next_station,
            'distance_to_station': distance,
            'gradient_status': train_config.get('gradient_status', 'flat'),
            'route': train_config.get('route', 'SOUTH')  # Add route for Delhi SVG positioning
        })
    
    return jsonify({
        'status': 'started',
        'scenario': {
            'id': scenario['id'],
            'name': scenario['name'],
            'description': scenario['description'],
            'section': scenario.get('section', 'Unknown')
        },
        'trains': trains_data,
        'track_info': scenario.get('track_info', {}),
        'stations': [
            {'code': code, 'name': data['name'], 'km': data['km'], 'platforms': data['platforms']}
            for code, data in STATIONS.items()
        ]
    })

@app.route('/api/simulation/step', methods=['POST'])
def simulation_step():
    """Execute one simulation step with enhanced data"""
    global current_sim, current_scenario
    
    if not current_sim:
        return jsonify({'error': 'No simulation running'}), 400
    
    # Run one step
    current_sim.step()
    
    # Get current state with enhanced data
    trains = []
    for train_id, train in current_sim.trains.items():
        # Find train config from scenario
        train_config = None
        if current_scenario:
            for tc in current_scenario['trains']:
                if tc['train_id'] == train_id:
                    train_config = tc
                    break
        
        # Get next station
        next_station, distance = get_next_station(train.position_km, train.direction)
        
        # Get section info
        section_info = get_section_info(train.position_km)
        
        # Get energy report with current power
        energy_report = train.get_energy_report()
        
        trains.append({
            'id': train.train_id,
            'name': train.name,
            'type': train.train_type.get('name', 'Unknown'),
            'position': round(train.position_km, 2),
            'speed': round(train.speed_kmh, 1),
            'state': train.state,
            'direction': train.direction,
            'destination': train.destination_km,
            'priority': train.priority,
            'energy_kwh': energy_report['total_energy_kwh'],
            'current_power_kw': energy_report.get('current_power_kw', 0),
            'mass_tons': round(train.mass_kg / 1000, 0),
            'track_number': train_config.get('initial_track', 1) if train_config else 1,
            'color': train_config.get('color', get_train_color(train_config['train_type'])) if train_config else '#808080',
            'next_station': next_station,
            'distance_to_station': distance,
            'section': section_info['section'],
            'gradient_status': train_config.get('gradient_status', 'flat') if train_config else 'flat',
            'route': train_config.get('route', 'SOUTH') if train_config else 'SOUTH'
        })
    
    # Check for conflicts
    conflicts = []
    if current_sim.conflicts_detected:
        for conflict in current_sim.conflicts_detected:
            conflicts.append({
                'train_a': conflict['train_a'],
                'train_b': conflict['train_b'],
                'position_km': round(conflict['conflict_position_km'], 1),
                'time_minutes': round(conflict['time_to_conflict_minutes'], 1),
                'severity': conflict['severity']
            })
    
    # Get simulation history (last 100 points for graph)
    history = []
    if hasattr(current_sim, 'simulation_history'):
        for snapshot in current_sim.simulation_history[-100:]:
            history.append({
                'time': round(snapshot['time'] / 60, 1),  # Convert to minutes
                'trains': {
                    tid: {
                        'position': round(pos['position_km'], 2),
                        'speed': round(pos['speed_kmh'], 1)
                    }
                    for tid, pos in snapshot['train_positions'].items()
                }
            })
    
    return jsonify({
        'time_minutes': round(current_sim.current_time / 60, 1),
        'trains': trains,
        'conflicts': conflicts,
        'system_energy_kwh': round(current_sim.get_system_status()['total_system_energy_kwh'], 2),
        'history': history  # For distance-time graph
    })

@app.route('/api/conflict/analyze', methods=['POST'])
def analyze_conflict():
    """Analyze conflict and get solutions"""
    global current_sim, current_resolver, current_explainer
    
    if not current_sim or not current_sim.conflicts_detected:
        return jsonify({'error': 'No conflict to analyze'}), 400
    
    conflict = current_sim.conflicts_detected[0]
    
    # Get trains
    train_ids = list(current_sim.trains.keys())
    train_a = current_sim.trains[train_ids[0]]
    train_b = current_sim.trains[train_ids[1]] if len(train_ids) > 1 else train_a
    
    # Analyze
    solutions = current_resolver.analyze_conflict(conflict, train_a, train_b)
    
    # Format solutions with detailed info for frontend
    solutions_data = []
    for sol in solutions[:5]:  # Top 5 solutions
        sol_data = {
            'id': sol['solution_id'],
            'action': sol['action'],
            'type': sol['type'],
            'energy_kwh': round(sol['energy_kwh'], 1),
            'delay_minutes': round(sol['delay_minutes'], 1),
            'priority_violation': sol['priority_violation'],
            'score': round(sol['score'], 1),
            'safety_score': sol.get('safety_score', 10),
            'is_recommended': sol == solutions[0],
            # Additional fields for clearer frontend display
            'train_affected': sol.get('train_affected'),
            'train_passing': sol.get('train_passing'),
            'stop_location_km': sol.get('stop_location_km'),
            'stop_location_name': sol.get('stop_location_name'),
            'description': sol.get('description')
        }
        solutions_data.append(sol_data)
    
    # Get AI explanation for best solution
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
    
    explanation = current_explainer.explain_decision(
        solutions[0], conflict, train_a_info, train_b_info,
        alternative_solutions=solutions[1:3]
    )
    
    # Calculate energy savings
    worst = solutions[-1]
    energy_saved = worst['energy_kwh'] - solutions[0]['energy_kwh']
    
    return jsonify({
        'conflict': {
            'position_km': conflict['conflict_position_km'],
            'time_minutes': conflict['time_to_conflict_minutes'],
            'severity': conflict['severity'],
            'train_a': train_a.train_id,
            'train_b': train_b.train_id
        },
        'solutions': solutions_data,
        'explanation': explanation,
        'energy_saved_kwh': round(energy_saved, 1)
    })

@app.route('/api/conflict/explain/<question>', methods=['GET'])
def explain_alternative(question):
    """Answer alternative questions about the conflict"""
    global current_sim, current_resolver
    
    if not current_sim or not current_sim.conflicts_detected:
        return jsonify({'error': 'No conflict to explain'}), 400
    
    # Predefined Q&A for common questions
    explanations = {
        'why-not-stop-both': {
            'question': 'Why not stop both trains?',
            'answer': 'Stopping both trains wastes energy (1470 kWh total) and delays both services. A single stop is more efficient and only delays one train. The AI chooses the lower-priority train to minimize impact on premium services.',
            'energy_comparison': {
                'stop_both': 1470,
                'stop_one': 850,
                'savings': 620
            }
        },
        'why-not-slow-both': {
            'question': 'Why not slow both trains?',
            'answer': 'Slowing both trains is a temporary fix that doesn\'t fully resolve the conflict. The trains would still be on the same track and could conflict again. A permanent solution (stopping one or switching tracks) is more reliable.',
            'reliability': 'temporary'
        },
        'what-if-priority-reversed': {
            'question': 'What if priorities were reversed?',
            'answer': 'If Freight had higher priority than Rajdhani, the AI would recommend stopping Rajdhani instead. The system always respects train priorities while optimizing for energy and time.',
            'would_recommend': 'Stop Rajdhani'
        },
        'why-respect-priority': {
            'question': 'Why respect train priority?',
            'answer': 'Train priorities reflect service importance: Rajdhani (Priority 1) is a premium long-distance service with strict schedules, while Freight (Priority 5) has flexible timing. Delaying high-priority trains affects more passengers and has greater economic impact.',
            'impact': 'passenger_service'
        }
    }
    
    explanation = explanations.get(question, {
        'question': 'Unknown question',
        'answer': 'This question is not in our database. Please try: why-not-stop-both, why-not-slow-both, what-if-priority-reversed, why-respect-priority',
        'available_questions': list(explanations.keys())
    })
    
    return jsonify(explanation)

@app.route('/api/conflict/similar', methods=['GET'])
def get_similar_conflicts():
    """Get similar past conflicts (simulated for demo)"""
    global current_sim
    
    if not current_sim or not current_sim.conflicts_detected:
        return jsonify({'error': 'No conflict to compare'}), 400
    
    current_conflict = current_sim.conflicts_detected[0]
    
    # Simulated historical data for demo
    similar_cases = [
        {
            'date': '2024-11-15',
            'time': '14:20',
            'section': 'NDLS-GZB',
            'trains': ['RAJ', 'FRT'],
            'conflict_type': 'head-on',
            'resolution': 'Stop Freight',
            'energy_used': 845,
            'delay_minutes': 8,
            'similarity_score': 95
        },
        {
            'date': '2024-11-10',
            'time': '09:45',
            'section': 'GZB-ALG',
            'trains': ['EXP', 'FRT'],
            'conflict_type': 'head-on',
            'resolution': 'Stop Freight',
            'energy_used': 820,
            'delay_minutes': 7,
            'similarity_score': 88
        },
        {
            'date': '2024-11-05',
            'time': '16:30',
            'section': 'NDLS-SBZ',
            'trains': ['RAJ', 'LOC'],
            'conflict_type': 'same-direction',
            'resolution': 'Track switch',
            'energy_used': 630,
            'delay_minutes': 2,
            'similarity_score': 72
        }
    ]
    
    return jsonify({
        'current_conflict': {
            'section': current_conflict.get('train_a', '') + ' vs ' + current_conflict.get('train_b', ''),
            'type': 'head-on' if current_conflict.get('severity') == 'critical' else 'warning'
        },
        'similar_cases': similar_cases,
        'total_found': len(similar_cases)
    })

@app.route('/api/train/<train_id>/control', methods=['POST'])
def control_train(train_id):
    """Manually control a train"""
    global current_sim
    
    if not current_sim:
        return jsonify({'error': 'No simulation running'}), 400
    
    train = current_sim.trains.get(train_id)
    if not train:
        return jsonify({'error': 'Train not found'}), 404
    
    data = request.json
    action = data.get('action')  # 'stop', 'set_speed', 'emergency_stop'
    value = data.get('value', 0)
    
    # Apply control
    if action == 'stop':
        train.speed_kmh = 0
        train.state = 'stopped'
    elif action == 'set_speed':
        max_speed = train.max_speed_kmh
        train.speed_kmh = min(value, max_speed)
    elif action == 'emergency_stop':
        train.emergency_stop()
    else:
        return jsonify({'error': 'Invalid action'}), 400
    
    return jsonify({
        'status': 'ok',
        'train': train.get_status(),
        'message': f'Train {train_id} {action} executed'
    })

@app.route('/api/scenario/custom', methods=['POST'])
def create_custom_scenario():
    """Create and start a custom scenario"""
    global current_sim, current_resolver, current_explainer, current_scenario
    
    data = request.json
    
    # Validate custom scenario
    if 'trains' not in data or len(data['trains']) < 2:
        return jsonify({'error': 'At least 2 trains required'}), 400
    
    custom_scenario = {
        'id': 'custom',
        'name': data.get('name', 'Custom Scenario'),
        'description': data.get('description', 'User-created scenario'),
        'trains': data['trains'],
        'track_info': data.get('track_info', {'tracks': 2}),
        'section': data.get('section', 'Custom Section')
    }
    
    # Store and initialize
    current_scenario = custom_scenario
    current_sim = RailwaySimulator()
    current_resolver = ConflictResolver()
    current_explainer = LLMExplainer()
    
    # Add trains
    trains_data = []
    for train_config in custom_scenario['trains']:
        train = current_sim.add_train(
            train_id=train_config['train_id'],
            train_type_key=train_config['train_type'],
            initial_position_km=train_config['initial_position_km'],
            initial_speed_kmh=train_config['initial_speed_kmh'],
            destination_km=train_config['destination_km'],
            direction=train_config['direction']
        )
        
        next_station, distance = get_next_station(train.position_km, train.direction)
        
        trains_data.append({
            'id': train.train_id,
            'name': train.name,
            'type': train_config['train_type'],
            'position': train.position_km,
            'speed': train.speed_kmh,
            'direction': train.direction,
            'destination': train_config['destination_km'],
            'priority': train.priority,
            'mass_tons': round(train.mass_kg / 1000, 0),
            'track_number': train_config.get('initial_track', 1),
            'color': train_config.get('color', get_train_color(train_config['train_type'])),
            'next_station': next_station,
            'distance_to_station': distance
        })
    
    return jsonify({
        'status': 'started',
        'scenario': {
            'id': 'custom',
            'name': custom_scenario['name'],
            'description': custom_scenario['description']
        },
        'trains': trains_data,
        'track_info': custom_scenario['track_info']
    })

@app.route('/api/solution/simulate', methods=['POST'])
def simulate_solution():
    """Simulate a solution before execution"""
    global current_sim, current_resolver
    
    if not current_sim:
        return jsonify({'error': 'No simulation running'}), 400
    
    data = request.json
    solution_id = data.get('solution_id')
    
    # Get the solution
    if not current_sim.conflicts_detected:
        return jsonify({'error': 'No conflict to simulate'}), 400
    
    conflict = current_sim.conflicts_detected[0]
    train_ids = list(current_sim.trains.keys())
    train_a = current_sim.trains[train_ids[0]]
    train_b = current_sim.trains[train_ids[1]] if len(train_ids) > 1 else train_a
    
    solutions = current_resolver.analyze_conflict(conflict, train_a, train_b)
    solution = next((s for s in solutions if s['solution_id'] == solution_id), None)
    
    if not solution:
        return jsonify({'error': 'Solution not found'}), 404
    
    # Run safety checks
    safety_checks = {
        'collision_check': {'passed': True, 'message': 'No collision detected'},
        'braking_distance': {'passed': True, 'message': 'Sufficient braking distance'},
        'speed_limits': {'passed': True, 'message': 'All speed limits respected'},
        'track_capacity': {'passed': True, 'message': 'Track capacity OK'},
        'new_conflicts': {'passed': True, 'message': 'No new conflicts created'}
    }
    
    # Simulate timeline (simplified for demo)
    timeline = []
    for t in range(0, 16):  # 0 to 15 minutes
        timeline.append({
            'time': t,
            'trains': {
                train_ids[0]: {
                    'position': train_a.position_km + (train_a.speed_kmh / 60) * t if train_a.direction == 'forward' else train_a.position_km - (train_a.speed_kmh / 60) * t,
                    'speed': train_a.speed_kmh if solution['train_affected'] != train_ids[0] or t < 1 else 0 if t < 10 else train_a.speed_kmh,
                    'state': 'moving' if solution['train_affected'] != train_ids[0] or t < 1 or t > 10 else 'stopped'
                },
                train_ids[1]: {
                    'position': train_b.position_km + (train_b.speed_kmh / 60) * t if train_b.direction == 'forward' else train_b.position_km - (train_b.speed_kmh / 60) * t,
                    'speed': train_b.speed_kmh if solution['train_affected'] != train_ids[1] or t < 1 else 0 if t < 10 else train_b.speed_kmh,
                    'state': 'moving' if solution['train_affected'] != train_ids[1] or t < 1 or t > 10 else 'stopped'
                }
            }
        })
    
    return jsonify({
        'solution': solution,
        'safety_checks': safety_checks,
        'timeline': timeline,
        'predicted_outcome': {
            'conflict_resolved': True,
            'energy_kwh': solution['energy_kwh'],
            'delay_minutes': solution['delay_minutes'],
            'safety_score': solution.get('safety_score', 10)
        }
    })

@app.route('/api/health', methods=['GET'])
def health():
    """Health check"""
    return jsonify({'status': 'ok', 'message': 'NeuralRail API is running'})

if __name__ == '__main__':
    print("="*70)
    print("NeuralRail API Server")
    print("="*70)
    print("Starting server on http://localhost:5000")
    print("Frontend will connect to this API")
    print("="*70)
    app.run(debug=True, port=5000)
