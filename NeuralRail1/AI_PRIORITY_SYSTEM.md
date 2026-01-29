# 🎯 NeuralRail AI PRIORITY DECISION SYSTEM

## The Core Question: What Matters Most?

Our AI considers **TWO main factors** when making decisions:

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                    AI DECISION FORMULA                               │
│                                                                      │
│         ┌─────────────────┐     ┌─────────────────┐                 │
│         │  TRAIN PRIORITY │  +  │ ENERGY SAVINGS  │  =  DECISION    │
│         │     (60%)       │     │     (40%)       │                 │
│         └─────────────────┘     └─────────────────┘                 │
│                                                                      │
│   RULE: Priority FIRST, Energy SECOND                               │
│   EXCEPTION: Energy overrides ONLY if savings > 1000 kWh            │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📊 FACTOR 1: TRAIN PRIORITY (60% Weight)

### Indian Railways Official 6-Tier Priority System

```
┌─────────────────────────────────────────────────────────────────────┐
│                    TRAIN PRIORITY HIERARCHY                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 1 ─ SPECIAL (Absolute - NEVER STOP)                      │
│   ════════════════════════════════════════════                      │
│   • President/PM Special                                            │
│   • Military Special                                                │
│   • Accident Relief Train (ART)                                     │
│   • Medical Emergency                                               │
│                                                                      │
│   AI Rule: NEVER recommend stopping P1 trains                       │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 2 ─ SUPERFAST EXPRESS (Critical)                         │
│   ════════════════════════════════════════════                      │
│   • Rajdhani Express                                                │
│   • Shatabdi Express                                                │
│   • Vande Bharat Express                                            │
│   • Duronto Express                                                 │
│   • Tejas Express                                                   │
│   • Gatimaan Express                                                │
│   • Festival Special (Diwali, Puja, Kumbh)                         │
│                                                                      │
│   AI Rule: Stop only if energy savings > 1500 kWh                   │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 3 ─ MAIL/EXPRESS (High)                                  │
│   ════════════════════════════════════════════                      │
│   • Regular Express (Deccan Queen, etc.)                            │
│   • Jan Shatabdi                                                    │
│   • Garib Rath                                                      │
│   • Sampark Kranti                                                  │
│                                                                      │
│   AI Rule: Stop only if energy savings > 800 kWh                    │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 4 ─ PASSENGER (Medium)                                   │
│   ════════════════════════════════════════════                      │
│   • Ordinary Passenger                                              │
│   • MEMU (Mainline EMU)                                             │
│   • DEMU (Diesel EMU)                                               │
│                                                                      │
│   AI Rule: Stop only if energy savings > 400 kWh                    │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 5 ─ SUBURBAN (Medium-Low)                                │
│   ════════════════════════════════════════════                      │
│   • Local EMU (Delhi Suburban)                                      │
│   • Metro Rail                                                      │
│   • Ring Railway                                                    │
│                                                                      │
│   AI Rule: Can be stopped for any significant energy saving         │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   PRIORITY 6 ─ FREIGHT (Lowest)                                     │
│   ════════════════════════════════════════════                      │
│   • Heavy Freight (Coal, Iron Ore)                                  │
│   • Container Freight (CONCOR)                                      │
│   • Tanker (Oil, Chemicals)                                         │
│   • Parcel/Postal                                                   │
│                                                                      │
│   AI Rule: Default choice to stop UNLESS energy cost is huge        │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔋 FACTOR 2: ENERGY SAVINGS (40% Weight)

### Energy Calculation Formula

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ENERGY CALCULATION                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   STOPPING ENERGY = ½ × Mass × Velocity²                            │
│                                                                      │
│   RESTART ENERGY  = Mass × Acceleration × Distance                  │
│                   + Gradient Penalty (if uphill)                    │
│                                                                      │
│   GRADIENT PENALTY = Mass × Gravity × Height_Gain                   │
│                    = Mass × 9.8 × (gradient% × distance)            │
│                                                                      │
│   TOTAL ENERGY WASTE = Stopping + Restart + Gradient                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### Energy Cost by Train Type

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ENERGY COST TABLE                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   ┌────────────────┬──────────┬───────────┬───────────┬──────────┐ │
│   │ Train Type     │ Mass     │ Typical   │ Stop      │ Restart  │ │
│   │                │ (tons)   │ Speed     │ Energy    │ Energy   │ │
│   ├────────────────┼──────────┼───────────┼───────────┼──────────┤ │
│   │ Vande Bharat   │ 430      │ 160 km/h  │ 280 kWh   │ 180 kWh  │ │
│   │ Rajdhani       │ 850      │ 140 km/h  │ 420 kWh   │ 350 kWh  │ │
│   │ Shatabdi       │ 720      │ 150 km/h  │ 400 kWh   │ 300 kWh  │ │
│   │ Express        │ 520      │ 110 km/h  │ 180 kWh   │ 150 kWh  │ │
│   │ Local EMU      │ 380      │ 80 km/h   │ 80 kWh    │ 70 kWh   │ │
│   │ Heavy Freight  │ 4200     │ 60 km/h   │ 600 kWh   │ 900 kWh* │ │
│   │ Container      │ 3500     │ 80 km/h   │ 550 kWh   │ 700 kWh  │ │
│   └────────────────┴──────────┴───────────┴───────────┴──────────┘ │
│                                                                      │
│   * Freight restart is HIGH because of mass + possible gradient     │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---


## 🧮 THE DECISION MATRIX

### How AI Combines Priority + Energy

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AI DECISION MATRIX                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   STEP 1: Calculate Priority Score (0-100)                          │
│   ─────────────────────────────────────────                         │
│   P1 (Special)    = 100 points (NEVER stop)                         │
│   P2 (Superfast)  = 85 points                                       │
│   P3 (Express)    = 70 points                                       │
│   P4 (Passenger)  = 50 points                                       │
│   P5 (Suburban)   = 30 points                                       │
│   P6 (Freight)    = 15 points                                       │
│                                                                      │
│   STEP 2: Calculate Energy Score (0-100)                            │
│   ─────────────────────────────────────────                         │
│   Energy to Stop + Restart = X kWh                                  │
│   Score = 100 - (X / 50)  [capped at 0-100]                        │
│                                                                      │
│   Low energy cost  = High score (good to stop)                      │
│   High energy cost = Low score (bad to stop)                        │
│                                                                      │
│   STEP 3: Calculate Final Score                                     │
│   ─────────────────────────────────────────                         │
│   Final Score = (Priority × 0.6) + (Energy × 0.4)                   │
│                                                                      │
│   STEP 4: Decision                                                  │
│   ─────────────────────────────────────────                         │
│   STOP the train with LOWER Final Score                             │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📋 EXAMPLE: Rajdhani vs Heavy Freight

```
┌─────────────────────────────────────────────────────────────────────┐
│                    EXAMPLE CALCULATION                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   CONFLICT: Rajdhani (P2) vs Heavy Freight (P6) on same track       │
│                                                                      │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    RAJDHANI (RAJ)                            │   │
│   ├─────────────────────────────────────────────────────────────┤   │
│   │ Priority Score    : 85 points (P2 Superfast)                │   │
│   │ Mass              : 850 tons                                 │   │
│   │ Speed             : 140 km/h                                 │   │
│   │ Stop Energy       : 420 kWh                                  │   │
│   │ Restart Energy    : 350 kWh                                  │   │
│   │ Total Energy      : 770 kWh                                  │   │
│   │ Energy Score      : 100 - (770/50) = 85 points              │   │
│   │                                                              │   │
│   │ FINAL SCORE = (85 × 0.6) + (85 × 0.4) = 51 + 34 = 85       │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    FREIGHT (FRT)                             │   │
│   ├─────────────────────────────────────────────────────────────┤   │
│   │ Priority Score    : 15 points (P6 Freight)                  │   │
│   │ Mass              : 4200 tons                                │   │
│   │ Speed             : 60 km/h                                  │   │
│   │ Stop Energy       : 600 kWh                                  │   │
│   │ Restart Energy    : 900 kWh (uphill)                        │   │
│   │ Total Energy      : 1500 kWh                                 │   │
│   │ Energy Score      : 100 - (1500/50) = 70 points             │   │
│   │                                                              │   │
│   │ FINAL SCORE = (15 × 0.6) + (70 × 0.4) = 9 + 28 = 37        │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
│   ════════════════════════════════════════════════════════          │
│   RESULT: Freight Score (37) < Rajdhani Score (85)                  │
│   DECISION: STOP FREIGHT, let Rajdhani pass                         │
│   ════════════════════════════════════════════════════════          │
│                                                                      │
│   BUT WAIT! Check Energy Override Rule...                           │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## ⚡ ENERGY OVERRIDE RULE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ENERGY OVERRIDE CONDITIONS                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   NORMAL RULE: Stop lower priority train                            │
│                                                                      │
│   OVERRIDE RULE: If energy difference > threshold, override!        │
│                                                                      │
│   ┌────────────────┬─────────────────────────────────────────────┐ │
│   │ Priority Gap   │ Energy Threshold to Override                │ │
│   ├────────────────┼─────────────────────────────────────────────┤ │
│   │ P6 vs P5       │ 200 kWh (easy to override)                  │ │
│   │ P6 vs P4       │ 400 kWh                                     │ │
│   │ P6 vs P3       │ 800 kWh                                     │ │
│   │ P6 vs P2       │ 1500 kWh (hard to override)                 │ │
│   │ P6 vs P1       │ NEVER (P1 is absolute)                      │ │
│   │ P5 vs P4       │ 300 kWh                                     │ │
│   │ P5 vs P3       │ 600 kWh                                     │ │
│   │ P5 vs P2       │ 1200 kWh                                    │ │
│   │ P4 vs P3       │ 500 kWh                                     │ │
│   │ P4 vs P2       │ 1000 kWh                                    │ │
│   │ P3 vs P2       │ 800 kWh                                     │ │
│   └────────────────┴─────────────────────────────────────────────┘ │
│                                                                      │
│   EXAMPLE:                                                          │
│   • Freight (P6) vs Rajdhani (P2)                                   │
│   • Stopping Freight saves: 1500 kWh                                │
│   • Stopping Rajdhani saves: 770 kWh                                │
│   • Difference: 1500 - 770 = 730 kWh                                │
│   • Threshold for P6 vs P2: 1500 kWh                                │
│   • 730 < 1500, so NO OVERRIDE                                      │
│   • DECISION: Stop Freight (normal priority rule)                   │
│                                                                      │
│   BUT IF Freight was going UPHILL:                                  │
│   • Stopping Freight: 1500 + 800 (gradient) = 2300 kWh              │
│   • Difference: 2300 - 770 = 1530 kWh                               │
│   • 1530 > 1500, so OVERRIDE!                                       │
│   • DECISION: Stop Rajdhani (energy override)                       │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 SUMMARY: PRIORITY SEQUENCE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FINAL PRIORITY SEQUENCE                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   1️⃣  SAFETY FIRST (Always)                                         │
│       • No collision ever                                           │
│       • All solutions must be safe                                  │
│                                                                      │
│   2️⃣  TRAIN PRIORITY (60% weight)                                   │
│       • P1 > P2 > P3 > P4 > P5 > P6                                │
│       • Higher priority trains get preference                       │
│                                                                      │
│   3️⃣  ENERGY OPTIMIZATION (40% weight)                              │
│       • Calculate energy cost for each option                       │
│       • Prefer options with lower energy waste                      │
│                                                                      │
│   4️⃣  ENERGY OVERRIDE (Exception)                                   │
│       • If energy savings exceed threshold                          │
│       • Override priority for massive savings                       │
│       • Never override P1 (Special trains)                          │
│                                                                      │
│   5️⃣  LOOP/TRACK UTILIZATION (Bonus)                                │
│       • Use loops instead of stopping                               │
│       • Use empty tracks for routing                                │
│       • Minimize full stops                                         │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---