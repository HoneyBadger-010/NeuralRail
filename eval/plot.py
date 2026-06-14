"""
Render a performance figure for a trained task: training curves (the "real PPO"
proof) + the RL-vs-baseline comparison. Doubles as a submission slide.

    python -m eval.plot --task basic_control [--seeds 50]

Outputs: models/<task>/performance.png
"""

from __future__ import annotations

import sys
try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

import argparse
import json
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

from agents.baselines import BASELINE_NAMES, make_baseline
from agents.policy_loader import load_rl_controller
from eval.metrics import aggregate, run_episode

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# --- CTC ops-room theme ---
BG, PANEL, INK, INK2, GRID = "#14130F", "#1b1815", "#ECE5D8", "#A89F90", "#2a2620"
HV, TEAL, GREEN, AMBER, RED, GREY = "#F4791F", "#2FBFB0", "#46C46A", "#F0B133", "#E8524C", "#5b554c"
plt.rcParams.update({
    "figure.facecolor": BG, "savefig.facecolor": BG, "axes.facecolor": PANEL,
    "text.color": INK, "axes.labelcolor": INK2, "axes.titlecolor": INK,
    "xtick.color": INK2, "ytick.color": INK2, "axes.edgecolor": "#3a352f",
    "grid.color": GRID, "font.family": "DejaVu Sans", "axes.grid": True,
    "grid.linewidth": 0.6, "axes.linewidth": 0.8,
})

CTRL_COLOR = {"no_control": GREY, "random": "#7a6f5e", "greedy_priority": TEAL, "rl_agent": HV}
CTRL_LABEL = {"no_control": "NO CONTROL", "random": "RANDOM", "greedy_priority": "GREEDY", "rl_agent": "RL AGENT"}


def load_curves(task: str) -> list:
    p = os.path.join(ROOT, "models", task, "training_curves.json")
    if not os.path.exists(p):
        return []
    with open(p) as f:
        return json.load(f)


def eval_task(task: str, seeds: int):
    controllers = [make_baseline(n) for n in BASELINE_NAMES]
    rl = load_rl_controller(task)
    if rl is not None:
        controllers.append(rl)
    out = {}
    for c in controllers:
        eps = [run_episode(task, c, seed=s) for s in range(seeds)]
        out[getattr(c, "name", "controller")] = aggregate(eps)
    return out


def _smooth(ys, k=9):
    if len(ys) < k:
        return ys
    out, half = [], k // 2
    for i in range(len(ys)):
        a, b = max(0, i - half), min(len(ys), i + half + 1)
        out.append(sum(ys[a:b]) / (b - a))
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--task", default="basic_control")
    ap.add_argument("--seeds", type=int, default=50)
    args = ap.parse_args()

    curves = load_curves(args.task)
    aggs = eval_task(args.task, args.seeds)

    fig, ax = plt.subplots(2, 2, figsize=(13, 8))
    fig.suptitle(f"NeuralRail · {args.task}  —  trained PPO performance",
                 fontsize=15, fontweight="bold", x=0.5, y=0.98)

    if curves:
        t = [c["t"] for c in curves]
        # A: reward
        ax[0, 0].plot(t, _smooth([c["reward"] for c in curves]), color=HV, lw=1.8)
        ax[0, 0].set_title("Episode reward  ↑", fontsize=11, loc="left")
        ax[0, 0].set_xlabel("training steps")
        # B: energy per episode
        ax[0, 1].plot(t, _smooth([c["energy_kwh"] for c in curves]), color=TEAL, lw=1.8)
        ax[0, 1].set_title("Energy per episode (kWh)  ↓", fontsize=11, loc="left")
        ax[0, 1].set_xlabel("training steps")
        # C: arrival rate + delay
        axc = ax[1, 0]
        axc.plot(t, _smooth([c["arrival_rate"] * 100 for c in curves]), color=GREEN, lw=1.8, label="arrival %")
        axc.set_ylim(0, 105)
        axc.set_title("Arrival rate (%)  ↑   ·   delay  ↓", fontsize=11, loc="left")
        axc.set_xlabel("training steps")
        axd = axc.twinx()
        axd.plot(t, _smooth([c["total_delay"] for c in curves]), color=AMBER, lw=1.2, ls="--", label="delay")
        axd.set_ylabel("delay (steps)", color=AMBER)
        axd.tick_params(axis="y", colors=AMBER); axd.grid(False)
        axc.legend(loc="center right", facecolor=PANEL, edgecolor="#3a352f", labelcolor=INK, fontsize=8)
    else:
        for a in (ax[0, 0], ax[0, 1], ax[1, 0]):
            a.text(0.5, 0.5, "no training curves", ha="center", va="center", color=INK2)

    # D: RL vs baseline — energy bars, arrival % annotated
    axb = ax[1, 1]
    order = [k for k in ["no_control", "random", "greedy_priority", "rl_agent"] if k in aggs]
    energy = [aggs[k].energy_kwh for k in order]
    colors = [CTRL_COLOR.get(k, GREY) for k in order]
    bars = axb.bar([CTRL_LABEL[k] for k in order], energy, color=colors, width=0.62)
    axb.set_title(f"RL vs baselines — energy (kWh), n={args.seeds}", fontsize=11, loc="left")
    axb.set_ylabel("energy (kWh)")
    axb.grid(axis="x")
    for k, b in zip(order, bars):
        a = aggs[k]
        axb.text(b.get_x() + b.get_width() / 2, b.get_height(),
                 f"{a.energy_kwh:.0f} kWh\n{100*a.arrival_rate:.0f}% arr · {a.collisions:.1f} col",
                 ha="center", va="bottom", fontsize=8, color=INK)
    axb.margins(y=0.18)

    fig.tight_layout(rect=[0, 0, 1, 0.96])
    out = os.path.join(ROOT, "models", args.task, "performance.png")
    fig.savefig(out, dpi=130)
    print(f"saved {out}")


if __name__ == "__main__":
    main()
