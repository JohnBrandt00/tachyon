# Plasma Furnace

**Tier**: 2 — Advanced Smelting
**Type**: Single-block machine
**Category**: Processing

---

## Overview

The Plasma Furnace is an RF-powered super-smelter. It processes any furnace recipe at 5x the speed of a blast furnace, and can handle up to 4 items simultaneously in parallel slots. High energy cost, but unmatched throughput.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 2,000 RF/t (per active slot) |
| Internal Buffer | 1,000,000 RF |
| Process Time | 20 ticks (1 second) per item |
| Input Slots | 4 (parallel processing) |
| Output Slots | 4 (one per input) |
| Max RF/t | 8,000 RF/t (all 4 slots active) |
| Hardness/Resistance | 5.0 / 8.0 |
| Tool | Pickaxe |

---

## Behavior

### Multi-Slot Processing
- 4 independent processing lanes, each with its own input and output slot
- Each lane operates independently — can smelt different items simultaneously
- Each active lane costs 2,000 RF/t
- If power is insufficient for all lanes, processes in order (lane 1 first, then 2, etc.)

### Recipe Compatibility
- Accepts ALL vanilla furnace recipes
- Accepts ALL vanilla blast furnace recipes (at same speed)
- Accepts ALL mod smelting recipes (crushed ores, etc.)
- Does NOT accept smoker recipes (it's not for food!)

### Speed
- 20 ticks per item = 5x faster than blast furnace (100 ticks)
- 10x faster than regular furnace (200 ticks)
- At full throughput: 4 items per second

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│            Plasma Furnace                    │
│                                              │
│  [Energy]  [In1] ──► [Out1]   ████ 8000 RF/t│
│   ██████   [In2] ──► [Out2]                  │
│   ██████   [In3] ──► [Out3]                  │
│   ██████   [In4] ──► [Out4]                  │
│   ██████                                     │
│                                              │
│  ┌──────────────────────────────────┐        │
│  │     Player Inventory             │        │
│  └──────────────────────────────────┘        │
└──────────────────────────────────────────────┘
```

- 4 progress arrows (one per lane)
- Each arrow shows independent progress
- Energy bar on left
- RF/t readout showing current consumption

---

## ContainerData (10 slots)

| Index | Data |
|-------|------|
| 0-3 | progress per lane (ticks) |
| 4-7 | maxProgress per lane (20) |
| 8 | energy stored |
| 9 | energy capacity |

---

## Visual

- **Blockstate**: `ACTIVE` boolean + `FACING` horizontal
  - `active=true`: Front shows plasma glow (intense blue-white), light level 15
  - `active=false`: Dark front
- Particle: Blue plasma spark particles
- Sound: Humming electrical sound when active

---

## Crafting Recipe

```
[Tachyon Alloy]    [Nether Brick]    [Tachyon Alloy]
[Reactor Plating]  [Blast Furnace]   [Reactor Plating]
[Tachyon Alloy]    [Redstone Block]  [Tachyon Alloy]
```

---

## Automation

- Top: All 4 input slots (round-robin distribution)
- Sides: Input (fills first empty input slot)
- Bottom: All 4 output slots
- Great with item pipes that can target specific slots

---

## Implementation Notes

### New Files
- `block/PlasmaFurnaceBlock.java`
- `block/entity/PlasmaFurnaceBlockEntity.java`
- `menu/PlasmaFurnaceMenu.java`
- `screen/PlasmaFurnaceScreen.java`

### Technical Details
- Use `level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, ...)` to check any furnace recipe
- Also check `RecipeType.BLASTING` for blast furnace recipes
- Each lane has independent progress tracking
- Energy priority: lane 1 > 2 > 3 > 4 when power is limited
