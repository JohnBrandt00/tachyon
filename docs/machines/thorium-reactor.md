# Thorium Reactor

**Tier**: 1 — Early Power
**Type**: Single-block machine
**Category**: Power Generation

---

## Overview

The Thorium Reactor is the mod's starter power generator. It burns Thorium Ingots to produce RF energy, providing a simple and reliable early-game power source. No coolant, no complex setup — just fuel and go.

---

## Specifications

| Property | Value |
|----------|-------|
| RF Output | 500 RF/t |
| Internal Buffer | 100,000 RF |
| Max RF Extract | 500 RF/t per side |
| Fuel | Thorium Ingot |
| Fuel Duration | 6,000 ticks (5 minutes) per ingot |
| Light Emission | 13 when burning, 0 when idle |
| Hardness/Resistance | 5.0 / 6.0 |
| Tool | Pickaxe |

---

## GUI Layout

```
┌─────────────────────────────────────────┐
│          Thorium Reactor                │
│                                         │
│  [Energy Bar]     [Fuel Slot]           │
│   ██████████       ┌──┐                │
│   ██████████       │TH│                │
│   ██████████       └──┘                │
│   ██████████                            │
│   ██████████      Burn: 4:32            │
│   ██████████      500 RF/t              │
│                                         │
│  ┌─────────────────────────────┐        │
│  │     Player Inventory        │        │
│  └─────────────────────────────┘        │
└─────────────────────────────────────────┘
```

- **Energy Bar** (left, 16x52): Shows stored RF / 100,000
- **Fuel Slot** (center): Accepts Thorium Ingots only
- **Burn Timer**: Shows remaining burn time in minutes:seconds
- **RF/t Display**: Shows current output rate

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | burnTime (ticks remaining) |
| 1 | maxBurnTime (6000) |
| 2 | energy stored |
| 3 | energy capacity |

---

## Behavior

### Fuel Consumption
- Accepts only Thorium Ingots in fuel slot
- Consumes 1 ingot when burnTime reaches 0 and buffer is not full
- Won't consume fuel if energy buffer is completely full (100,000 RF)
- Keeps burning through the current fuel even if buffer fills mid-burn

### Energy Output
- Pushes energy to all 6 adjacent blocks each tick (auto-output)
- Distributes evenly across all accepting neighbors
- 500 RF/t total output regardless of neighbor count

### Visual
- **Blockstate**: `BURNING` boolean property
  - `burning=true`: Front face shows glowing core texture, emits light level 13
  - `burning=false`: Front face shows inactive texture, no light
- **Facing**: Horizontal FACING property (N/S/E/W) set on placement, front face faces player
- Particle effect: Flame + smoke particles from top when burning

---

## Crafting Recipe

```
[Reactor Plating] [Reactor Plating] [Reactor Plating]
[Reactor Plating] [Furnace]         [Reactor Plating]
[Reactor Plating] [Redstone Block]  [Reactor Plating]
```

*Requires Reactor Plating from the Alloy Forge (Lithium + Thorium), creating a dependency on Tier 2 processing to build additional reactors, but one reactor can be obtained via alternative means or a simpler bootstrap recipe.*

### Bootstrap Recipe (First Reactor)
```
[Thorium Ingot] [Thorium Ingot] [Thorium Ingot]
[Thorium Ingot] [Furnace]       [Thorium Ingot]
[Iron Ingot]    [Redstone Block] [Iron Ingot]
```

---

## Automation Notes

- Hopper inserts into fuel slot from any side
- Energy auto-pushes to neighbors — no extraction needed
- Simple and reliable: no meltdowns, no coolant, no maintenance
- Stack multiple reactors for more RF/t
- Each reactor is independent (no multiblock)

---

## Implementation Notes

### New Files
- `block/ThoriumReactorBlock.java` — FACING + BURNING blockstate, EntityBlock
- `block/entity/ThoriumReactorBlockEntity.java` — Fuel burning + energy generation
- `menu/ThoriumReactorMenu.java` — 1 fuel slot + ContainerData
- `screen/ThoriumReactorScreen.java` — Energy bar + burn indicator

### Registration
- `ModBlocks.java` — THORIUM_REACTOR block + item
- `ModBlockEntities.java` — THORIUM_REACTOR entity type
- `ModMenuTypes.java` — THORIUM_REACTOR menu type
- `ModCapabilities.java` — Energy storage capability
- `tachyonClient.java` — Screen registration

### Assets
- Blockstate: `thorium_reactor.json` (facing × burning variants)
- Block models: `thorium_reactor.json`, `thorium_reactor_on.json`
- Item model: `thorium_reactor.json`
- Textures: `thorium_reactor_front.png`, `thorium_reactor_front_on.png`, `thorium_reactor_side.png`, `thorium_reactor_top.png`
- GUI: `thorium_reactor.png`
- Loot table: `thorium_reactor.json`
- Tags: Add to `mineable/pickaxe`
