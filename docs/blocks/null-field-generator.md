# Null Field Generator

**Tier**: 5 — Area Protection
**Type**: Single-block machine
**Category**: Utility / Defense

---

## Overview

Creates a protective energy field that prevents mob spawning, blocks explosions, and stops fire spread within a configurable radius. The ultimate base protection system — an invisible force field powered by RF.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 500 RF/t (base) + 50 RF/t per block of radius |
| Internal Buffer | 1,000,000 RF |
| Range | 16 to 64 blocks (configurable) |
| Default Range | 32 blocks |
| Effects | No mob spawning, no explosions, no fire spread |
| Hardness/Resistance | 10.0 / 1200.0 |
| Tool | Pickaxe (diamond+) |

---

## Energy Cost by Radius

| Radius | RF/t | Description |
|--------|------|-------------|
| 16 blocks | 1,300 RF/t | Small area (bedroom) |
| 32 blocks | 2,100 RF/t | Medium area (base) |
| 48 blocks | 2,900 RF/t | Large area (compound) |
| 64 blocks | 3,700 RF/t | Massive area (district) |

Formula: `cost = 500 + (radius * 50)`

---

## Field Effects

### Mob Spawn Prevention
- Cancels all natural mob spawns within radius (sphere)
- Does NOT prevent spawner-based spawns
- Does NOT affect passive mobs (animals)
- Does NOT affect boss mobs (Wither, Ender Dragon)

### Explosion Blocking
- All explosions within radius are cancelled
- Includes: TNT, Creepers, Ghast fireballs, Wither skulls, bed explosions
- Entities still take explosion damage (force field absorbs block damage only)

### Fire Prevention
- Fire spread is cancelled within radius
- Lava fire is prevented
- Lightning fire is prevented
- Existing fires are NOT extinguished — only new fire is prevented

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│         Null Field Generator                 │
│                                              │
│  [Energy]     [ON / OFF]                     │
│   ██████                                     │
│   ██████      Radius: [- 32 +]              │
│   ██████                                     │
│   ██████      Cost: 2,100 RF/t               │
│   ██████      Status: ACTIVE                 │
│                                              │
│   Field Coverage:                            │
│   ┌───────────────┐                          │
│   │    ╭──────╮   │                          │
│   │   ╱        ╲  │  32-block radius sphere  │
│   │  │    ★     │ │  ★ = generator          │
│   │   ╲        ╱  │                          │
│   │    ╰──────╯   │                          │
│   └───────────────┘                          │
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
| 2 | radius |
| 3 | active (0/1) |

---

## Visual

- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Core glows white, light level 12
  - `active=false`: Dark
- Particle: Subtle white particles at the boundary of the field (visible but not obtrusive)
- Boundary particles appear roughly at the sphere edge, fading in and out
- Density: ~20 particles per second scattered around the boundary

---

## Crafting Recipe

```
[Exotic Shard]    [Tachyon Alloy]     [Exotic Shard]
[Tachyon Alloy]   [Nether Star]       [Tachyon Alloy]
[Exotic Shard]    [Quantum Processor] [Exotic Shard]
```

---

## Implementation Notes

### New Files
- `block/NullFieldGeneratorBlock.java`
- `block/entity/NullFieldGeneratorBlockEntity.java`
- `menu/NullFieldGeneratorMenu.java`
- `screen/NullFieldGeneratorScreen.java`

### Technical Details
- Mob spawning: Subscribe to `MobSpawnEvent.FinalizeSpawn` or `LivingSpawnEvent`
- Explosion: Subscribe to `ExplosionEvent.Start`, cancel if within any active generator's radius
- Fire: Subscribe to `BlockEvent.NeighborNotifyEvent` or fire spread events
- Store active generators in a level-based registry for efficient radius checks
- Each tick: check if enough energy, consume if active
- Boundary particles: spawn on client tick at random positions on the sphere surface
