# NeuralRail - AI Decision Making System

> How the AI decides which train to stop, slow, or let pass during conflicts

---

## 🎯 THE BIG QUESTION: Priority vs Energy?

### Answer: **PRIORITY FIRST, ENERGY SECOND**

This is the most important design decision in NeuralRail:

| Decision Level | What We Consider | Why |
|----------------|------------------|-----|
| **Level 1** | Train Priority | Indian Railways operates on strict hierarchy - you CAN'T stop a Rajdhani for a freight train |
| **Level 2** | Energy Optimization | WITHIN the same priority level, or when choosing HOW to resolve (slow vs stop) |
| **Level 3** | Exception | Only when energy savings are MASSIVE (>1000 kWh) can priority be overridden |

### Why Not Energy First?

1. **Operational Reality:** Indian Railways has 13,000+ trains daily. Priority system ensures predictable operations.
2. **Passenger Expectations:** Rajdhani passengers paid premium fares for on-time arrival.
3. **Safety:** Priority system is well-understood by all staff. Changing it creates confusion.
4. **Legal:** Railway Board circulars mandate priority adherence.

### When Energy CAN Override Priority

| Scenario | Energy Savings | Override Allowed? |
|----------|----------------|-------------------|
| Normal conflict | 100-500 kWh | ❌ NO - Follow priority |
| Gradient conflict | 500-800 kWh | ⚠️ MAYBE - Case by case |
| Extreme gradient | >1000 kWh | ✅ YES - Justified override |

**Example:** Stopping a 4000-ton freight on Bhor Ghat uphill costs 500 kWh extra. If the alternative is stopping a Rajdhani (which costs only 200 kWh), the 300 kWh savings does NOT justify stopping the Rajdhani. But if the freight is mid-ghat and stopping it costs 1200 kWh total, then letting it continue (even if it delays an Express) is justified.

---

## ⚡ WHEN ENERGY DECIDES: The 5 Scenarios

### Scenario 1: Same Priority Conflict (Energy DECIDES)

**Situation:** Two trains with SAME priority conflict (e.g., two Rajdhanis)

**Decision Logic:**
```
IF train_a.priority == train_b.priority:
    COMPARE energy_to_stop(train_a) vs energy_to_stop(train_b)
    STOP the one with LOWER energy cost
```

**Example:** Rajdhani A (850 tons) vs Rajdhani B (900 tons)
- Stop A: 680 kWh
- Stop B: 720 kWh
- **Decision: Stop A (saves 40 kWh)**

---

### Scenario 2: Different Priority (Energy OPTIMIZES)

**Situation:** Different priority trains conflict (e.g., Rajdhani vs Freight)

**Decision Logic:**
```
ALWAYS stop lower priority train
BUT choose HOW to stop based on energy:
  - Option 1: Full stop (higher energy, faster resolution)
  - Option 2: Slow down (lower energy, slower resolution)
  - Option 3: Slow + Stop combo (optimal energy)
```

**Example:** Rajdhani (P2) vs Freight (P6)
- Stop Freight immediately: 1500 kWh
- Slow Rajdhani 20% + Stop Freight: 1200 kWh ✅ BETTER
- **Decision: Slow Rajdhani, Stop Freight (saves 300 kWh)**

---

### Scenario 3: Gradient Situation (Energy WARNS)

**Situation:** Train on uphill gradient (ghat section)

**Decision Logic:**
```
IF train_on_uphill AND train_mass > 1000 tons:
    ADD gradient_penalty to energy calculation
    WARN: "Stopping here costs extra energy"
    PREFER stopping on flat section if possible
```

**Gradient Penalties:**
| Train Mass | Gradient Penalty |
|------------|------------------|
| 4000+ tons (Heavy Freight) | +500 kWh |
| 1500-4000 tons (Express) | +300 kWh |
| 800-1500 tons (Local) | +150 kWh |
| <800 tons (Light) | +80 kWh |

**Example:** Freight on Bhor Ghat (1.83% gradient)
- Stop on uphill: 1500 + 500 = 2000 kWh
- Let it clear ghat, stop on flat: 1500 kWh
- **Decision: Wait for flat section (saves 500 kWh)**

---

### Scenario 4: Regenerative Braking (Energy RECOVERS)

**Situation:** Electric vs Diesel train

**Decision Logic:**
```
IF train.traction == 'electric':
    energy_loss = braking_energy × 0.70  # 30% recovered
ELSE:
    energy_loss = braking_energy × 1.00  # 0% recovered
```

