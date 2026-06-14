"""Pydantic request/response schemas for the API."""

from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, Field


class StartRequest(BaseModel):
    task: str = Field(default="basic_control")
    mode: str = Field(default="rl", description="rl | no_control | random | greedy_priority | manual")
    seed: int = Field(default=0)


class StepRequest(BaseModel):
    n: int = Field(default=1, ge=1, le=200)


class ModeRequest(BaseModel):
    mode: str


class OverrideRequest(BaseModel):
    kind: str = Field(..., description="'hold' | 'release' | 'signal'")
    target: str = Field(..., description="train_id (hold/release) or segment_id (signal)")
    value: Optional[str] = Field(default=None, description="signal state for kind='signal'")


class CompareRequest(BaseModel):
    task: str = "basic_control"
    seed: int = 0
    modes: Optional[list[str]] = None
