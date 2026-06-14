"""
Economic / environmental conversion constants.

Approximate traction electricity at ~5 / kWh and a grid emissions factor of
~0.8 kg CO₂ / kWh turn the physics-based kWh figures into the cost and CO₂ KPIs
shown in the UI and used in the agent-vs-baseline numbers.
"""

PRICE_PER_KWH_INR = 5.0      # currency units per kWh (traction electricity, approx.)
CO2_KG_PER_KWH = 0.8         # kg CO₂ per kWh (grid factor, approx.)


def kwh_to_inr(kwh: float) -> float:
    """Energy cost in rupees."""
    return kwh * PRICE_PER_KWH_INR


def kwh_to_co2_kg(kwh: float) -> float:
    """Carbon emissions in kg CO₂."""
    return kwh * CO2_KG_PER_KWH
