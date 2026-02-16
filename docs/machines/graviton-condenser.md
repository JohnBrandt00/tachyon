# Graviton Condenser

**Tier**: 4 — Singularity Engine Addon
**Type**: Single-block machine (attaches to Singularity Engine)
**Category**: Resource Generation

---

## Overview

The Graviton Condenser is a passive addon that attaches to the Singularity Engine. It harvests waste gravitational energy from the running singularity, slowly condensing it into rare Graviton Crystals. Higher black hole mass = faster production.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Required | None (harvests waste gravitational energy) |
| Output | Graviton Crystal |
| Base Production | ~1 crystal per 6,000 ticks (5 minutes) at mass >1200 |
| Internal Buffer | 8 Graviton Crystals |
| Placement | Must be adjacent to a Singularity Port (energy output mode) |
| Hardness/Resistance | 8.0 / 12.0 |
| Tool | Pickaxe (diamond+) |

---

## Production Mechanics

### Placement Requirement
- Must be placed directly adjacent (6 sides) to a Singularity Port
- The Singularity Port must be part of a formed Singularity Engine
- The engine must be running (black hole active, mass > 0)
- If engine stops or is deconstructed, production halts

### Mass-Scaled Production Rate
Production rate scales exponentially with black hole mass:

| Mass Range | Production Rate |
|------------|----------------|
| 0 - 500 | No production |
| 500 - 1000 | 1 crystal per ~15 minutes |
| 1000 - 1500 | 1 crystal per ~5 minutes |
| 1500 - 2000 | 1 crystal per ~2 minutes |
| 2000+ (max) | 1 crystal per ~1 minute |

```java
// Production formula
if (mass > 500) {
    double rate = Math.pow((mass - 500) / 1500.0, 2.0);
    progress += rate;
    if (progress >= 6000) {
        produceGravitonCrystal();
        progress -= 6000;
    }
}
```

### Buffer
- Stores up to 8 Graviton Crystals internally
- Stops producing when buffer is full
- Right-click to extract, or use hoppers/pipes

---

## Interaction

### Right-Click (No GUI)
- Extracts 1 Graviton Crystal to player inventory
- Shows status: `"Graviton Condenser: 3/8 crystals (Producing - Mass: 1547)"`
- If no adjacent engine: `"Graviton Condenser: Not connected to Singularity Engine"`

### Automation
- Item handler on all sides (output only)
- Hoppers extract from bottom
- Pipes/conduits extract from any side

---

## Visual

- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Faint gravitational distortion particles, purple glow, light level 5
  - `active=false`: Dormant
- Particle: Slow-orbiting purple particles that spiral inward
- Model: Heavy industrial block with crystalline growth on top surface

---

## Crafting Recipe

```
[Obsidian]       [Amethyst Block]   [Obsidian]
[Exotic Matter]  [Tachyon Core]     [Exotic Matter]
[Obsidian]       [Reactor Plating]  [Obsidian]
```

---

## New Item: Graviton Crystal

| Property | Value |
|----------|-------|
| Registry | `graviton_crystal` |
| Display Name | Graviton Crystal |
| Rarity | EPIC (purple name) |
| Max Stack | 16 |
| Glow | Enchantment glint effect |

Used in:
- Exotic Matter Infuser (catalyst, not consumed)
- Graviton Pill (consumable)
- Graviton Launcher (crafting)
- Gravity Staff (crafting)
- Quantum Entangler (crafting)

---

## Implementation Notes

### New Files
- `block/GravitonCondenserBlock.java` — ACTIVE property, right-click extraction
- `block/entity/GravitonCondenserBlockEntity.java` — Scans for adjacent SingularityPort, production tick

### Technical Details
- `serverTick()`: Check adjacent blocks for SingularityPortBlockEntity
- From the port, get the controller reference → read current mass
- Scale production based on mass
- No menu/screen needed (no GUI)
