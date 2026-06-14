"""
PhysicalAttributeMapper — bridges the gym-env's abstract trains to NeuralRail's
real physical specs, and dimensions the abstract network into km / km-h.

The gym-env trains carry only a `train_type` string ("regular" | "express" |
"high-speed") and a 1..3 priority. To compute real energy we attach mass, max
speed, idle power and traction from NeuralRail's TRAIN_TYPES, and we turn the
dimensionless segment `length` (in steps) into kilometres.
"""

from __future__ import annotations

from dataclasses import dataclass

from configs.tasks import (
    JUNCTION_SPEED_KMH,
    NOMINAL_KM_PER_STEP,
    SEGMENT_KM_MAX,
    SEGMENT_KM_MIN,
)
from .train_types import TRAIN_TYPES

# gym train_type  ->  NeuralRail TRAIN_TYPES key
GYM_TO_NEURALRAIL = {
    "regular": "express_passenger",   # 520 t, 110 km/h
    "express": "shatabdi",            # 720 t, 150 km/h
    "high-speed": "vande_bharat",     # 430 t, 160 km/h
    "high_speed": "vande_bharat",     # alias (underscore form)
    "freight": "freight_heavy",       # 4200 t, 75 km/h
}

# Fallback by gym priority int if train_type string is unknown.
PRIORITY_FALLBACK = {1: "express_passenger", 2: "shatabdi", 3: "vande_bharat"}


@dataclass(frozen=True)
class PhysicalSpec:
    mass_kg: float
    max_speed_kmh: float
    idle_power_kw: float
    accel_mps2: float
    braking_rate_mps2: float
    traction_type: str
    neuralrail_key: str

    @property
    def is_electric(self) -> bool:
        return self.traction_type == "electric"


class PhysicalAttributeMapper:
    """Maps gym TrainState -> PhysicalSpec, and TrackSegment -> km / cruise km/h."""

    def __init__(self):
        self._cache: dict = {}

    def spec_for(self, train) -> PhysicalSpec:
        """train: a TrainState (or any object with .train_type / .priority)."""
        ttype = (train.train_type or "regular").lower()
        key = GYM_TO_NEURALRAIL.get(ttype)
        if key is None:
            key = PRIORITY_FALLBACK.get(getattr(train, "priority", 1), "express_passenger")
        if key in self._cache:
            return self._cache[key]
        t = TRAIN_TYPES[key]
        spec = PhysicalSpec(
            mass_kg=float(t["mass_kg"]),
            max_speed_kmh=float(t["max_speed_kmh"]),
            idle_power_kw=float(t["idle_power_kw"]),
            accel_mps2=float(t["acceleration_mps2"]),
            braking_rate_mps2=float(t["braking_rate_mps2"]),
            traction_type=t["traction_type"],
            neuralrail_key=key,
        )
        self._cache[key] = spec
        return spec

    @staticmethod
    def segment_km(segment) -> float:
        """Physical length of a segment (km), clamped to a sane band."""
        km = float(segment.length) * NOMINAL_KM_PER_STEP
        return max(SEGMENT_KM_MIN, min(SEGMENT_KM_MAX, km))

    @staticmethod
    def cruise_kmh(spec: PhysicalSpec, segment) -> float:
        """Effective cruise speed on a segment: train max, capped at junctions/stations."""
        if segment.is_junction or segment.station_name:
            return min(spec.max_speed_kmh, JUNCTION_SPEED_KMH)
        return spec.max_speed_kmh
