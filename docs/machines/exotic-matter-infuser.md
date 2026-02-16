# Exotic Matter Infuser

**Tier**: 5 — Endgame Crafting
**Type**: Single-block machine
**Category**: Advanced Crafting

---

## Overview

The Exotic Matter Infuser is the endgame crafting station. It infuses base items with Exotic Shards to create the most powerful equipment in the mod. Requires a Graviton Crystal as a reusable catalyst and massive amounts of RF energy.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 10,000 RF/t |
| Internal Buffer | 5,000,000 RF |
| Process Time | 600 ticks (30 seconds) |
| Total RF per Craft | 6,000,000 RF |
| Input Slot 1 | Base item (tool/armor) |
| Input Slot 2 | Exotic Shard(s) |
| Catalyst Slot | Graviton Crystal (NOT consumed) |
| Output Slot | Infused result |
| Hardness/Resistance | 10.0 / 1200.0 |
| Tool | Pickaxe (requires diamond+) |

---

## Recipes

| Base Item | Exotic Shards | Output |
|-----------|--------------|--------|
| Tachyon Pickaxe | 4 | Singularity Pick |
| Tachyon Sword | 4 | Entropy Blade |
| Tachyon Core | 8 | Gravity Staff |
| Diamond | 2 | Void Prism |
| Tachyon Helmet | 4 | Exotic Helmet |
| Tachyon Chestplate | 4 | Exotic Chestplate |
| Tachyon Leggings | 4 | Exotic Leggings |
| Tachyon Boots | 4 | Exotic Boots |
| Quantum Processor | 4 | Spatial Anchor |
| Energy Crystal | 8 | Null Field Generator Core |

*All recipes require a Graviton Crystal in the catalyst slot (not consumed).*

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│         Exotic Matter Infuser                │
│                                              │
│  [Energy]  [Base]         [Catalyst]  [Out]  │
│   ██████    ┌──┐   ──►    ┌──┐       ┌──┐  │
│   ██████    │  │  Progress │GC│       │  │  │
│   ██████    └──┘   ──►    └──┘       └──┘  │
│   ██████    ┌──┐                             │
│   ██████    │ES│   30.0s                     │
│             └──┘                             │
│            [Shards]                          │
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
| 0 | progress (ticks) |
| 1 | maxProgress (600) |
| 2 | energy stored |
| 3 | energy capacity |

---

## Behavior

### Processing
- All 3 inputs required: base item, exotic shard(s), catalyst crystal
- Catalyst is NOT consumed — stays in slot after crafting
- Consumes exact number of Exotic Shards per recipe
- Output slot must be empty (endgame items don't stack)
- 30-second craft time creates a dramatic feel for endgame items

### Energy
- 10,000 RF/t — requires substantial power infrastructure
- Needs 6,000,000 RF total per craft
- Internal buffer of 5,000,000 RF means you need continuous power feed
- Pauses without power, progress preserved

### Visual
- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Glowing purple aura, particles spiral inward, light level 15
  - `active=false`: Dark obsidian-like appearance
- Particle: Purple/magenta vortex particles spiraling into the center
- Sound: Deep resonant hum that builds in pitch as progress increases

---

## Crafting Recipe

```
[Obsidian]      [Exotic Matter]    [Obsidian]
[Exotic Matter] [Nether Star]      [Exotic Matter]
[Obsidian]      [Exotic Matter]    [Obsidian]
```

---

## Implementation Notes

### New Files
- `block/ExoticMatterInfuserBlock.java`
- `block/entity/ExoticMatterInfuserBlockEntity.java`
- `menu/ExoticMatterInfuserMenu.java`
- `screen/ExoticMatterInfuserScreen.java`

### Registration
- Standard block/entity/menu/screen/capability registration
- Requires EXOTIC_SHARD and GRAVITON_CRYSTAL items to exist
