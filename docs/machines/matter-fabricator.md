# Matter Fabricator

**Tier**: 5 — Endgame Item Creation
**Type**: Single-block machine
**Category**: Advanced Processing

---

## Overview

The Matter Fabricator converts pure RF energy into UU-Matter (Universal Usable Matter), a liquid that can be shaped into any basic material. The ultimate endgame machine — if you have enough power, you never need to mine again. Inspired by IC2's Mass Fabricator but with Tachyon flavor.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 50,000 RF/t (max input) |
| Internal Buffer | 10,000,000 RF |
| UU-Matter per Craft | 1,000 mB (1 bucket) |
| RF per UU-Matter | 50,000,000 RF (50M) |
| Production Time | 1,000 ticks (50 sec) at max power |
| Internal Fluid Tank | 16,000 mB UU-Matter |
| Scrap Slot | 1 (amplifier input) |
| Hardness/Resistance | 15.0 / 1200.0 |
| Tool | Pickaxe (netherite) |

---

## UU-Matter System

### What is UU-Matter?
A new fluid — glowing white liquid that represents pure condensed energy-matter. Stored in the fabricator's internal tank, extracted via fluid pipes or buckets.

### Scrap Amplifier
Feed any item into the scrap slot to boost production speed:
- Each item consumed adds a speed bonus based on EMC-like value
- Common items (cobblestone, dirt): +1% speed per item
- Uncommon items (iron, gold): +5% speed
- Rare items (diamond, emerald): +15% speed
- Exotic items (Tachyon Shard, Exotic Matter): +50% speed
- Maximum amplification: 8x speed (12.5% of normal time)

---

## UU-Matter Recipes (Separate Crafting Station: Matter Replicator)

UU-Matter is used in the **Matter Replicator** (companion machine) to create items:

| Output | UU-Matter Cost |
|--------|---------------|
| 16x Iron Ingot | 500 mB |
| 16x Gold Ingot | 750 mB |
| 8x Diamond | 1,000 mB |
| 4x Emerald | 1,000 mB |
| 16x Titanium Ingot | 750 mB |
| 16x Tungsten Ingot | 750 mB |
| 16x Lithium Ingot | 500 mB |
| 16x Thorium Ingot | 500 mB |
| 1x Nether Star | 5,000 mB |
| 4x Tachyon Shard | 2,000 mB |
| 1x Exotic Matter | 8,000 mB |

---

## GUI Layout

```
┌──────────────────────────────────────────────────┐
│            Matter Fabricator                     │
│                                                  │
│  [Energy]   [Scrap]   [UU Tank]    Amplifier: 3x│
│   ██████     ┌──┐      ████████                 │
│   ██████     │  │      ████████    50,000 RF/t   │
│   ██████     └──┘      ████████                 │
│   ██████               ████████    Progress:     │
│   ██████    ──────►    ████████    ██████░░ 67%  │
│                        ████████                 │
│              Scrap Boost: +15%    12,000 / 16,000│
│                                                  │
│  ┌──────────────────────────────────────┐        │
│  │     Player Inventory                 │        │
│  └──────────────────────────────────────┘        │
└──────────────────────────────────────────────────┘
```

---

## ContainerData (6 slots)

| Index | Data |
|-------|------|
| 0 | energy stored (scaled) |
| 1 | energy capacity (scaled) |
| 2 | progress (RF spent so far, scaled) |
| 3 | max progress (50M, scaled) |
| 4 | fluid stored (mB) |
| 5 | amplifier multiplier (x100) |

---

## Visual

- **Blockstate**: `ACTIVE` boolean
  - `active=true`: White glow emanating from all sides, intense particle burst, light level 15
  - `active=false`: Dark, heavy industrial look
- Particle: White energy particles converging into the center
- Sound: Deep power-up whine, building in intensity

---

## Crafting Recipe

```
[Exotic Matter]    [Quantum Processor] [Exotic Matter]
[Graviton Crystal] [Nether Star]       [Graviton Crystal]
[Exotic Matter]    [Quantum Processor] [Exotic Matter]
```

---

## Companion: Matter Replicator

A separate single-block machine that converts UU-Matter into items.

| Property | Value |
|----------|-------|
| Energy Consumption | 1,000 RF/t |
| Internal Buffer | 500,000 RF |
| Internal UU Tank | 16,000 mB |
| Process Time | 100 ticks (5 seconds) |
| Recipe Slot | 1 (template — not consumed) |
| Output Slot | 1 |

### How It Works
1. Place a "template" item in the recipe slot (e.g., Diamond)
2. Machine reads the UU-Matter cost for that item
3. Consumes UU-Matter from internal tank
4. Produces the item in the output slot
5. Template stays — it's a reference, not consumed

---

## New Fluid: UU-Matter

| Property | Value |
|----------|-------|
| Registry | `uu_matter` |
| Color | White with iridescent shimmer |
| Density | 2000 (heavy) |
| Viscosity | 3000 (thick) |
| Temperature | 300 (room temp) |
| Luminosity | 15 (bright) |
| Bucket | UU-Matter Bucket |

---

## Implementation Notes

### New Files (Matter Fabricator)
- `block/MatterFabricatorBlock.java`
- `block/entity/MatterFabricatorBlockEntity.java`
- `menu/MatterFabricatorMenu.java`
- `screen/MatterFabricatorScreen.java`

### New Files (Matter Replicator)
- `block/MatterReplicatorBlock.java`
- `block/entity/MatterReplicatorBlockEntity.java`
- `menu/MatterReplicatorMenu.java`
- `screen/MatterReplicatorScreen.java`

### New Fluid
- Register UU-Matter fluid type, source, flowing, bucket in ModFluids
- Client extension for white/iridescent texture + tint
