"""
Railway domain models (vendored & de-coupled from the original gym-env).

These are plain pydantic models — no FastMCP / openenv-core dependency — so the
simulator can run fully in-process for fast RL training and live serving.

Origin: railway_controller_gym_env/models.py (HoneyBadger-010), trimmed to the
fields the simulator actually uses.
"""

from __future__ import annotations

from enum import Enum
from typing import Dict, List, Optional

from pydantic import BaseModel, Field


class SignalState(str, Enum):
    """Signal aspects for a track segment (block entry control)."""

    RED = "red"        # stop
    YELLOW = "yellow"  # caution / wait one step
    GREEN = "green"    # proceed if block clear


class TrainStatus(str, Enum):
    """Lifecycle status of a train."""

    WAITING = "waiting"   # blocked at a signal / held
    MOVING = "moving"     # advancing on the network
    ARRIVED = "arrived"   # reached destination on time
    DELAYED = "delayed"   # reached destination behind schedule


class TrainState(BaseModel):
    """State of a single train."""

    train_id: str = Field(..., description="Unique train identifier")
    current_segment: str = Field(..., description="Current track segment ID")
    destination: str = Field(..., description="Destination segment ID")
    status: TrainStatus = Field(default=TrainStatus.MOVING)
    speed: float = Field(default=0.0, ge=0.0, le=1.0, description="Current speed (0-1)")
    scheduled_arrival: int = Field(..., description="Scheduled arrival step")
    delay: int = Field(default=0, ge=0, description="Delay in steps")
    priority: int = Field(
        default=1, ge=1, le=3,
        description="Gym priority (1=regular, 2=express, 3=high-speed). "
                    "NOTE: this is the OPPOSITE convention to NeuralRail's "
                    "TRAIN_TYPES priority (where lower = more important). The "
                    "physics mapper converts via train_type, never via this int.",
    )
    train_type: str = Field(
        default="regular",
        description="Train class: 'regular' | 'express' | 'high-speed' | 'freight'",
    )

    def get_priority_name(self) -> str:
        return {3: "high-speed", 2: "express", 1: "regular"}.get(self.priority, "regular")


class TrackSegment(BaseModel):
    """A track segment ('block') in the railway network."""

    segment_id: str = Field(..., description="Unique segment identifier")
    length: float = Field(..., description="Segment length in travel-time steps")
    signal_state: SignalState = Field(default=SignalState.GREEN)
    occupied_by: Optional[str] = Field(default=None, description="Train ID occupying this block")
    next_segments: List[str] = Field(default_factory=list, description="Connected segment IDs")
    is_junction: bool = Field(default=False, description="Whether this is a junction block")
    station_name: Optional[str] = Field(default=None, description="Station name, if any")


class RailwayObservation(BaseModel):
    """Full network observation (god's-eye view for the central controller)."""

    trains: Dict[str, TrainState] = Field(default_factory=dict)
    track_segments: Dict[str, TrackSegment] = Field(default_factory=dict)
    current_step: int = Field(default=0, ge=0)
    max_steps: int = Field(default=50, ge=1)
    collisions: int = Field(default=0, ge=0)
    message: str = Field(default="")


class TaskResult(BaseModel):
    """Scored result of an episode (used for evaluation / grading)."""

    task_name: str
    score: float = Field(ge=0.0, le=1.0)
    trains_arrived: int
    trains_delayed: int
    collisions: int
    avg_delay: float
    message: str = ""
