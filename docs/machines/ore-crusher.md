# Ore Crusher

**Tier**: 1 — Early Processing
**Type**: Single-block machine
**Category**: Ore Processing

---

## Overview

The Ore Crusher grinds raw ores into crushed ore dust, doubling your ingot output. Every tech mod needs ore doubling — this is ours. It's the first machine most players will build, and it stays useful through endgame.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 200 RF/t |
| Internal Buffer | 50,000 RF |
| Process Time | 80 ticks (4 seconds) |
| Input Slot | 1 |
| Output Slot | 1 |
| Hardness/Resistance | 4.0 / 6.0 |
| Tool | Pickaxe |

---

## Recipes

| Input | Output | Notes |
|-------|--------|-------|
| Raw Titanium | 2x Crushed Titanium | Smelt dust → Titanium Ingot |
| Raw Tungsten | 2x Crushed Tungsten | Smelt dust → Tungsten Ingot |
| Raw Lithium | 2x Crushed Lithium | Smelt dust → Lithium Ingot |
| Raw Thorium | 2x Crushed Thorium | Smelt dust → Thorium Ingot |
| Raw Iron | 2x Crushed Iron | Smelt dust → Iron Ingot |
| Raw Gold | 2x Crushed Gold | Smelt dust → Gold Ingot |
| Raw Copper | 2x Crushed Copper | Smelt dust → Copper Ingot |
| Cobblestone | 1x Sand | Utility recipe |
| Gravel | 1x Flint | Utility recipe |
| Blaze Rod | 4x Blaze Powder | Better than crafting (2x) |

*Crushed ore dusts are new items that smelt into their respective ingots in a furnace/blast furnace.*

---

## GUI Layout

```
┌──────────────────────────────────────────┐
│            Ore Crusher                   │
│                                          │
│  [Energy]     [Input]    ──►    [Output] │
│   ██████       ┌──┐    Crush    ┌──┐    │
│   ██████       │  │     ──►     │  │    │
│   ██████       └──┘             └──┘    │
│   ██████                                 │
│                200 RF/t                  │
│                                          │
│  ┌──────────────────────────────┐        │
│  │     Player Inventory         │        │
│  └──────────────────────────────┘        │
└──────────────────────────────────────────┘
```

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | progress (ticks) |
| 1 | maxProgress (80) |
| 2 | energy stored |
| 3 | energy capacity |

---

## Behavior

### Processing
- Accepts raw ores and specific vanilla items
- Always produces output — won't start without room in output slot
- Pauses on power loss, progress preserved
- Resets on input removal

### Energy
- Accepts from all sides
- 200 RF/t — cheap to run, designed for Thorium Reactor (500 RF/t) to power it easily
- Can run 2 crushers off one reactor with energy to spare

### Visual
- **Blockstate**: `ACTIVE` boolean + `FACING` horizontal
  - `active=true`: Front shows spinning crusher animation, light level 4
  - `active=false`: Idle
- Sound: Grinding/crushing sound when active
- Particles: Small rock particles when processing

---

## Crafting Recipe

```
[Iron Ingot]    [Flint]          [Iron Ingot]
[Cobblestone]   [Piston]         [Cobblestone]
[Iron Ingot]    [Redstone]       [Iron Ingot]
```

*Intentionally cheap — this is an early-game machine.*

---

## New Items Required

### Crushed Ore Dusts (7 items)
| Item | Registry Name | Smelts Into |
|------|--------------|-------------|
| Crushed Titanium | `crushed_titanium` | Titanium Ingot |
| Crushed Tungsten | `crushed_tungsten` | Tungsten Ingot |
| Crushed Lithium | `crushed_lithium` | Lithium Ingot |
| Crushed Thorium | `crushed_thorium` | Thorium Ingot |
| Crushed Iron | `crushed_iron` | Iron Ingot |
| Crushed Gold | `crushed_gold` | Gold Ingot |
| Crushed Copper | `crushed_copper` | Copper Ingot |

Each dust needs:
- Item registration in ModItems
- Item model JSON
- Item texture PNG
- Furnace + Blast Furnace smelting recipes

---

## Automation

- Top: Input slot
- Bottom: Output extraction
- Sides: Energy input
- Works with hoppers and item pipes

---

## Implementation Notes

### New Files
- `block/OreCrusherBlock.java`
- `block/entity/OreCrusherBlockEntity.java`
- `menu/OreCrusherMenu.java`
- `screen/OreCrusherScreen.java`

### Registration
- Standard block/entity/menu/screen/capability registration
- 7 new crushed ore items + models + textures + smelting recipes
