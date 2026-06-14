"""
Evaluation metrics + episode runner.

Drives the simulator with any controller and measures the joint objective:
throughput, on-time rate, collisions, delay, and physics-based energy (kWh/₹/CO₂)
computed identically for every controller via the shared EnergyModel.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from statistics import mean, pstdev
from typing import List

from physics.attribute_mapper import PhysicalAttributeMapper
from physics.econ_constants import kwh_to_co2_kg, kwh_to_inr
from physics.energy_model import EnergyModel
from sim.models import TrainStatus
from sim.railway_simulator import RailwaySimulator

_TERMINAL = (TrainStatus.ARRIVED, TrainStatus.DELAYED)


@dataclass
class EpisodeMetrics:
    task: str
    controller: str
    seed: int
    steps: int
    total_trains: int
    trains_arrived: int
    trains_on_time: int
    collisions: int
    total_delay: int
    avg_delay: float
    energy_kwh: float

    @property
    def arrival_rate(self) -> float:
        return self.trains_arrived / max(self.total_trains, 1)

    @property
    def on_time_rate(self) -> float:
        return self.trains_on_time / max(self.total_trains, 1)

    @property
    def energy_inr(self) -> float:
        return kwh_to_inr(self.energy_kwh)

    @property
    def energy_co2_kg(self) -> float:
        return kwh_to_co2_kg(self.energy_kwh)


def run_episode(task: str, controller, seed: int) -> EpisodeMetrics:
    sim = RailwaySimulator(task)
    sim.reset(seed=seed)
    mapper = PhysicalAttributeMapper()
    energy = EnergyModel(mapper)
    if hasattr(controller, "reset"):
        controller.reset()

    total_kwh = 0.0
    steps = 0
    done = False
    while not done and steps < sim.max_steps + 1:
        prev = sim.observation
        action = controller.act(sim)
        nxt, _reward, done = sim.step(action)
        e = energy.step_energy(prev, nxt)
        total_kwh += e.total_kwh
        if hasattr(controller, "update_energy"):
            controller.update_energy(e.per_train_kwh, e.total_kwh)
        steps += 1

    trains = sim._trains
    arrived = sum(1 for t in trains.values() if t.status in _TERMINAL)
    on_time = sum(1 for t in trains.values() if t.status == TrainStatus.ARRIVED)
    total_delay = sum(t.delay for t in trains.values())
    return EpisodeMetrics(
        task=task, controller=getattr(controller, "name", "controller"), seed=seed,
        steps=steps, total_trains=len(trains), trains_arrived=arrived,
        trains_on_time=on_time, collisions=sim.collisions, total_delay=total_delay,
        avg_delay=total_delay / max(len(trains), 1), energy_kwh=total_kwh,
    )


@dataclass
class AggregateMetrics:
    task: str
    controller: str
    n: int
    arrival_rate: float
    on_time_rate: float
    collisions: float
    avg_delay: float
    energy_kwh: float
    energy_kwh_std: float
    energy_inr: float
    energy_co2_kg: float


def aggregate(episodes: List[EpisodeMetrics]) -> AggregateMetrics:
    assert episodes, "no episodes to aggregate"
    e_kwh = [e.energy_kwh for e in episodes]
    return AggregateMetrics(
        task=episodes[0].task,
        controller=episodes[0].controller,
        n=len(episodes),
        arrival_rate=mean(e.arrival_rate for e in episodes),
        on_time_rate=mean(e.on_time_rate for e in episodes),
        collisions=mean(e.collisions for e in episodes),
        avg_delay=mean(e.avg_delay for e in episodes),
        energy_kwh=mean(e_kwh),
        energy_kwh_std=pstdev(e_kwh) if len(e_kwh) > 1 else 0.0,
        energy_inr=mean(e.energy_inr for e in episodes),
        energy_co2_kg=mean(e.energy_co2_kg for e in episodes),
    )
