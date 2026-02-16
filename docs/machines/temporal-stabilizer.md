# Temporal Stabilizer

**Tier**: 4 — Machine Acceleration
**Type**: Single-block machine
**Category**: Utility / Automation

---

## Overview

The Temporal Stabilizer warps local spacetime to speed up adjacent machines. Place it next to any ticking block entity and it will effectively double (or triple) its processing speed. Extremely power-hungry — this is a late-game luxury for players who have RF to burn.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 5,000 RF/t (per accelerated block) |
| Internal Buffer | 2,000,000 RF |
| Speed Multiplier | 2x (base), 3x (upgraded with Graviton Crystal) |
| Range | Adjacent blocks only (6 sides) |
| Max Accelerated Blocks | 6 (one per side) |
| Hardness/Resistance | 6.0 / 10.0 |
| Tool | Pickaxe |

---

## Mechanics

### How It Works
Each tick, the Temporal Stabilizer calls `serverTick()` on adjacent block entities an extra time, effectively doubling their processing speed. With a Graviton Crystal upgrade, it calls the tick two extra times (3x total speed).

### Speed Tiers

| Mode | Extra Ticks | Effective Speed | RF/t per Block |
|------|------------|-----------------|----------------|
| Normal | +1 | 2x | 5,000 RF/t |
| Overclocked | +2 | 3x | 12,000 RF/t |

### Overclock Upgrade
- Place a Graviton Crystal in the upgrade slot
- Crystal is NOT consumed — stays in slot
- Increases from 2x to 3x speed
- Energy cost more than doubles (5k → 12k per block) — diminishing returns

### Compatible Blocks
Works with ANY `BlockEntity` that has a server tick method:
- Tachyon machines (Alloy Forge, Plasma Furnace, etc.)
- Vanilla furnaces, blast furnaces, smokers
- Other mod machines (if they use standard BlockEntity ticking)
- Does NOT stack — two stabilizers on the same block don't give 4x

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│          Temporal Stabilizer                 │
│                                              │
│  [Energy]    [Upgrade Slot]                  │
│   ██████      ┌──┐                          │
│   ██████      │GC│  Mode: Overclocked (3x)  │
│   ██████      └──┘                          │
│   ██████                                     │
│   ██████    Accelerating: 3 blocks           │
│             Cost: 36,000 RF/t                │
│                                              │
│  ┌──────────────────────────────────┐        │
│  │     Player Inventory             │        │
│  └──────────────────────────────────┘        │
└──────────────────────────────────────────────┘
```

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | energy stored |
| 1 | energy capacity |
| 2 | accelerated block count |
| 3 | mode (0=normal 2x, 1=overclocked 3x) |

---

## Behavior

### Energy Priority
- If not enough energy for all adjacent blocks, prioritizes by direction:
  - Down > Up > North > South > East > West
- Blocks without enough energy that tick are skipped

### Anti-Stacking
- Checks if a target block is already being accelerated by another Temporal Stabilizer
- If so, skips that block (prevents exploit stacking)
- Uses a transient tag/capability flag set at the start of each tick

### Edge Cases
- Does NOT accelerate other Temporal Stabilizers (prevents infinite recursion)
- Does NOT accelerate the Singularity Engine (too dangerous — would bypass stability calculations)
- Does NOT accelerate chunk loaders or other infrastructure blocks

---

## Visual

- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Clock-like particle effect (spinning particles), time-distortion shimmer
  - `active=false`: Dormant
- Particle: Cyan clock-hand particles orbiting the block, speed lines toward adjacent machines
- Sound: Ticking/clicking sound, faster when overclocked

---

## Crafting Recipe

```
[Tachyon Alloy]    [Clock]           [Tachyon Alloy]
[Energy Crystal]   [Tachyon Core]    [Energy Crystal]
[Reactor Plating]  [Redstone Block]  [Reactor Plating]
```

---

## Implementation Notes

### New Files
- `block/TemporalStabilizerBlock.java`
- `block/entity/TemporalStabilizerBlockEntity.java`
- `menu/TemporalStabilizerMenu.java`
- `screen/TemporalStabilizerScreen.java`

### Technical Details
- In `serverTick()`: iterate over 6 adjacent positions
- For each: get `BlockEntity`, check it's not a stabilizer or blacklisted
- Call the block entity's tick method extra times
- NeoForge: Use `BlockEntityTicker` from the block's `getTicker()` result
- Anti-stack: Use a `HashSet<BlockPos>` stored in a level capability or transient field
