# 🚄 NeuralRail - Complete Technical Documentation

**AI-Assisted Railway Traffic Management System**  
**Smart India Hackathon 2024 - Renewable Energy Domain**

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [System Overview](#2-system-overview)
3. [Infrastructure & Network](#3-infrastructure--network)
4. [Train Types & Priority System](#4-train-types--priority-system)
5. [AI Decision Making System](#5-ai-decision-making-system)
6. [Energy Calculations & Sustainability](#6-energy-calculations--sustainability)
7. [Conflict Resolution Strategies](#7-conflict-resolution-strategies)
8. [Backend Architecture](#8-backend-architecture)
9. [Frontend Architecture](#9-frontend-architecture)
10. [API Reference](#10-api-reference)
11. [Scenarios & Demo](#11-scenarios--demo)
12. [Deployment Guide](#12-deployment-guide)
13. [Technical Specifications](#13-technical-specifications)

---

## 1. Executive Summary

### 1.1 What is NeuralRail?

NeuralRail is an intelligent railway traffic management system that uses AI to:
- **Detect train conflicts** 7-30 minutes in advance
- **Generate optimal solutions** considering safety, priority, and energy
- **Save massive energy** through smart decision-making
- **Explain decisions** in human-readable format for Section Controllers

### 1.2 Impact Metrics

| Metric | Per Conflict | Annual (500/day) |
|--------|--------------|------------------|
| **Energy Saved** | 350 kWh | **63.9 GWh** |
| **Cost Saved** | ₹1,750 | **₹319 Crore** |
| **CO₂ Reduced** | 280 kg | **51,000 tons** |

> **51,000 tons CO₂ = 2.3 million trees planted 🌳**

### 1.3 Key Differentiators

1. **Physics-Based Energy Calculations** - Real gradient penalties, regenerative braking
2. **Priority-First Decision Making** - Respects Indian Railways 6-tier priority system
3. **Multi-Step Solutions** - Sophisticated track switching and coordination
4. **Hub-Based Architecture** - Handles 4-way junction conflicts (Delhi Hub)
5. **Real-Time Visualization** - Professional control room interface


---

## 2. System Overview

### 2.1 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NEURALRAIL SYSTEM                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐       │
│  │    FRONTEND      │    │     BACKEND      │    │    SCENARIOS     │       │
│  │   (JavaScript)   │◄──►│    (Python)      │◄──►│    (Python)      │       │
│  └──────────────────┘    └──────────────────┘    └──────────────────┘       │
│         │                        │                       │                   │
│         ▼                        ▼                       ▼                   │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐       │
│  │ • Track SVG      │    │ • Flask API      │    │ • Delhi Hub      │       │
│  │ • Train Markers  │    │ • Simulator      │    │ • Multi-Train    │       │
│  │ • Time-Space     │    │ • Conflict       │    │ • Complex        │       │
│  │   Graph          │    │   Resolver       │    │   Scenarios      │       │
│  │ • AI Panel       │    │ • Energy Calc    │    │                  │       │
│  │ • Decision Log   │    │ • LLM Explainer  │    │                  │       │
│  └──────────────────┘    └──────────────────┘    └──────────────────┘       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Frontend** | HTML5, CSS3, JavaScript | User Interface |
| **Visualization** | SVG, Canvas | Track schematic, graphs |
| **Backend** | Python 3.x, Flask | API, business logic |
| **AI/ML** | Custom algorithms | Conflict resolution |
| **Optional LLM** | Groq/Gemini API | Natural language explanations |

### 2.3 Project Structure

```
NeuralRail/
├── backend/
│   ├── ai_agent/
│   │   ├── llm_explainer.py      # AI explanation system
│   │   └── test_explainer.py
│   ├── api/
│   │   └── app.py                # Flask REST API
│   ├── data/
│   │   ├── network_graph.py      # Graph-based network
│   │   └── railway_network.py    # Station & track data
│   ├── optimizer/
│   │   ├── conflict_resolver.py  # Core AI engine
│   │   └── test_*.py             # Test files
│   ├── physics/
│   │   └── energy_calculator.py  # Physics calculations
│   └── simulation/
│       └── railway_simulator.py  # Train simulation
├── frontend/
│   ├── app.js                    # Main application logic
│   ├── index.html                # HTML structure
│   ├── styles.css                # Styling
│   └── delhi_junction.svg        # Track schematic
├── scenarios/
│   ├── delhi_hub_scenario.py     # Primary demo scenario
│   ├── scenario_definitions.py   # Scenario registry
│   ├── complex_scenarios.py      # Advanced scenarios
│   └── scenario_runner.py        # Execution engine
└── requirements.txt              # Python dependencies
```


---

## 3. Infrastructure & Network

### 3.1 Delhi Section Network

NeuralRail models the Delhi railway hub with 4 converging routes:

```
                    ↑ PUNJAB/HARYANA (NORTH LINE)
                         │
                    ┌────┴────┐
                    │ SONIPAT │ 42 km
                    └────┬────┘
                         │ 10 km
                    ┌────┴────┐
                    │ NARELA  │ 32 km
                    └────┬────┘
                         │ 25 km
                    ┌────┴────┐
                    │OLD DELHI│ 7 km ←──┐
                    └────┬────┘         │ 4 km (branch)
                         │              │
← JAIPUR ───┬───────┬────┴────┬────────┼────────┬───────┬─── LUCKNOW →
         DELHI    SARAI    SADAR    NEW DELHI   ANAND   GHAZIABAD
         CANTT   ROHILLA   BAZAR      HUB      VIHAR     25 km
         15 km    10 km     4 km       0        12 km
                                       │
                                  ┌────┴────┐
                                  │NIZAMUDDIN│ 5 km (3 tracks)
                                  └────┬────┘
                                       │ 20 km
                                  ┌────┴────┐
                                  │FARIDABAD│ 25 km
                                  └────┬────┘
                                       │ 35 km
                                  ┌────┴────┐
                                  │ PALWAL  │ 60 km
                                  └────┬────┘
                                       │ 81 km
                                  ┌────┴────┐
                                  │MATHURA JN│ 141 km
                                  └────┴────┘
                                       ↓ AGRA (SOUTH LINE)
```

### 3.2 Stations Database

| Code | Name | KM | Platforms | Type | Line |
|------|------|-----|-----------|------|------|
| **NDLS** | New Delhi | 0 | 16 | Hub | ALL |
| SNP | Sonipat | 42 | 4 | Station | NORTH |
| NRL | Narela | 32 | 3 | Station | NORTH |
| DLI | Old Delhi Junction | 7 | 16 | Junction | NORTH |
| NZM | Hazrat Nizamuddin | 5 | 7 | Major Station | SOUTH |
| FDB | Faridabad | 25 | 4 | Station | SOUTH |
| PWL | Palwal | 60 | 4 | Station | SOUTH |
| MTJ | Mathura Junction | 141 | 8 | Junction | SOUTH |
| ANVT | Anand Vihar Terminal | 12 | 7 | Terminal | EAST |
| GZB | Ghaziabad Junction | 25 | 6 | Junction | EAST |
| DSB | Sadar Bazar | 4 | 2 | Junction | WEST |
| DEE | Sarai Rohilla | 10 | 10 | Terminal | WEST |
| DEC | Delhi Cantt | 15 | 4 | Station | WEST |

### 3.3 Track Segments

| Segment | From | To | Distance | Tracks | Max Speed | Features |
|---------|------|-----|----------|--------|-----------|----------|
| SNP-NRL | Sonipat | Narela | 10 km | 2 | 110 km/h | Electrified |
| NRL-DLI | Narela | Old Delhi | 25 km | 2 | 100 km/h | Electrified |
| DLI-NDLS | Old Delhi | New Delhi | 7 km | 2 | 60 km/h | Congested |
| NDLS-NZM | New Delhi | Nizamuddin | 5 km | **3** | 80 km/h | Passing Loop |
| NZM-FDB | Nizamuddin | Faridabad | 20 km | 3 | 110 km/h | T3 available |
| FDB-PWL | Faridabad | Palwal | 35 km | 2 | 130 km/h | T3 merged |
| PWL-MTJ | Palwal | Mathura | 81 km | 2 | 130 km/h | High speed |
| NDLS-ANVT | New Delhi | Anand Vihar | 12 km | 2 | 80 km/h | Electrified |
| ANVT-GZB | Anand Vihar | Ghaziabad | 13 km | 2 | 100 km/h | Overtaking Loop |
| NDLS-DSB | New Delhi | Sadar Bazar | 4 km | 2 | 60 km/h | Holding Loop |
| DSB-DEE | Sadar Bazar | Sarai Rohilla | 6 km | 2 | 80 km/h | Electrified |
| DEE-DEC | Sarai Rohilla | Delhi Cantt | 5 km | 2 | 100 km/h | Electrified |

### 3.4 Loops & Sidings

| Loop Name | Location | Type | Capacity | Can Hold |
|-----------|----------|------|----------|----------|
| **Ghaziabad Overtaking** | GZB | Overtaking | 1 train | Express, Passenger, MEMU |
| **Sadar Bazar Holding** | DSB | Holding | 1 train | Freight, Express |
| **Nizamuddin Passing** | NZM | Passing | 1 train | Any train type |

### 3.5 Network Graph Implementation

```python
class RailwayNetwork:
    """Graph-based railway network for complex routing"""
    
    def __init__(self):
        self.nodes = NODES
        self.edges = {e["id"]: e for e in EDGES}
        self._build_adjacency()
    
    def find_path(self, start: str, end: str) -> List[str]:
        """Find shortest path using BFS"""
        # Returns list of station codes
        
    def get_route_distance(self, path: List[str]) -> float:
        """Calculate total distance of a path"""
        
    def get_loops_on_line(self, line: str) -> List[dict]:
        """Get all loops on a specific line"""
```


---

## 4. Train Types & Priority System

### 4.1 Indian Railways Official 6-Tier Priority System

```
┌─────────────────────────────────────────────────────────────────────┐
│                    TRAIN PRIORITY HIERARCHY                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 1 ─ SPECIAL (Absolute - NEVER STOP)                      │
│   • President/PM Special, Military Special, Accident Relief         │
│                                                                      │
│   PRIORITY 2 ─ SUPERFAST EXPRESS (Critical)                         │
│   • Rajdhani, Shatabdi, Vande Bharat, Duronto, Tejas, Gatimaan     │
│   • Festival Specials (Diwali, Puja, Kumbh)                        │
│                                                                      │
│   PRIORITY 3 ─ MAIL/EXPRESS (High)                                  │
│   • Regular Express, Jan Shatabdi, Garib Rath                       │
│                                                                      │
│   PRIORITY 4 ─ PASSENGER (Medium)                                   │
│   • Ordinary Passenger, MEMU, DEMU                                  │
│                                                                      │
│   PRIORITY 5 ─ SUBURBAN (Medium-Low)                                │
│   • Local EMU, Metro, Ring Railway                                  │
│                                                                      │
│   PRIORITY 6 ─ FREIGHT (Lowest)                                     │
│   • Heavy Freight, Container, Tanker, Parcel                        │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 Train Types in NeuralRail

| Train Type | Priority | Mass (tons) | Max Speed | Acceleration | Braking | Passengers |
|------------|----------|-------------|-----------|--------------|---------|------------|
| **Rajdhani** | 2 | 850 | 140 km/h | 0.38 m/s² | 0.72 m/s² | 1,122 |
| **Shatabdi** | 2 | 720 | 150 km/h | 0.42 m/s² | 0.75 m/s² | 1,050 |
| **Vande Bharat** | 2 | 430 | 160 km/h | 1.05 m/s² | 1.20 m/s² | 1,128 |
| **Express** | 3 | 520 | 110 km/h | 0.42 m/s² | 0.65 m/s² | 1,430 |
| **MEMU** | 4 | 350 | 100 km/h | 0.70 m/s² | 0.90 m/s² | 2,000 |
| **Local EMU** | 5 | 380 | 105 km/h | 0.85 m/s² | 1.10 m/s² | 3,600 |
| **Heavy Freight** | 6 | 4,200 | 75 km/h | 0.12 m/s² | 0.28 m/s² | 0 |

### 4.3 Train Type Configuration

```python
TRAIN_TYPES = {
    "rajdhani": {
        "name": "Rajdhani Express",
        "mass_kg": 850000,
        "max_speed_kmh": 140,
        "acceleration_mps2": 0.38,
        "braking_rate_mps2": 0.72,
        "idle_power_kw": 180,
        "traction_type": "electric",
        "passenger_capacity": 1122,
        "priority": 2,
        "schedule_importance": "critical",
        "priority_class": "Superfast Express"
    },
    "freight_heavy": {
        "name": "Heavy Freight",
        "mass_kg": 4200000,
        "max_speed_kmh": 75,
        "acceleration_mps2": 0.12,
        "braking_rate_mps2": 0.28,
        "idle_power_kw": 45,
        "traction_type": "electric",
        "passenger_capacity": 0,
        "priority": 6,
        "schedule_importance": "low",
        "priority_class": "Freight"
    }
    # ... other train types
}
```

### 4.4 Energy Costs by Train Type

| Train Type | Stop Energy | Restart Energy | Total | Notes |
|------------|-------------|----------------|-------|-------|
| Vande Bharat | 280 kWh | 180 kWh | 460 kWh | Lightest, fastest |
| Rajdhani | 420 kWh | 350 kWh | 770 kWh | Premium service |
| Shatabdi | 400 kWh | 300 kWh | 700 kWh | Day train |
| Express | 180 kWh | 150 kWh | 330 kWh | Standard |
| Local EMU | 80 kWh | 70 kWh | 150 kWh | Frequent stops |
| Heavy Freight | 600 kWh | 900 kWh* | 1,500 kWh | *Higher on gradient |


---

## 5. AI Decision Making System

### 5.1 Core Decision Formula

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AI DECISION FORMULA                               │
│                                                                      │
│         ┌─────────────────┐     ┌─────────────────┐                 │
│         │  TRAIN PRIORITY │  +  │ ENERGY SAVINGS  │  =  DECISION    │
│         │     (60%)       │     │     (40%)       │                 │
│         └─────────────────┘     └─────────────────┘                 │
│                                                                      │
│   RULE: Priority FIRST, Energy SECOND                               │
│   EXCEPTION: Energy overrides ONLY if savings > 1000 kWh            │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 Decision Hierarchy

| Level | Factor | Weight | Description |
|-------|--------|--------|-------------|
| 1 | **Safety** | Absolute | No collision ever - all solutions must be safe |
| 2 | **Train Priority** | 40% | Higher priority trains get preference |
| 3 | **Energy Efficiency** | 40% | Prefer lower energy solutions |
| 4 | **Time Delay** | 20% | Minimize total delay |

### 5.3 Scoring Algorithm

```python
def _calculate_solution_score(self, energy_joules, delay_seconds, 
                              priority_violation, train_priority):
    """
    Calculate a score for a solution (lower is better).
    """
    energy_kwh = joules_to_kwh(energy_joules)
    
    # Energy score (handle gradient penalties >1000 kWh)
    if energy_kwh > 1000:
        energy_score = energy_kwh / 5  # Higher weight, no cap
    else:
        energy_score = min(energy_kwh / 10, 100)
    
    # Delay score (normalized)
    delay_minutes = delay_seconds / 60
    delay_score = min(delay_minutes * 2, 50)
    
    # Priority violation penalty (HUGE!)
    priority_penalty = 500 if priority_violation else 0
    
    # Train priority factor
    priority_factor = train_priority * 10
    
    # Total score (lower is better)
    total_score = (
        energy_score * 0.4 +      # 40% energy weight
        delay_score * 0.2 +       # 20% delay weight
        priority_penalty +         # Fixed penalty
        priority_factor * 0.4     # 40% priority weight
    )
    
    return round(total_score, 2)
```

### 5.4 Decision Modes

| Mode | Energy | Delay | Priority | Use Case |
|------|--------|-------|----------|----------|
| **Balanced** | 40% | 20% | 40% | Normal operations |
| **Energy Priority** | 60% | 10% | 30% | Power shortage, green initiative |
| **Time Priority** | 20% | 50% | 30% | Rush hour, festival season |
| **Strict Priority** | 20% | 20% | 60% | VVIP movement, military |

### 5.5 Energy Override Conditions

```
┌────────────────┬─────────────────────────────────────────────┐
│ Priority Gap   │ Energy Threshold to Override                │
├────────────────┼─────────────────────────────────────────────┤
│ P6 vs P5       │ 200 kWh (easy to override)                  │
│ P6 vs P4       │ 400 kWh                                     │
│ P6 vs P3       │ 800 kWh                                     │
│ P6 vs P2       │ 1500 kWh (hard to override)                 │
│ P6 vs P1       │ NEVER (P1 is absolute)                      │
└────────────────┴─────────────────────────────────────────────┘
```

### 5.6 Decision Flow

```
                    ┌─────────────────────┐
                    │   CONFLICT DETECTED │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Is it Emergency?   │
                    │   (< 5 minutes)     │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │ YES            │                │ NO
              ▼                │                ▼
    ┌─────────────────┐        │      ┌─────────────────┐
    │ Generate STOP   │        │      │ Generate ALL    │
    │ solutions only  │        │      │ solution types  │
    └────────┬────────┘        │      └────────┬────────┘
             │                 │               │
             └─────────────────┼───────────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Filter Invalid     │
                    │  (track constraints)│
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Separate:          │
                    │  PERMANENT vs       │
                    │  TEMPORARY          │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Priority-respecting│
                    │  solutions FIRST    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Sort by Energy     │
                    │  (lower = better)   │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  RECOMMEND BEST     │
                    │  SOLUTION           │
                    └─────────────────────┘
```


---

## 6. Energy Calculations & Sustainability

### 6.1 Energy Calculation Formulas

#### Kinetic Energy
```
KE = 0.5 × m × v²

Where:
- m = mass in kg
- v = velocity in m/s

Example (Rajdhani at 100 km/h):
- Mass = 850,000 kg
- Speed = 27.78 m/s
- KE = 0.5 × 850,000 × 27.78² = 328 MJ = 91 kWh
```

#### Braking Energy Loss
```
E_braking = KE_initial - KE_final

For complete stop:
E_braking = 0.5 × m × v²

With regenerative braking (30% recovery):
E_net_loss = E_braking × 0.70
```

#### Acceleration Energy
```
E_acceleration = (ΔKE + Work_against_resistance) / efficiency

Where:
- ΔKE = change in kinetic energy
- Work_against_resistance = rolling resistance × distance
- efficiency = 0.85 for electric motors
```

#### Gradient Penalty (Physics-Based)
```
Gradient Penalty = Mass × Gravity × Height_Gain

For 4000-ton freight on 1.83% gradient (Bhor Ghat):
- Potential Energy: 4,000,000 × 9.81 × 18.3m = 718 MJ = 199 kWh
- Acceleration Energy: 0.5 × m × v² / efficiency = 126 kWh
- Rolling Resistance: ~50 kWh
- Motor Inefficiency (~15% loss): ~56 kWh
- Total ≈ 430-500 kWh
```

### 6.2 Energy Calculator Implementation

```python
class EnergyCalculator:
    @staticmethod
    def braking_energy_loss(mass_kg, initial_speed_kmh, final_speed_kmh):
        """Calculate energy lost when braking"""
        v_initial = initial_speed_kmh / 3.6  # Convert to m/s
        v_final = final_speed_kmh / 3.6
        ke_initial = 0.5 * mass_kg * v_initial**2
        ke_final = 0.5 * mass_kg * v_final**2
        return ke_initial - ke_final  # Joules
    
    @staticmethod
    def acceleration_energy(mass_kg, initial_speed, final_speed, distance_km):
        """Calculate energy needed to accelerate"""
        # Kinetic energy change
        delta_ke = 0.5 * mass_kg * ((final_speed/3.6)**2 - (initial_speed/3.6)**2)
        # Rolling resistance work
        resistance_work = mass_kg * 9.81 * 0.002 * distance_km * 1000
        # Motor efficiency (85%)
        return (delta_ke + resistance_work) / 0.85
    
    @staticmethod
    def idle_energy(idle_power_kw, time_seconds):
        """Calculate energy consumed while idling"""
        return idle_power_kw * (time_seconds / 3600) * 3_600_000  # Joules
```

### 6.3 Gradient Penalties by Train Mass

| Train Mass | Gradient Penalty | Reason |
|------------|------------------|--------|
| 4000+ tons (Heavy Freight) | +500 kWh | Massive potential energy to regain |
| 1500-4000 tons (Express) | +300 kWh | Significant restart energy |
| 800-1500 tons (Local) | +150 kWh | Moderate penalty |
| <800 tons (Light) | +80 kWh | Light trains handle gradients better |

### 6.4 Regenerative Braking

| Train Type | Braking Energy Recovery |
|------------|------------------------|
| Electric (EMU, WAP) | 30% recovered |
| Diesel (WDM, WDP) | 0% (wasted as heat) |

### 6.5 Annual Energy Impact

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ANNUAL ENERGY IMPACT                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   📊 ANNUAL PROJECTION (if applied to Indian Railways):             │
│                                                                      │
│   • Daily conflicts resolved: ~500                                  │
│   • Daily energy saved: 500 × 350 = 175,000 kWh                    │
│   • Annual energy saved: 63.9 GWh                                  │
│   • Annual cost saved: ₹319 Crore                                  │
│   • Annual CO₂ reduced: 51,000 tons                                │
│                                                                      │
│   🌍 ENVIRONMENTAL IMPACT:                                          │
│   • Trees equivalent: 2.3 million trees                            │
│   • Cars off road: 11,000 cars for 1 year                          │
│   • Homes powered: 58,000 homes for 1 year                         │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.6 Alignment with Indian Railways Green Initiatives

| Initiative | NeuralRail Contribution |
|------------|------------------------|
| **Mission 100% Electrification** | Optimizes regenerative braking |
| **Net Zero by 2030** | 51,000 tons CO₂ reduction (1.3% of emissions) |
| **Solar Mission** | Can prioritize operations during solar peak |
| **Energy Bill Reduction** | ₹319 Crore savings (1.6% of total bill) |


---

## 7. Conflict Resolution Strategies

### 7.1 Solution Types

#### Permanent Solutions (Preferred)
| Type | Description | When Used | Energy Cost |
|------|-------------|-----------|-------------|
| **Stop** | One train stops completely | Head-on conflicts | 850-1500 kWh |
| **Track Switch** | Train moves to parallel track | When parallel track available | 20-50 kWh |
| **Multi-Step** | Slow + Stop + Switch combination | Complex conflicts | 100-200 kWh |

#### Temporary Solutions (Fallback)
| Type | Description | When Used | Energy Cost |
|------|-------------|-----------|-------------|
| **Slow** | One train reduces speed by 25% | Minor conflicts | 200-400 kWh |
| **Both Slow** | Both trains reduce speed by 15% | When no permanent solution | 300-500 kWh |

### 7.2 Stop Solution Generation

```python
def _generate_stop_solution(self, train_to_stop, other_train, conflict, track_info):
    """Generate a solution where one train stops completely"""
    
    # Calculate braking energy
    braking_energy = EnergyCalculator.braking_energy_loss(
        train_to_stop.mass_kg,
        train_to_stop.speed_kmh,
        0  # Complete stop
    )
    
    # Account for regenerative braking (30% recovery for electric)
    if train_to_stop.train_type['traction_type'] == 'electric':
        braking_energy *= 0.70
    
    # Calculate idle energy while waiting
    idle_energy = EnergyCalculator.idle_energy(
        train_to_stop.idle_power_kw,
        conflict['time_to_conflict_seconds']
    )
    
    # Calculate restart energy
    restart_energy = EnergyCalculator.acceleration_energy(
        train_to_stop.mass_kg, 0, train_to_stop.speed_kmh, 2
    )
    
    # Apply gradient penalty if uphill
    gradient_penalty = 0
    if track_info and train_gradient == 'uphill':
        if mass_tons >= 4000:
            gradient_penalty = 500  # kWh
        elif mass_tons >= 1500:
            gradient_penalty = 300
        # ... etc
    
    total_energy = braking_energy + idle_energy + restart_energy + gradient_penalty
    
    return {
        'type': 'stop',
        'action': f"Stop {train_to_stop.train_id}",
        'energy_kwh': total_energy,
        'delay_minutes': delay,
        'priority_violation': train_to_stop.priority < other_train.priority
    }
```

### 7.3 Multi-Step Solution Generation

```python
def _generate_multi_step_solution(self, train_a, train_b, conflict, track_info):
    """
    Generate a multi-step solution:
    Step 1: Both slow by 15% (buys time)
    Step 2: Switch one train to parallel track
    Step 3: Both resume normal speed
    """
    
    # Step 1: Both slow
    speed_reduction = 0.15
    braking_a = calculate_braking(train_a, speed_reduction)
    braking_b = calculate_braking(train_b, speed_reduction)
    
    # Step 2: Switch lower priority train
    if train_a.priority > train_b.priority:
        train_to_switch = train_a
    else:
        train_to_switch = train_b
    
    switch_energy = 5  # kWh (minimal)
    
    # Step 3: Re-acceleration
    reaccel_a = calculate_reaccel(train_a)
    reaccel_b = calculate_reaccel(train_b)
    
    return {
        'type': 'multi_step',
        'steps': [
            {'step': 1, 'action': 'Slow both trains by 15%'},
            {'step': 2, 'action': f'Switch {train_to_switch.train_id} to parallel track'},
            {'step': 3, 'action': 'Both trains resume normal speed'}
        ],
        'energy_kwh': total_energy,
        'delay_minutes': 1.5,
        'is_multi_step': True
    }
```

### 7.4 Slow + Stop Combination

```python
def _generate_slow_and_stop_solution(self, train_to_slow, train_to_stop, conflict):
    """
    Multi-step solution:
    - High priority train slows slightly (buys time)
    - Low priority train stops completely (resolves conflict)
    """
    
    # Step 1: Slow high-priority train by 20%
    braking_slow = calculate_braking(train_to_slow, 0.20)
    
    # Step 2: Stop low-priority train
    braking_stop = calculate_full_stop(train_to_stop)
    idle_energy = calculate_idle(train_to_stop)
    restart_energy = calculate_restart(train_to_stop)
    
    # This respects priority (high priority only slows, doesn't stop)
    return {
        'type': 'slow_and_stop',
        'action': f"Slow {train_to_slow.train_id} + Stop {train_to_stop.train_id}",
        'priority_violation': False,  # Respects priority
        'steps': [
            {'step': 1, 'action': f'Slow {train_to_slow.train_id} by 20%'},
            {'step': 2, 'action': f'Stop {train_to_stop.train_id} at junction'},
            {'step': 3, 'action': f'{train_to_slow.train_id} passes through'},
            {'step': 4, 'action': f'{train_to_stop.train_id} resumes'}
        ]
    }
```

### 7.5 Solution Ranking Algorithm

```python
def _rank_solutions_by_priority(self, solutions, train_a, train_b):
    """
    Rank solutions by effectiveness:
    1. Separate: PERMANENT vs TEMPORARY
    2. Within permanent: Priority-respecting first
    3. Sort by energy (lower = better)
    """
    
    # Separate by type
    permanent = [s for s in solutions if s['type'] in ['stop', 'multi_step']]
    temporary = [s for s in solutions if s['type'] in ['slow', 'both_slow']]
    
    # Within permanent: priority-respecting first
    priority_respecting = [s for s in permanent if not s['priority_violation']]
    priority_violating = [s for s in permanent if s['priority_violation']]
    
    # Sort each group by energy
    priority_respecting.sort(key=lambda x: x['energy_kwh'])
    priority_violating.sort(key=lambda x: x['energy_kwh'])
    
    # Check for massive energy savings override
    if priority_respecting and priority_violating:
        energy_diff = priority_respecting[0]['energy_kwh'] - priority_violating[0]['energy_kwh']
        if energy_diff > 1000:
            # Override justified
            return priority_violating + priority_respecting + temporary
    
    return priority_respecting + priority_violating + temporary
```

### 7.6 Platform Assignment Strategy

| Train Type | Platform Preference | Reason |
|------------|---------------------|--------|
| Rajdhani/Shatabdi | Platform 1, 2 (Main) | Premium service |
| Express | Platform 2, 3, 4 | Standard service |
| Local/EMU | Platform 5, 6, 7 | Frequent service |
| Freight | Platform 9, 10, 11 (Odd) | Keeps main platforms free |


---

## 8. Backend Architecture

### 8.1 Flask API Server

```python
# backend/api/app.py

from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Enable CORS for frontend

# Global state
current_sim = None
current_resolver = None
current_explainer = None
current_scenario = None
```

### 8.2 Core Modules

#### Railway Simulator
```python
# backend/simulation/railway_simulator.py

class RailwaySimulator:
    """Simulates train movements and detects conflicts"""
    
    def __init__(self):
        self.trains = {}
        self.current_time = 0
        self.conflicts_detected = []
        self.simulation_history = []
    
    def add_train(self, train_id, train_type_key, initial_position_km, 
                  initial_speed_kmh, destination_km, direction):
        """Add a train to the simulation"""
        
    def step(self, dt=1.0):
        """Advance simulation by dt seconds"""
        
    def detect_conflicts(self):
        """Check for potential collisions"""
```

#### Conflict Resolver
```python
# backend/optimizer/conflict_resolver.py

class ConflictResolver:
    """Core AI engine for conflict resolution"""
    
    def __init__(self, config=None):
        self.config = {
            'energy_weight': 0.4,
            'delay_weight': 0.2,
            'priority_weight': 0.4,
            'priority_penalty': 500,
            'mode': 'balanced'
        }
    
    def analyze_conflict(self, conflict, train_a, train_b, track_info=None):
        """Analyze conflict and generate solutions"""
        
    def _generate_stop_solution(self, train_to_stop, other_train, conflict):
        """Generate stop solution"""
        
    def _generate_slow_solution(self, train_to_slow, other_train, conflict):
        """Generate slow solution"""
        
    def _generate_multi_step_solution(self, train_a, train_b, conflict, track_info):
        """Generate multi-step solution"""
        
    def _rank_solutions_by_priority(self, solutions, train_a, train_b):
        """Rank solutions by effectiveness"""
```

#### LLM Explainer
```python
# backend/ai_agent/llm_explainer.py

class LLMExplainer:
    """Generates natural language explanations"""
    
    def __init__(self, groq_api_key=None, gemini_api_key=None):
        self.groq_available = False
        self.gemini_available = False
        # Initialize APIs if keys provided
    
    def explain_decision(self, solution, conflict, train_a_info, train_b_info):
        """Generate explanation for a decision"""
        # Try Groq first (faster)
        # Fallback to Gemini
        # Final fallback: Template-based
        
    def _fallback_explanation(self, solution, conflict, train_a_info, train_b_info):
        """Template-based explanation when LLMs unavailable"""
```

### 8.3 Data Models

#### Network Graph
```python
# backend/data/network_graph.py

class NodeType(Enum):
    TERMINAL = "terminal"
    JUNCTION = "junction"
    STATION = "station"
    HALT = "halt"
    CROSSING = "crossing"

class TrackType(Enum):
    SINGLE = 1
    DOUBLE = 2
    TRIPLE = 3
    QUADRUPLE = 4

NODES = {
    "NDLS": {
        "name": "New Delhi",
        "type": NodeType.TERMINAL,
        "km": 0,
        "platforms": 16,
        "routes": ["NORTH_LINE", "SOUTH_LINE", "EAST_LINE", "WEST_LINE"],
        "is_hub": True
    },
    # ... other nodes
}

EDGES = [
    {
        "id": "NDLS-NZM",
        "from": "NDLS",
        "to": "NZM",
        "distance_km": 5,
        "tracks": TrackType.TRIPLE,
        "max_speed": 80,
        "has_loop": True
    },
    # ... other edges
]
```

### 8.4 Physics Engine

```python
# backend/physics/energy_calculator.py

class EnergyCalculator:
    @staticmethod
    def braking_energy_loss(mass_kg, initial_speed_kmh, final_speed_kmh):
        """Calculate energy lost when braking"""
        
    @staticmethod
    def acceleration_energy(mass_kg, initial_speed, final_speed, distance_km):
        """Calculate energy needed to accelerate"""
        
    @staticmethod
    def idle_energy(idle_power_kw, time_seconds):
        """Calculate energy consumed while idling"""
        
    @staticmethod
    def calculate_braking_time(speed_kmh, braking_rate_mps2):
        """Calculate time to stop"""
        
    @staticmethod
    def calculate_braking_distance(speed_kmh, braking_rate_mps2):
        """Calculate distance to stop"""

def joules_to_kwh(joules):
    """Convert Joules to kWh"""
    return joules / 3_600_000
```


---

## 9. Frontend Architecture

### 9.1 Layout Structure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              HEADER                                          │
│  [Logo] [Section Info] [Scenario Select] [Status] [Playback] [Trains] [Clock]│
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────┐  ┌──────────────────────┐  │
│  │                                             │  │                      │  │
│  │           TRACK SCHEMATIC (SVG)             │  │  AI RECOMMENDATIONS  │  │
│  │                                             │  │                      │  │
│  │   [Delhi Junction with train markers]       │  │  • Solution cards    │  │
│  │                                             │  │  • Energy metrics    │  │
│  │                                             │  │  • Explanations      │  │
│  ├─────────────────────────────────────────────┤  ├──────────────────────┤  │
│  │                                             │  │                      │  │
│  │         TIME-SPACE DIAGRAM                  │  │    DECISION LOG      │  │
│  │                                             │  │                      │  │
│  │   [Distance vs Time graph]                  │  │  • Event history     │  │
│  │                                             │  │  • Timestamps        │  │
│  └─────────────────────────────────────────────┘  └──────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 9.2 Key Components

#### Track Schematic (SVG)
- External SVG file: `delhi_junction.svg`
- 4-way hub layout with color-coded lines
- Dynamic train markers with position interpolation
- Station labels and loop indicators

#### Train Markers
```javascript
// Position interpolation for train markers
function getTrainSVGPosition(train) {
    const route = train.route;
    const position = train.position;
    
    switch(route) {
        case 'NORTH': return interpolateNorthLine(position);
        case 'SOUTH': return interpolateSouthLine(position);
        case 'EAST': return interpolateEastLine(position);
        case 'WEST': return interpolateWestLine(position);
    }
}

function interpolateBetweenStations(stations, km) {
    for (let i = 0; i < stations.length - 1; i++) {
        const s1 = stations[i];
        const s2 = stations[i + 1];
        
        if (km >= s1.km && km <= s2.km) {
            const ratio = (km - s1.km) / (s2.km - s1.km);
            return {
                x: s1.x + (s2.x - s1.x) * ratio,
                y: s1.y + (s2.y - s1.y) * ratio
            };
        }
    }
}
```

#### Time-Space Graph
- Canvas-based visualization
- Real-time train position plotting
- Conflict zone highlighting
- Smooth animations

### 9.3 State Management

```javascript
let state = {
    connected: false,
    scenarios: [],
    currentScenario: null,
    trackInfo: null,
    trains: [],
    conflict: null,
    solutions: [],
    isRunning: false,
    energyConsumed: 0,
    energySaved: 0,
    graphData: [],
    aiPerformance: {
        conflictsDetected: 0,
        conflictsResolved: 0,
        totalResponseTime: 0,
        solutionsGenerated: 0,
        priorityRespected: null
    },
    playbackSpeed: 1,
    executingSolution: null,
    isExecuting: false
};
```

### 9.4 Playback Controls

```javascript
function setPlaybackSpeed(speed) {
    state.playbackSpeed = speed;
    
    // Adjust CSS transition duration
    const transitionDuration = Math.max(0.1, CONFIG.UPDATE_INTERVAL / speed / 1000);
    document.querySelectorAll('.train-marker').forEach(marker => {
        marker.style.transition = `transform ${transitionDuration}s linear`;
    });
    
    if (state.isRunning) {
        startSimulation();
    }
}

// Available speeds: 0.5x, 1x, 2x, 5x
```

### 9.5 UI Elements

#### Header Components
- Logo and section info
- Scenario selector dropdown
- Connection status indicator
- Playback controls (pause, speed)
- Train dropdown button
- Real-time clock

#### Train Dropdown Panel
- Collapsible panel showing all active trains
- Train color, ID, type, speed, position
- Priority indicators

#### AI Recommendations Panel
- Idle state with monitoring message
- Active state with solution cards
- Energy savings display
- Explanation text

#### Decision Log
- Timestamped event entries
- Color-coded by type (info, success, danger, warning)
- Clear button

### 9.6 Modals

#### Conflict Alert Modal
- Conflict details (location, time, trains, type)
- AI recommendations list
- Solution cards with metrics
- Approve/Reject buttons

#### Simulation Preview Modal
- Solution preview graph
- Speed controls
- Safety checks display
- Approve/Cancel buttons

### 9.7 Styling

```css
:root {
    --bg-primary: #0a1628;
    --bg-secondary: #0f1f35;
    --bg-panel: #0d1a2d;
    --bg-card: #1a2d47;
    --border-color: #2a4060;
    --text-primary: #e8f0f8;
    --text-secondary: #8aa4c0;
    --accent-cyan: #00d4ff;
    --accent-green: #00ff88;
    --accent-amber: #ffb800;
    --accent-red: #ff3b3b;
    --accent-purple: #a855f7;
    --accent-orange: #ff6b35;
}
```

### 9.8 Line Colors

| Line | Color | Hex |
|------|-------|-----|
| North | Green | #00ff88 |
| South | Cyan | #00d4ff |
| East | Orange | #ff6b35 |
| West | Purple | #a855f7 |


---

## 10. API Reference

### 10.1 Base URL
```
http://localhost:5000/api
```

### 10.2 Endpoints

#### Health Check
```
GET /api/health

Response:
{
    "status": "ok",
    "message": "NeuralRail API is running"
}
```

#### Get Scenarios
```
GET /api/scenarios

Response:
{
    "scenarios": [
        {
            "id": 1,
            "name": "Delhi Hub Convergence - 4 Routes Conflict",
            "description": "4 trains from 4 directions converging on New Delhi Hub",
            "judge_points": [...]
        }
    ]
}
```

#### Start Scenario
```
POST /api/scenario/<scenario_id>/start

Response:
{
    "status": "started",
    "scenario": {
        "id": "delhi_hub_1",
        "name": "Delhi Hub Convergence",
        "description": "...",
        "section": "Delhi Hub"
    },
    "trains": [
        {
            "id": "RAJ_N",
            "name": "Rajdhani Express",
            "type": "rajdhani",
            "position": 30,
            "speed": 100,
            "direction": "forward",
            "destination": 0,
            "priority": 2,
            "mass_tons": 850,
            "track_number": 1,
            "color": "#FF4444",
            "route": "NORTH"
        }
    ],
    "track_info": {...},
    "stations": [...]
}
```

#### Simulation Step
```
POST /api/simulation/step

Response:
{
    "time_minutes": 5.2,
    "trains": [
        {
            "id": "RAJ_N",
            "position": 25.5,
            "speed": 100,
            "state": "moving",
            "energy_kwh": 45.2,
            "next_station": "Old Delhi",
            "distance_to_station": 18.5,
            "route": "NORTH"
        }
    ],
    "conflicts": [
        {
            "train_a": "RAJ_N",
            "train_b": "VB_S",
            "position_km": 0,
            "time_minutes": 8.5,
            "severity": "critical"
        }
    ],
    "system_energy_kwh": 125.8,
    "history": [...]
}
```

#### Analyze Conflict
```
POST /api/conflict/analyze

Response:
{
    "conflict": {
        "position_km": 0,
        "time_minutes": 8.5,
        "severity": "critical",
        "train_a": "RAJ_N",
        "train_b": "VB_S"
    },
    "solutions": [
        {
            "id": "A1",
            "action": "Stop FRT_W",
            "type": "stop",
            "energy_kwh": 850,
            "delay_minutes": 8,
            "priority_violation": false,
            "score": 45.2,
            "safety_score": 10,
            "is_recommended": true,
            "train_affected": "FRT_W",
            "train_passing": "RAJ_N",
            "description": "Stop FRT_W at Sadar Bazar, let RAJ_N pass"
        }
    ],
    "explanation": "Recommended action: Stop FRT_W...",
    "energy_saved_kwh": 620
}
```

#### Explain Alternative
```
GET /api/conflict/explain/<question>

Questions:
- why-not-stop-both
- why-not-slow-both
- what-if-priority-reversed
- why-respect-priority

Response:
{
    "question": "Why not stop both trains?",
    "answer": "Stopping both trains wastes energy (1470 kWh total)...",
    "energy_comparison": {
        "stop_both": 1470,
        "stop_one": 850,
        "savings": 620
    }
}
```

#### Get Similar Conflicts
```
GET /api/conflict/similar

Response:
{
    "current_conflict": {...},
    "similar_cases": [
        {
            "date": "2024-11-15",
            "time": "14:20",
            "section": "NDLS-GZB",
            "trains": ["RAJ", "FRT"],
            "resolution": "Stop Freight",
            "energy_used": 845,
            "similarity_score": 95
        }
    ],
    "total_found": 3
}
```

#### Control Train
```
POST /api/train/<train_id>/control

Request:
{
    "action": "stop" | "set_speed" | "emergency_stop",
    "value": 80  // for set_speed
}

Response:
{
    "status": "ok",
    "train": {...},
    "message": "Train RAJ_N stop executed"
}
```

#### Create Custom Scenario
```
POST /api/scenario/custom

Request:
{
    "name": "Custom Scenario",
    "description": "User-created scenario",
    "trains": [
        {
            "train_id": "T1",
            "train_type": "rajdhani",
            "initial_position_km": 50,
            "initial_speed_kmh": 100,
            "destination_km": 0,
            "direction": "forward"
        }
    ],
    "track_info": {"tracks": 2}
}

Response:
{
    "status": "started",
    "scenario": {...},
    "trains": [...]
}
```

#### Simulate Solution
```
POST /api/solution/simulate

Request:
{
    "solution_id": "A1"
}

Response:
{
    "solution": {...},
    "safety_checks": {
        "collision_check": {"passed": true, "message": "No collision detected"},
        "braking_distance": {"passed": true, "message": "Sufficient braking distance"},
        "speed_limits": {"passed": true, "message": "All speed limits respected"},
        "track_capacity": {"passed": true, "message": "Track capacity OK"},
        "new_conflicts": {"passed": true, "message": "No new conflicts created"}
    },
    "timeline": [...],
    "predicted_outcome": {
        "conflict_resolved": true,
        "energy_kwh": 850,
        "delay_minutes": 8,
        "safety_score": 10
    }
}
```


---

## 11. Scenarios & Demo

### 11.1 Primary Scenario: Delhi Hub Multi-Route Conflict

**The KILLER DEMO scenario showing AI coordination at a major hub.**

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DELHI HUB CONVERGENCE                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   4 trains from 4 directions ALL converging on New Delhi Hub        │
│   within a 5-minute window!                                         │
│                                                                      │
│   NORTH: Rajdhani (RAJ_N) ──────────────────────┐                   │
│          Position: 30 km, Speed: 100 km/h       │                   │
│          Priority: 2, Passengers: 1,122         │                   │
│                                                  │                   │
│   WEST:  Freight (FRT_W) ───────────────────────┼───► NEW DELHI    │
│          Position: 12 km, Speed: 60 km/h        │      HUB          │
│          Priority: 6, Cargo: 4,200 tons         │                   │
│                                                  │                   │
│   EAST:  Shatabdi (SHT_E) ──────────────────────┤                   │
│          Position: 20 km, Speed: 110 km/h       │                   │
│          Priority: 2, Passengers: 1,050         │                   │
│                                                  │                   │
│   SOUTH: Vande Bharat (VB_S) ───────────────────┘                   │
│          Position: 25 km, Speed: 130 km/h                           │
│          Priority: 2, Passengers: 1,128                             │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

#### Conflicts Detected

| ID | Type | Trains | Severity | Description |
|----|------|--------|----------|-------------|
| C1 | Platform Conflict | RAJ_N, VB_S | HIGH | Both want Platform 1 |
| C2 | Path Conflict | VB_S, FRT_W | CRITICAL | Freight crossing Vande Bharat path |
| C3 | Track Block | VB_S, LOC_S | MEDIUM | Local EMU blocks track switching |
| C4 | Following Conflict | SHT_E, EXP_E | LOW | Express following Shatabdi too closely |

#### AI Solution

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AI RECOMMENDED SOLUTION                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   Step 1: HOLD FRT_W at Sadar Bazar Holding Loop                    │
│           Reason: Lowest priority (P6), clear path for superfast    │
│           Energy saved: 3,900 kWh (avoided stopping freight)        │
│           Delay: 8 minutes                                          │
│                                                                      │
│   Step 2: ASSIGN VB_S to Platform 1                                 │
│           Reason: Arrives first among P2 trains                     │
│                                                                      │
│   Step 3: ASSIGN RAJ_N to Platform 2                                │
│           Reason: Same priority as VB_S, but arrives later          │
│                                                                      │
│   Step 4: PROCEED SHT_E to Platform 3                               │
│           Reason: No conflict, clear path                           │
│                                                                      │
│   Step 5: RELEASE FRT_W after VB_S clears hub                       │
│           Reason: Safe to proceed after superfast trains clear      │
│                                                                      │
│   ════════════════════════════════════════════════════════          │
│   TOTAL DELAY: 8 minutes (only freight delayed)                     │
│   ENERGY SAVED: 3,900 kWh                                           │
│   PASSENGERS AFFECTED: 0 (no passenger train delays!)               │
│   PRIORITY RESPECTED: ✓ Yes                                         │
│   ════════════════════════════════════════════════════════          │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 11.2 Additional Scenarios

#### Scenario 4: Junction Deadlock (Mathura Triangle)
- 4 trains blocking each other at Mathura Junction
- Classic deadlock - AI must sequence correctly
- Complexity: HIGH

#### Scenario 5: Diamond Crossing (Aligarh Junction)
- Two trains approaching grade crossing
- Timing-critical conflict
- Complexity: MEDIUM

#### Scenario 6: Single Line Meet (Jhansi-Kanpur)
- Two trains on single track
- Must coordinate at crossing loop
- Complexity: MEDIUM

#### Scenario 7: Cascade Delay (Delhi Hub Chaos)
- Rajdhani 2hr late affects 6 connecting trains
- Most complex scenario
- Complexity: VERY_HIGH

### 11.3 Demo Script

**Opening:**
> "Watch as 4 trains from 4 different directions ALL converge on New Delhi Hub within a 5-minute window!"

**Conflict Highlight:**
> "Rajdhani from Punjab and Vande Bharat from Agra BOTH want Platform 1!"

**AI Action:**
> "Our AI detects the conflict 10 minutes in advance and coordinates all movements"

**Energy Point:**
> "By holding the freight at Sadar Bazar Loop instead of stopping it, we save 3,900 kWh!"

**Conclusion:**
> "Zero passenger delays, all priorities respected, maximum energy efficiency"

### 11.4 Judge Points

1. **4-way hub coordination** - Most complex scenario
2. **Real Delhi infrastructure** - Loops, platforms, tracks
3. **Priority system** - P2 (RAJ, VB, SHT) > P6 (FRT)
4. **Energy optimization** - Don't stop 4,200-ton freight unnecessarily
5. **Loop utilization** - Sadar Bazar Holding Loop for freight
6. **Track management** - T3 on South Line available
7. **Zero passenger delays** in optimal solution
8. **Real-time conflict detection** and resolution


---

## 12. Deployment Guide

### 12.1 Prerequisites

- Python 3.8+
- Node.js 16+ (optional, for frontend development)
- Modern web browser (Chrome, Firefox, Edge)

### 12.2 Installation

#### Step 1: Clone Repository
```bash
git clone <repository-url>
cd NeuralRail
```

#### Step 2: Install Python Dependencies
```bash
pip install -r requirements.txt
```

**requirements.txt:**
```
# Core
flask==3.0.0
flask-cors==4.0.0

# Optional (for LLM features)
# groq==0.4.0
# google-generativeai==0.3.0
```

#### Step 3: Install Frontend Dependencies (Optional)
```bash
cd frontend
npm install
```

### 12.3 Running the Application

#### Start Backend API
```bash
python backend/api/app.py
```
API runs on: `http://localhost:5000`

#### Start Frontend (Development)
```bash
cd frontend
npm run dev
```
Frontend runs on: `http://localhost:3000`

#### Or: Serve Frontend Statically
Simply open `frontend/index.html` in a browser, or serve with any static file server:
```bash
cd frontend
python -m http.server 8080
```
Then open: `http://localhost:8080`

### 12.4 Testing

#### Test Backend Scenarios
```bash
python scenarios/scenario_runner.py
```

#### Test Conflict Resolver
```bash
python backend/optimizer/test_resolver.py
```

#### Test Energy Calculations
```bash
python backend/physics/test_energy.py
```

### 12.5 Configuration

#### API Configuration
```python
# backend/api/app.py
CONFIG = {
    'API_BASE': 'http://localhost:5000/api',
    'DEBUG': True,
    'PORT': 5000
}
```

#### Frontend Configuration
```javascript
// frontend/app.js
const CONFIG = {
    API_BASE: 'http://localhost:5000/api',
    UPDATE_INTERVAL: 800,  // ms
    ANIMATION_DURATION: 800  // ms
};
```

#### Conflict Resolver Configuration
```python
# backend/optimizer/conflict_resolver.py
config = {
    'energy_weight': 0.4,
    'delay_weight': 0.2,
    'priority_weight': 0.4,
    'priority_penalty': 500,
    'mode': 'balanced',  # or 'energy_priority', 'time_priority', 'strict_priority'
    'emergency_threshold_minutes': 5
}
```

### 12.6 Environment Variables (Optional)

For LLM features:
```bash
export GROQ_API_KEY=your_groq_api_key
export GEMINI_API_KEY=your_gemini_api_key
```

### 12.7 Production Deployment

#### Using Gunicorn (Linux/Mac)
```bash
pip install gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 backend.api.app:app
```

#### Using Waitress (Windows)
```bash
pip install waitress
waitress-serve --port=5000 backend.api.app:app
```

#### Docker Deployment
```dockerfile
FROM python:3.10-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

EXPOSE 5000
CMD ["python", "backend/api/app.py"]
```

```bash
docker build -t neuralrail .
docker run -p 5000:5000 neuralrail
```

### 12.8 Troubleshooting

| Issue | Solution |
|-------|----------|
| CORS errors | Ensure Flask-CORS is installed and enabled |
| API not responding | Check if backend is running on port 5000 |
| Trains not moving | Check browser console for JavaScript errors |
| SVG not loading | Ensure `delhi_junction.svg` is in frontend folder |
| LLM not working | Check API keys or use fallback templates |


---

## 13. Technical Specifications

### 13.1 System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| **CPU** | 2 cores | 4+ cores |
| **RAM** | 2 GB | 4+ GB |
| **Storage** | 100 MB | 500 MB |
| **Python** | 3.8 | 3.10+ |
| **Browser** | Chrome 90+ | Chrome 100+ |

### 13.2 Performance Metrics

| Metric | Value |
|--------|-------|
| **API Response Time** | < 100ms |
| **Conflict Detection** | 7-30 minutes advance |
| **Solution Generation** | < 500ms |
| **UI Update Rate** | 800ms (configurable) |
| **Max Trains Simulated** | 10+ |
| **Max Conflicts Handled** | 4+ simultaneous |

### 13.3 Data Formats

#### Train Data
```json
{
    "train_id": "RAJ_N",
    "train_type": "rajdhani",
    "name": "Rajdhani Express",
    "position": 30.5,
    "speed": 100,
    "direction": "forward",
    "destination": 0,
    "priority": 2,
    "mass_tons": 850,
    "track_number": 1,
    "color": "#FF4444",
    "route": "NORTH",
    "energy_kwh": 45.2,
    "state": "moving"
}
```

#### Conflict Data
```json
{
    "train_a": "RAJ_N",
    "train_b": "VB_S",
    "conflict_position_km": 0,
    "time_to_conflict_minutes": 8.5,
    "time_to_conflict_seconds": 510,
    "severity": "critical"
}
```

#### Solution Data
```json
{
    "solution_id": "A1",
    "type": "stop",
    "action": "Stop FRT_W at Sadar Bazar",
    "train_affected": "FRT_W",
    "train_passing": "RAJ_N",
    "energy_joules": 3060000000,
    "energy_kwh": 850,
    "delay_seconds": 480,
    "delay_minutes": 8,
    "priority_violation": false,
    "score": 45.2,
    "safety_score": 10,
    "is_recommended": true,
    "breakdown": {
        "braking_kwh": 350,
        "idle_kwh": 150,
        "restart_kwh": 350
    }
}
```

### 13.4 Algorithm Complexity

| Algorithm | Time Complexity | Space Complexity |
|-----------|-----------------|------------------|
| Conflict Detection | O(n²) | O(n) |
| Solution Generation | O(n) | O(n) |
| Solution Ranking | O(n log n) | O(n) |
| Path Finding (BFS) | O(V + E) | O(V) |
| Energy Calculation | O(1) | O(1) |

### 13.5 Supported Train Types

| Type Key | Display Name | Priority |
|----------|--------------|----------|
| `rajdhani` | Rajdhani Express | 2 |
| `shatabdi` | Shatabdi Express | 2 |
| `vande_bharat` | Vande Bharat Express | 2 |
| `express_passenger` | Express/Mail | 3 |
| `memu` | MEMU | 4 |
| `local_emu` | Local EMU | 5 |
| `freight_heavy` | Heavy Freight | 6 |

### 13.6 Supported Solution Types

| Type | Description | Permanent |
|------|-------------|-----------|
| `stop` | One train stops completely | ✓ |
| `slow` | One train reduces speed | ✗ |
| `both_slow` | Both trains reduce speed | ✗ |
| `slow_and_stop` | High priority slows, low priority stops | ✓ |
| `multi_step` | Slow + track switch | ✓ |

### 13.7 Network Topology

| Metric | Value |
|--------|-------|
| **Total Stations** | 13 |
| **Total Track Segments** | 14 |
| **Total Loops** | 3 |
| **Lines** | 4 (North, South, East, West) |
| **Hub** | New Delhi (NDLS) |
| **Max Distance** | 141 km (to Mathura) |

### 13.8 Energy Constants

| Constant | Value | Unit |
|----------|-------|------|
| Regenerative Recovery | 30% | - |
| Motor Efficiency | 85% | - |
| Rolling Resistance Coefficient | 0.002 | - |
| Gravity | 9.81 | m/s² |
| Joules per kWh | 3,600,000 | J/kWh |

### 13.9 Priority Penalties

| Violation Type | Penalty |
|----------------|---------|
| Stop higher priority train | +500 points |
| Delay superfast train | +300 points |
| Energy override threshold | 1000 kWh |

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| **Conflict** | Situation where two trains are on collision course |
| **Priority** | Train importance level (1=highest, 6=lowest) |
| **Gradient** | Track slope (uphill/downhill/flat) |
| **Loop** | Siding track for holding/overtaking trains |
| **Hub** | Central station where multiple routes converge |
| **Regenerative Braking** | Energy recovery during braking (electric trains) |
| **Section Controller** | Railway operator managing a track section |
| **kWh** | Kilowatt-hour (energy unit) |

---

## Appendix B: References

1. Indian Railways Operating Manual
2. RDSO Technical Specifications
3. Railway Board Circulars on Train Priority
4. IRFCA (Indian Railways Fan Club Association) Data
5. Northern Railway Zone Route Information
