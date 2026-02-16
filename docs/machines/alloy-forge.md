# Alloy Forge

**Tier**: 2 — Material Processing
**Type**: Single-block machine
**Category**: Processing

---

## Overview

The Alloy Forge combines two different ingots into advanced alloys using heat and RF energy. It's the gateway to mid-game materials — without it, you can't craft Tachyon Alloy, Reactor Plating, or Exotic Shards.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 1,000 RF/t |
| Internal Buffer | 250,000 RF |
| Process Time | 150 ticks (7.5 seconds) |
| Input Slots | 2 (one for each ingot type) |
| Output Slot | 1 |
| Hardness/Resistance | 5.0 / 6.0 |
| Tool | Pickaxe |

---

## Recipes

| Input 1 | Input 2 | Output | RF Cost |
|---------|---------|--------|---------|
| Titanium Ingot | Tungsten Ingot | Tachyon Alloy Ingot | 150,000 RF |
| Lithium Ingot | Thorium Ingot | Reactor Plating | 150,000 RF |
| Tachyon Alloy Ingot | Exotic Matter | Exotic Shard | 150,000 RF |
| Iron Ingot | Tachyon Shard | Energy Crystal | 150,000 RF |
| Tachyon Alloy Ingot | Graviton Crystal | Quantum Processor | 150,000 RF |

*Recipes are order-independent — inputs can go in either slot.*

---

## GUI Layout

```
┌──────────────────────────────────────────┐
│            Alloy Forge                   │
│                                          │
│  [Energy]   [In 1]              [Out]    │
│   ██████     ┌──┐    ──►        ┌──┐    │
│   ██████     │  │   Progress     │  │    │
│   ██████     └──┘    ──►        └──┘    │
│   ██████     ┌──┐                        │
│   ██████     │  │                        │
│              └──┘                        │
│             [In 2]                       │
│                                          │
│  ┌──────────────────────────────┐        │
│  │     Player Inventory         │        │
│  └──────────────────────────────┘        │
└──────────────────────────────────────────┘
```

- **Energy Bar** (left, 16x52): Stored RF / 250,000
- **Input Slot 1** (52, 27): First ingredient
- **Input Slot 2** (52, 47): Second ingredient
- **Progress Arrow** (79, 34): Shows processing progress
- **Output Slot** (116, 35): Result

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | progress (ticks) |
| 1 | maxProgress (150) |
| 2 | energy stored |
| 3 | energy capacity |

---

## Behavior

### Processing
- Checks both input slots for a valid recipe combination
- Recipe matching is order-independent (slot 1 ↔ slot 2 interchangeable)
- Consumes 1 of each input, produces 1 output
- Won't start if output slot is full or contains a different item
- Pauses if energy runs out, resumes when power returns (progress preserved)
- Resets progress if inputs are removed mid-craft

### Energy
- Accepts energy from all 6 sides
- Consumes 1,000 RF/t while processing
- Idles at 0 RF/t when not processing
- No auto-output of energy

### Visual
- **Blockstate**: `ACTIVE` boolean + `FACING` horizontal
  - `active=true`: Front shows molten glow, light level 10
  - `active=false`: Front shows inactive, no light
- Particle: Lava drip particles from front when active

---

## Crafting Recipe

```
[Tungsten Ingot]  [Furnace]        [Tungsten Ingot]
[Titanium Ingot]  [Blast Furnace]  [Titanium Ingot]
[Iron Block]      [Redstone Block]  [Iron Block]
```

---

## Automation

- Hoppers/pipes insert into input slots from top and sides
- Hoppers/pipes extract from output slot from bottom
- Side-configurable: top = input 1, sides = input 2, bottom = output
- Energy accepted from all directions

---

## Implementation Notes

### New Files
- `block/AlloyForgeBlock.java` — FACING + ACTIVE, EntityBlock, menu provider
- `block/entity/AlloyForgeBlockEntity.java` — Dual input processing + energy
- `menu/AlloyForgeMenu.java` — 2 input + 1 output + ContainerData
- `screen/AlloyForgeScreen.java` — Energy bar + progress arrow

### Registration
- `ModBlocks.java` — ALLOY_FORGE
- `ModBlockEntities.java` — ALLOY_FORGE
- `ModMenuTypes.java` — ALLOY_FORGE
- `ModCapabilities.java` — Energy + Item handler
- `tachyonClient.java` — Screen

### New Items Required
- `TACHYON_ALLOY_INGOT` — registered in ModItems
- `REACTOR_PLATING` — registered in ModItems
- `EXOTIC_SHARD` — registered in ModItems
- `ENERGY_CRYSTAL` — registered in ModItems
- `QUANTUM_PROCESSOR` — registered in ModItems
