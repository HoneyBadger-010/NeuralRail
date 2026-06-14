"""
Train physical specifications — vendored from NeuralRail
(NeuralRail1/backend/data/railway_network.py :: TRAIN_TYPES).

These are the real physical parameters (mass, speeds, idle power, traction) that
NeuralRail's energy model consumes. We keep the full catalogue for fidelity even
though the gym-env only maps onto four of them.

NOTE on priority: NeuralRail's `priority` here is the OPPOSITE convention to the
gym-env (NeuralRail: 2=Rajdhani … 6=freight, lower = more important). We use
these specs ONLY for physics (mass/speed/idle/traction); train priority for the
reward comes from the gym-env's own TrainState.priority via the simulator.
"""

from __future__ import annotations

TRAIN_TYPES = {
    # Priority 2 — Superfast Express
    "rajdhani": {
        "name": "Rajdhani Express", "mass_kg": 850000, "max_speed_kmh": 140,
        "acceleration_mps2": 0.38, "braking_rate_mps2": 0.72, "idle_power_kw": 180,
        "traction_type": "electric", "passenger_capacity": 1122, "priority": 2,
    },
    "shatabdi": {
        "name": "Shatabdi Express", "mass_kg": 720000, "max_speed_kmh": 150,
        "acceleration_mps2": 0.42, "braking_rate_mps2": 0.75, "idle_power_kw": 150,
        "traction_type": "electric", "passenger_capacity": 1050, "priority": 2,
    },
    "vande_bharat": {
        "name": "Vande Bharat Express", "mass_kg": 430000, "max_speed_kmh": 160,
        "acceleration_mps2": 1.05, "braking_rate_mps2": 1.2, "idle_power_kw": 95,
        "traction_type": "electric", "passenger_capacity": 1128, "priority": 2,
    },
    "duronto": {
        "name": "Duronto Express", "mass_kg": 780000, "max_speed_kmh": 130,
        "acceleration_mps2": 0.40, "braking_rate_mps2": 0.70, "idle_power_kw": 160,
        "traction_type": "electric", "passenger_capacity": 850, "priority": 2,
    },
    # Priority 3 — Mail/Express
    "express_passenger": {
        "name": "Express/Mail", "mass_kg": 520000, "max_speed_kmh": 110,
        "acceleration_mps2": 0.42, "braking_rate_mps2": 0.65, "idle_power_kw": 65,
        "traction_type": "electric", "passenger_capacity": 1430, "priority": 3,
    },
    # Priority 4 — Passenger
    "memu": {
        "name": "MEMU", "mass_kg": 350000, "max_speed_kmh": 100,
        "acceleration_mps2": 0.70, "braking_rate_mps2": 0.90, "idle_power_kw": 50,
        "traction_type": "electric", "passenger_capacity": 2000, "priority": 4,
    },
    # Priority 5 — Suburban
    "local_emu": {
        "name": "Local EMU", "mass_kg": 380000, "max_speed_kmh": 105,
        "acceleration_mps2": 0.85, "braking_rate_mps2": 1.1, "idle_power_kw": 55,
        "traction_type": "electric", "passenger_capacity": 3600, "priority": 5,
    },
    # Priority 6 — Freight
    "freight_heavy": {
        "name": "Heavy Freight", "mass_kg": 4200000, "max_speed_kmh": 75,
        "acceleration_mps2": 0.12, "braking_rate_mps2": 0.28, "idle_power_kw": 45,
        "traction_type": "electric", "passenger_capacity": 0, "priority": 6,
    },
}