**Example:** Stop Electric EMU vs Diesel DEMU
- Electric EMU braking: 500 kWh × 0.70 = 350 kWh net loss
- Diesel DEMU braking: 500 kWh × 1.00 = 500 kWh net loss
- **Decision: Prefer stopping Electric (saves 150 kWh)**

---

### Scenario 5: Massive Savings Override (Energy OVERRIDES Priority)

**Situation:** Energy savings > 1000 kWh

**Decision Logic:**
```
IF energy_savings > 1000 kWh:
    ALLOW priority override
    LOG: "Priority override justified by {savings} kWh savings"
ELSE:
    FOLLOW priority strictly
```

**Example:** Express (P3) vs Freight on steep uphill (P6)
- Stop Freight on uphill: 2000 kWh
- Stop Express on flat: 600 kWh
- Savings: 1400 kWh > 1000 kWh threshold
- **Decision: Stop Express (saves 1400 kWh) - PRIORITY OVERRIDE JUSTIFIED**

---

## 📊 Energy Calculation Formula

### Total Energy Cost of Stopping a Train

```
Total Energy = Braking Energy 
             + Idle Energy 
             + Restart Energy 
             + Gradient Penalty
             - Regenerative Recovery
```

### Component Calculations

| Component | Formula | Example (Rajdhani 850 tons, 100 km/h) |
|-----------|---------|---------------------------------------|
| **Braking Energy** | 0.5 × mass × velocity² | 0.5 × 850,000 × 27.78² = 328 MJ = 91 kWh |
| **Regenerative Recovery** | Braking × 0.30 (electric) | 91 × 0.30 = 27 kWh recovered |
| **Net Braking Loss** | Braking - Recovery | 91 - 27 = 64 kWh |
| **Idle Energy** | Power × Time | 180 kW × 0.1 hr = 18 kWh |
| **Restart Energy** | (ΔKE + Resistance) / 0.85 | 91 / 0.85 = 107 kWh |
| **Gradient Penalty** | Based on mass & gradient | 0 kWh (flat section) |
| **TOTAL** | Sum of all | 64 + 18 + 107 + 0 = **189 kWh** |

---

## 🎉 Festival Special Trains - Priority 2

### Why Festival Trains Get High Priority

Festival Special trains run during:
- **Diwali** (October-November)
- **Holi** (March)
- **Chhath Puja** (November)
- **Durga Puja** (October)
- **Ganesh Chaturthi** (September)
- **Kumbh Mela** (Every 12 years)
- **Summer Holidays** (May-June)

**These trains carry millions of passengers** going home for festivals. Delaying them causes:
- Missed family reunions
- Missed religious ceremonies
- Massive public outcry
- Political consequences

### Festival Train Types in NeuralRail

| Train Type | Priority | Capacity | When Runs |
|------------|----------|----------|-----------|
| Festival Special | 2 | 1800 | Diwali, Holi, Chhath |
| Puja Special | 2 | 1600 | Durga Puja, Ganesh Chaturthi |
| Summer Special | 2 | 1500 | May-June holidays |
| Kumbh Special | 2 | 2000 | Kumbh Mela (millions of pilgrims) |

**Key Point:** Festival Specials get **Priority 2** (same as Rajdhani/Shatabdi) because:
1. They carry more passengers than regular trains
2. Time-sensitive (festival dates are fixed)
3. Social importance (family reunions)
4. Government mandate (Railway Board orders)

---

## 1. Indian Railways Official Train Priority System

### Priority Classes (Highest to Lowest)

| Priority | Class | Train Types | Examples |
|----------|-------|-------------|----------|
| **1** | **Special** | President's Special, VVIP trains, Military Specials, Accident Relief | Saloon trains, Defence movements, ART |
| **2** | **Superfast Express** | Rajdhani, Shatabdi, Duronto, Vande Bharat, Tejas, Gatimaan | 12301 Rajdhani, 12009 Shatabdi, 22439 Vande Bharat |
| **3** | **Mail/Express** | Regular Express trains, Jan Shatabdi | 12137 Punjab Mail, Deccan Express |
| **4** | **Passenger** | Ordinary passenger trains, MEMU, DEMU | Slow passenger trains |
| **5** | **Suburban/EMU** | Local trains, Metro | Delhi Ring Railway, Chennai MRTS, Kolkata EMU |
| **6** | **Freight** | Goods trains | Container, Coal rake, Oil tankers, Parcel |

---

## 2. Priority Implementation in NeuralRail

### Train Type to Priority Mapping

