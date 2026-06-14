"""
Task configurations and physical-dimensioning constants.

TASK_CONFIGS are vendored verbatim from the original gym-env (difficulty,
train count, step budget, feature flags). The PHYSICS_* constants are NEW: they
give the otherwise-dimensionless simulation real units (km, seconds) so
NeuralRail's energy model produces meaningful kWh.
"""

# --- Vendored from railway_controller_gym_env (RailwayControllerEnvironment) ---
TASK_CONFIGS = {
    "basic_control": {
        "difficulty": "easy",
        "num_trains": 2,
        "max_steps": 30,
        "has_junctions": False,
        "has_delays": False,
        "has_weather": False,
        "express_trains": 0,
    },
    "junction_management": {
        "difficulty": "medium",
        "num_trains": 4,
        "max_steps": 50,
        "has_junctions": True,
        "has_delays": False,
        "has_weather": False,
        "express_trains": 1,
    },
    "express_priority": {
        "difficulty": "medium-hard",
        "num_trains": 5,
        "max_steps": 40,
        "has_junctions": True,
        "has_delays": False,
        "has_weather": False,
        "express_trains": 3,
    },
    "rush_hour": {
        "difficulty": "hard",
        "num_trains": 6,
        "max_steps": 80,
        "has_junctions": True,
        "has_delays": True,
        "has_weather": True,
        "express_trains": 2,
    },
}

TASK_NAMES = list(TASK_CONFIGS.keys())

# --- NEW: physical dimensioning so the discrete sim maps to real energy ---
# One simulation step represents this many wall-clock seconds. Drives idle energy.
STEP_SECONDS = 60.0
# A segment of `length` steps is treated as this many km per step of length.
NOMINAL_KM_PER_STEP = 1.5
# Clamp derived segment distance to a sane band (km).
SEGMENT_KM_MIN = 1.0
SEGMENT_KM_MAX = 40.0
# Speed limits (km/h) used to derive a cruise speed per segment.
JUNCTION_SPEED_KMH = 60.0   # junctions / stations are slow
PLAINLINE_SPEED_KMH = None  # None => use the train's own max speed
