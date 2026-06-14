"""
EnergyModel — the integration crux.

Turns the gym-env's *discrete* segment transitions into physically-grounded
energy (kWh) using NeuralRail's `EnergyCalculator`. It is a STATELESS function of
two consecutive observations: it classifies each train's transition into exactly
one energy event per step, mirroring NeuralRail's `Train.move()` case logic
(idle / braking / restart-acceleration / cruise) but driven by segment changes
rather than continuous speed targets.

Per-train transition classification (one event per step):
  * already terminal (ARRIVED/DELAYED in prev)  -> no energy (parked)
  * advanced a segment, was stopped              -> RESTART  (acceleration_energy 0->cruise)
  * advanced a segment, was moving               -> CRUISE   (rolling-resistance work)
  * stayed put, was moving, now stopped          -> BRAKING  (braking_energy_loss, 30% regen if electric)
  * stayed put, stopped both steps               -> IDLE     (idle_power x step_seconds)
  * otherwise (degenerate)                        -> 0

This mirrors railway_controller's dynamics: a train advances INTO a segment on
one step (MOVING) and only ARRIVES on a later step (segment unchanged), so a
single step never double-counts "travel + stop".
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Dict

from configs.tasks import STEP_SECONDS
from sim.models import RailwayObservation, TrainStatus

from .attribute_mapper import PhysicalAttributeMapper
from .energy_calculator import (
    GRAVITY,
    ROLLING_RESISTANCE_COEFF,
    EnergyCalculator,
    joules_to_kwh,
)

REGEN_EFFICIENCY = 0.30  # electric regenerative braking recovery (matches NeuralRail)
_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)


@dataclass
class EnergyResult:
    total_kwh: float = 0.0
    per_train_kwh: Dict[str, float] = field(default_factory=dict)
    breakdown_kwh: Dict[str, float] = field(
        default_factory=lambda: {
            "accel": 0.0, "cruise": 0.0, "brake": 0.0, "idle": 0.0, "regen": 0.0
        }
    )


def _is_stopped(train) -> bool:
    return train.status != TrainStatus.MOVING or train.speed < 0.1


class EnergyModel:
    """Computes per-step energy from two consecutive observations."""

    def __init__(self, mapper: PhysicalAttributeMapper, step_seconds: float = STEP_SECONDS):
        self.mapper = mapper
        self.step_seconds = step_seconds

    def step_energy(self, prev_obs: RailwayObservation, next_obs: RailwayObservation) -> EnergyResult:
        result = EnergyResult()
        for tid, prev_t in prev_obs.trains.items():
            next_t = next_obs.trains.get(tid)
            if next_t is None:
                continue

            # Parked / already finished: no traction energy.
            if prev_t.status in _TERMINAL:
                continue

            spec = self.mapper.spec_for(prev_t)
            m = spec.mass_kg
            joules = 0.0

            advanced = prev_t.current_segment != next_t.current_segment

            if advanced:
                entered = next_obs.track_segments.get(next_t.current_segment)
                if entered is None:
                    continue
                dist_km = self.mapper.segment_km(entered)
                v_cruise = self.mapper.cruise_kmh(spec, entered)

                if _is_stopped(prev_t):
                    # Restart from a standstill: ΔKE + rolling resistance, motor-efficiency adjusted.
                    e = EnergyCalculator.acceleration_energy(m, 0.0, v_cruise, dist_km)
                    result.breakdown_kwh["accel"] += joules_to_kwh(e)
                    joules += e
                else:
                    # Steady cruise: rolling-resistance work over the segment (matches Train.move Case 4).
                    e = m * GRAVITY * ROLLING_RESISTANCE_COEFF * dist_km * 1000.0
                    result.breakdown_kwh["cruise"] += joules_to_kwh(e)
                    joules += e
            else:
                current = next_obs.track_segments.get(next_t.current_segment)
                v_cruise = self.mapper.cruise_kmh(spec, current) if current is not None else spec.max_speed_kmh

                if (not _is_stopped(prev_t)) and _is_stopped(next_t):
                    # Decelerated to a halt (a forced stop, hold, or arrival).
                    e = EnergyCalculator.braking_energy_loss(m, v_cruise, 0.0)
                    if spec.is_electric:
                        regen = e * REGEN_EFFICIENCY
                        result.breakdown_kwh["regen"] -= joules_to_kwh(regen)
                        e -= regen
                    result.breakdown_kwh["brake"] += joules_to_kwh(e)
                    joules += e
                elif _is_stopped(prev_t) and _is_stopped(next_t):
                    # Sitting still (waiting at a signal / held): auxiliary idle draw.
                    e = EnergyCalculator.idle_energy(spec.idle_power_kw, self.step_seconds)
                    result.breakdown_kwh["idle"] += joules_to_kwh(e)
                    joules += e
                # else: moving-but-didn't-advance (degenerate) -> ~0

            kwh = joules_to_kwh(joules)
            result.per_train_kwh[tid] = kwh
            result.total_kwh += kwh

        return result