```python
TRAIN_PRIORITIES = {
    # Priority 1 - Special (Not in demo, but system supports)
    'special': 1,
    'military': 1,
    'accident_relief': 1,
    
    # Priority 2 - Superfast Express
    'rajdhani': 2,
    'shatabdi': 2,
    'vande_bharat': 2,
    'duronto': 2,
    'tejas': 2,
    'gatimaan': 2,
    
    # Priority 3 - Mail/Express
    'mail_express': 3,
    'express_passenger': 3,
    'jan_shatabdi': 3,
    
    # Priority 4 - Passenger
    'passenger': 4,
    'memu': 4,
    'demu': 4,
    
    # Priority 5 - Suburban
    'local_emu': 5,
    'suburban': 5,
    'metro': 5,
    
    # Priority 6 - Freight
    'freight': 6,
    'freight_heavy': 6,
    'freight_container': 6,
    'freight_tanker': 6
}
```

---

## 3. Decision Criteria (In Order of Importance)

### Level 1: Safety (Non-Negotiable)
- **Collision avoidance is ALWAYS the top priority**
- All generated solutions must be safe
- No solution that risks collision is ever recommended

### Level 2: Train Priority
- Higher priority trains should NOT be stopped for lower priority trains
- Example: Rajdhani (P2) should not stop for Freight (P6)
- **Priority Violation Penalty: +500 points to solution score**

### Level 3: Energy Efficiency
- Prefer solutions that consume less energy
- Consider regenerative braking (30% recovery for electric trains)
- Account for gradient penalties (uphill restart costs more)

### Level 4: Time/Delay Impact
- Minimize total delay to the railway network
- Consider which train can better absorb delays
- Freight trains have more flexible schedules

---

## 4. Scoring Formula

Each solution is scored (lower score = better solution):

```
Total Score = (Energy Score × 0.4) 
            + (Delay Score × 0.2) 
            + Priority Penalty 
            + (Train Priority Factor × 0.4)
```

### Score Components

| Component | Calculation | Weight |
|-----------|-------------|--------|
| **Energy Score** | `energy_kwh / 10` (max 100) | 40% |
| **Delay Score** | `delay_minutes × 2` (max 50) | 20% |
| **Priority Penalty** | +500 if stopping higher priority train | Fixed |
| **Priority Factor** | `train_priority × 10` | 40% |

### Example Calculation

**Scenario:** Stop Freight (Priority 6) to let Rajdhani (Priority 2) pass

```
Energy: 850 kWh → Score = 850/10 = 85 × 0.4 = 34
Delay: 8 minutes → Score = 8 × 2 = 16 × 0.2 = 3.2
Priority Penalty: 0 (not stopping higher priority)
Priority Factor: 6 × 10 = 60 × 0.4 = 24

Total Score = 34 + 3.2 + 0 + 24 = 61.2
```

**Scenario:** Stop Rajdhani (Priority 2) to let Freight (Priority 6) pass

```
Energy: 920 kWh → Score = 920/10 = 92 × 0.4 = 36.8
Delay: 6 minutes → Score = 6 × 2 = 12 × 0.2 = 2.4
Priority Penalty: 500 (stopping higher priority!) 
Priority Factor: 2 × 10 = 20 × 0.4 = 8

Total Score = 36.8 + 2.4 + 500 + 8 = 547.2
```

**Winner:** Stop Freight (Score 61.2) beats Stop Rajdhani (Score 547.2)

---

## 5. Solution Types

### Permanent Solutions (Preferred)
These actually resolve the conflict:

| Type | Description | When Used |
|------|-------------|-----------|
| **Stop** | One train stops completely at junction | Head-on conflicts |
| **Track Switch** | Train moves to parallel track | When parallel track available |

### Temporary Solutions (Fallback)
These only delay the conflict:

| Type | Description | When Used |
|------|-------------|-----------|
| **Slow** | One train reduces speed by 25% | Minor conflicts |
| **Both Slow** | Both trains reduce speed by 15% | When no permanent solution possible |

### Multi-Step Solutions (Best)
Combination approach:

| Type | Description | When Used |
|------|-------------|-----------|
| **Slow + Stop** | High priority slows, low priority stops | Head-on with time to react |

---

## 6. Special Considerations

### Gradient Impact (Ghat Sections)

| Situation | Energy Penalty | Reason |
|-----------|----------------|--------|
| Stop heavy train on uphill | +500 kWh | Physics-based restart energy against gravity |
| Stop light train on uphill | +400 kWh | Moderate restart energy |
| Stop on flat/downhill | No penalty | Normal restart |

**Example:** In Bhor Ghat (1.83% gradient), stopping a 4000-ton freight train uphill costs ~500 kWh extra to restart (potential energy + acceleration + rolling resistance).

### Regenerative Braking

