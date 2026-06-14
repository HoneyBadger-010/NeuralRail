"""
Evaluate controllers across seeds and tasks; print a comparison table and write
CSV. Includes the RL agent automatically if a checkpoint exists.

    python -m eval.run_eval --task all --seeds 30
    python -m eval.run_eval --task basic_control --seeds 50 --model models/basic_control/best.zip
    python -m eval.run_eval --task basic_control --calibrate   # report p95 step-energy for energy_ref
"""

from __future__ import annotations

import argparse
import csv
import os

from rich.console import Console
from rich.table import Table

from agents.baselines import BASELINE_NAMES, make_baseline
from agents.policy_loader import load_rl_controller
from configs.tasks import TASK_NAMES
from eval.metrics import aggregate, run_episode

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
console = Console()


def evaluate(task: str, seeds: int, model_path: str | None):
    controllers = [make_baseline(n) for n in BASELINE_NAMES]
    rl = load_rl_controller(task, model_path)
    if rl is not None:
        controllers.append(rl)
        console.print(f"[green]✓ RL agent loaded for {task}[/green]")
    else:
        console.print(f"[yellow]• no RL checkpoint for {task} — baselines only[/yellow]")

    aggs = []
    for ctrl in controllers:
        eps = [run_episode(task, ctrl, seed=s) for s in range(seeds)]
        aggs.append(aggregate(eps))
    return aggs


def print_table(task: str, aggs):
    t = Table(title=f"{task}  (n={aggs[0].n} seeds)", header_style="bold")
    t.add_column("controller")
    t.add_column("arrival%", justify="right")
    t.add_column("on-time%", justify="right")
    t.add_column("collisions", justify="right")
    t.add_column("avg delay", justify="right")
    t.add_column("energy kWh", justify="right")
    t.add_column("₹", justify="right")
    t.add_column("CO₂ kg", justify="right")
    best_energy = min(a.energy_kwh for a in aggs)
    for a in aggs:
        e = f"{a.energy_kwh:.0f}±{a.energy_kwh_std:.0f}"
        if abs(a.energy_kwh - best_energy) < 1e-6:
            e = f"[bold green]{e}[/bold green]"
        t.add_row(
            a.controller, f"{100*a.arrival_rate:.0f}", f"{100*a.on_time_rate:.0f}",
            f"{a.collisions:.2f}", f"{a.avg_delay:.1f}", e,
            f"{a.energy_inr:.0f}", f"{a.energy_co2_kg:.0f}",
        )
    console.print(t)


def calibrate(task: str, seeds: int):
    """Report the p95 per-step energy under no-control — a good energy_ref_kwh."""
    from physics.attribute_mapper import PhysicalAttributeMapper
    from physics.energy_model import EnergyModel
    from sim.railway_simulator import RailwaySimulator
    from agents.baselines import NoControlAgent

    mapper, em, ctrl = PhysicalAttributeMapper(), None, NoControlAgent()
    em = EnergyModel(mapper)
    step_kwh = []
    for s in range(seeds):
        sim = RailwaySimulator(task); sim.reset(seed=s)
        done = False
        while not done and sim.step_count < sim.max_steps + 1:
            prev = sim.observation
            nxt, _, done = sim.step(ctrl.act(sim))
            step_kwh.append(em.step_energy(prev, nxt).total_kwh)
    step_kwh.sort()
    p = lambda q: step_kwh[min(len(step_kwh) - 1, int(q * len(step_kwh)))]
    console.print(f"[bold]{task}[/bold] step-energy kWh — "
                  f"p50={p(0.5):.1f} p95={p(0.95):.1f} max={step_kwh[-1]:.1f}  "
                  f"(suggested energy_ref_kwh ≈ {p(0.95):.0f})")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--task", default="all")
    ap.add_argument("--seeds", type=int, default=30)
    ap.add_argument("--model", default=None, help="RL checkpoint (default models/<task>/best.zip)")
    ap.add_argument("--csv", default=os.path.join(ROOT, "eval", "results.csv"))
    ap.add_argument("--calibrate", action="store_true")
    args = ap.parse_args()

    tasks = TASK_NAMES if args.task == "all" else [args.task]

    if args.calibrate:
        for task in tasks:
            calibrate(task, args.seeds)
        return

    all_aggs = []
    for task in tasks:
        aggs = evaluate(task, args.seeds, args.model)
        print_table(task, aggs)
        all_aggs.extend(aggs)

    os.makedirs(os.path.dirname(args.csv), exist_ok=True)
    with open(args.csv, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["task", "controller", "n", "arrival_rate", "on_time_rate",
                    "collisions", "avg_delay", "energy_kwh", "energy_kwh_std",
                    "energy_inr", "energy_co2_kg"])
        for a in all_aggs:
            w.writerow([a.task, a.controller, a.n, f"{a.arrival_rate:.4f}",
                        f"{a.on_time_rate:.4f}", f"{a.collisions:.4f}",
                        f"{a.avg_delay:.4f}", f"{a.energy_kwh:.2f}",
                        f"{a.energy_kwh_std:.2f}", f"{a.energy_inr:.2f}",
                        f"{a.energy_co2_kg:.2f}"])
    console.print(f"[dim]wrote {args.csv}[/dim]")


if __name__ == "__main__":
    main()
