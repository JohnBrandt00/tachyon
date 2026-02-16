# Antimatter Containment Unit

**Tier**: 4 — Hazardous Material Storage
**Type**: Single-block machine
**Category**: Storage / Safety

---

## Overview

The Antimatter Containment Unit safely stores Antimatter — a new dangerous byproduct of the Singularity Engine at very high mass levels. Without containment, loose Antimatter items explode on contact with any block. The containment unit uses magnetic fields (RF energy) to suspend antimatter safely.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 500 RF/t (while containing antimatter) |
| Internal Buffer | 200,000 RF |
| Storage Capacity | 64 Antimatter units |
| Hardness/Resistance | 15.0 / 1200.0 (blast resistant) |
| Tool | Pickaxe (diamond+) |

---

## Antimatter (New Material)

### Generation
- Produced by the Singularity Engine when mass > 1800 (very high)
- Appears in a dedicated output slot on the Singularity Port
- Rate: ~1 Antimatter per 3,000 ticks at mass 1800+
- Rate scales with mass (faster at 2000)

### Danger
- **If dropped on the ground**: Explodes after 5 seconds (power 4.0, like TNT)
- **If right-clicked**: Explodes immediately (power 6.0)
- **If thrown in fire/lava**: Explodes (power 8.0)
- **Item entity collision with other entities**: Explodes (power 3.0)
- Only safe inside a Containment Unit or in player inventory (magnetic containment from Tachyon Armor)

### Uses
- Antimatter Bomb (throwable, massive explosion)
- Antimatter Neutralizer (crafting recipe already exists)
- Matter-Antimatter Reactor (potential future content)
- Required catalyst for some Matter Fabricator recipes
- Extreme crafting ingredient for endgame items

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│      Antimatter Containment Unit             │
│                                              │
│  [Energy]     [Containment Display]          │
│   ██████                                     │
│   ██████      ╔══════════════╗              │
│   ██████      ║  ◆ 32 / 64  ║              │
│   ██████      ║  Antimatter  ║              │
│   ██████      ╚══════════════╝              │
│                                              │
│  [Input]      Status: STABLE     [Output]    │
│   ┌──┐        500 RF/t           ┌──┐       │
│   │  │                           │  │       │
│   └──┘                           └──┘       │
│                                              │
│  ┌──────────────────────────────────┐        │
│  │     Player Inventory             │        │
│  └──────────────────────────────────┘        │
└──────────────────────────────────────────────┘
```

- **Containment Display**: Shows stored antimatter count with visual representation
- **Input Slot**: Insert Antimatter (safely absorbed)
- **Output Slot**: Extract Antimatter (safely dispensed)
- **Status**: STABLE (powered), WARNING (low power), CRITICAL (power failing)

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | energy stored |
| 1 | energy capacity |
| 2 | antimatter stored |
| 3 | antimatter capacity (64) |

---

## Behavior

### Power Failure
If the Containment Unit loses power while containing Antimatter:
1. **Warning Phase** (0-100 ticks without power): Status = WARNING, alarm sound
2. **Critical Phase** (100-200 ticks): Status = CRITICAL, intense particles, louder alarm
3. **Containment Breach** (200+ ticks): ALL contained antimatter detonates
   - Explosion power = 4.0 + (antimatterCount * 0.5)
   - At 64 antimatter: power 36.0 (devastating)
   - Creates "Void Scar" — temporary area of void damage (see below)

### Void Scar (Containment Breach Effect)
- 10-block radius area of "void energy" lasting 30 seconds
- Any entity in the area takes 4 damage/tick (bypasses armor)
- Blocks in radius have a 10% chance of being converted to air each tick
- Visual: Dark purple/black particle cloud
- Sound: Distorted dimensional tearing

### Safe Extraction
- Right-clicking output slot gives 1 Antimatter safely
- Hoppers/pipes can extract from output
- Breaking the block while containing Antimatter → containment breach!

---

## Visual

- **Blockstate**: `STATUS` enum (EMPTY, STABLE, WARNING, CRITICAL)
  - EMPTY: Dark, no particles
  - STABLE: Soft cyan magnetic field particles orbiting the block
  - WARNING: Yellow particles, flickering light
  - CRITICAL: Red pulsing, intense shaking particles, alarm sound

---

## Crafting Recipe

```
[Obsidian]        [Reactor Plating]  [Obsidian]
[Reactor Plating] [Exotic Matter]    [Reactor Plating]
[Obsidian]        [Reactor Plating]  [Obsidian]
```

---

## New Item: Antimatter

| Property | Value |
|----------|-------|
| Registry | `antimatter` |
| Display Name | Antimatter |
| Rarity | EPIC (purple name) |
| Max Stack | 1 |
| Glow | Red enchantment glint |
| Tooltip | "Extremely unstable. Handle with extreme caution." |

---

## Implementation Notes

### New Files
- `block/AntimatterContainmentBlock.java`
- `block/entity/AntimatterContainmentBlockEntity.java`
- `menu/AntimatterContainmentMenu.java`
- `screen/AntimatterContainmentScreen.java`
- `item/AntimatterItem.java` — custom item with explosion-on-drop behavior

### Technical Details
- AntimatterItem overrides `inventoryTick()` for item entity behavior
- Custom `EntityItem` subclass or event handler for dropped item explosions
- Power failure timer tracked in NBT
- Void Scar: scheduled tick effect at BlockPos, damages entities in radius