| Train Type | Braking Energy Recovery |
|------------|------------------------|
| Electric (EMU, WAP) | 30% recovered |
| Diesel (WDM, WDP) | 0% (wasted as heat) |

### Emergency Situations

| Time to Collision | Action |
|-------------------|--------|
| > 5 minutes | Generate all solution types |
| < 5 minutes | Only generate STOP solutions (no time for slow) |
| < 2 minutes | Emergency braking, alert control room |

---

## 7. Decision Modes

The system can operate in different modes:

| Mode | Energy | Delay | Priority | Use Case |
|------|--------|-------|----------|----------|
| **Balanced** | 40% | 20% | 40% | Normal operations |
| **Energy Priority** | 60% | 10% | 30% | Power shortage, green initiative |
| **Time Priority** | 20% | 50% | 30% | Rush hour, festival season |
| **Strict Priority** | 20% | 20% | 60% | VVIP movement, military |

---

## 8. Real-World Exceptions

### When Lower Priority Train Gets Preference

| Situation | Reason |
|-----------|--------|
| Medical emergency on freight | Patient needs hospital |
| Essential commodities | Food/fuel shortage in region |
| Almost at destination | Train 2 km from station vs 50 km |
| Massive energy savings | >1000 kWh difference justifies override |

### Platform Assignment

| Train Type | Platform Preference |
|------------|---------------------|
| Rajdhani/Shatabdi | Platform 1, 2 (Main) |
| Express | Platform 2, 3, 4 |
| Local/EMU | Platform 5, 6, 7 |
| Freight | Platform 9, 10, 11 (Odd/Less used) |

---

## 9. Decision Flow Chart

```
                    ┌─────────────────────┐
                    │   CONFLICT DETECTED │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Is it Emergency?   │
                    │   (< 5 minutes)     │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │ YES            │                │ NO
              ▼                │                ▼
    ┌─────────────────┐        │      ┌─────────────────┐
    │ Generate STOP   │        │      │ Generate ALL    │
    │ solutions only  │        │      │ solution types  │
    └────────┬────────┘        │      └────────┬────────┘
             │                 │               │
             └─────────────────┼───────────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Filter Invalid     │
                    │  (track constraints)│
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Separate:          │
                    │  PERMANENT vs       │
                    │  TEMPORARY          │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Within Permanent:  │
                    │  Priority-respecting│
                    │  FIRST              │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Sort by Energy     │
                    │  (lower = better)   │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  RECOMMEND BEST     │
                    │  SOLUTION           │
                    └─────────────────────┘
```

---

## 10. Example Scenarios

### Scenario 1: Rajdhani vs Freight (Head-On)

**Setup:**
- RAJ (Rajdhani, Priority 2) going forward at 100 km/h
- FRT (Freight, Priority 6) going backward at 50 km/h
- Both on Track 1, collision in 16 minutes

**AI Decision:**
1. ✅ **Stop FRT at Platform 9** - Best (no priority violation, permanent)
2. ⚠️ Stop RAJ - Bad (priority violation!)
3. ⚠️ Slow both - Temporary fix only

**Result:** FRT stops at Ghaziabad Platform 9, RAJ passes safely

### Scenario 2: Two Rajdhanis (Same Priority)

**Setup:**
- RAJ1 (Priority 2) going forward
- RAJ2 (Priority 2) going backward
- Same priority conflict

**AI Decision:**
1. Check which has traveled longer distance
2. Check which is running on time
3. Stop the one that can better absorb delay
4. Consider energy (which costs less to stop)

### Scenario 3: Freight on Uphill (Ghat Section)

**Setup:**
- FRT (Freight, 4000 tons) climbing Bhor Ghat
- EXP (Express, Priority 3) behind it

**AI Decision:**
1. ⚠️ Stop FRT on uphill = +500 kWh penalty (AVOID!)
2. ✅ Slow EXP, let FRT clear the ghat section
3. Even though EXP is higher priority, energy savings justify letting FRT continue

---

## 11. Summary

### The AI Always Considers:

1. **Safety First** - No solution risks collision
2. **Respect Priority** - Higher priority trains get preference
3. **Save Energy** - Choose energy-efficient solutions
4. **Minimize Delay** - Reduce total network impact
5. **Permanent > Temporary** - Prefer solutions that resolve conflict

### The AI Never:

- Recommends unsafe solutions
- Ignores train priorities without massive justification
- Chooses temporary fixes when permanent solutions exist
- Stops trains on dangerous gradients unnecessarily

---

*Document Version: 1.0*
*Last Updated: December 2024*
*For: Smart India Hackathon 2024*
