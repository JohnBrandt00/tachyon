# Dimensional Rift

**Tier**: 5+ — Stretch Goal / Endgame Content
**Type**: Multiblock portal structure
**Category**: Exploration / Custom Dimension

---

## Overview

The Dimensional Rift is the ultimate endgame structure — a portal to the **Tachyon Dimension**, a custom dimension filled with unique resources, challenges, and aesthetics. Built from exotic materials and powered by massive RF, it tears a hole in spacetime that players can walk through.

---

## The Tachyon Dimension

### Theme
A shattered, crystalline void dimension with floating islands of exotic matter and gravitational anomalies. The sky is a swirling purple-black void with distant nebulae. Physics work differently here.

### Environment
- **No day/night cycle**: Perpetual twilight with aurora-like effects
- **Reduced gravity**: Player jump height doubled, fall speed halved
- **Void below y=0**: Falling into the void returns you to the Overworld (not death)
- **No weather**: Clear skies always
- **Ambient sound**: Eerie cosmic hum

### Terrain Features
- **Exotic Islands**: Floating platforms of Exotic Stone (new block)
- **Crystal Spires**: Tall crystalline formations that can be mined for Graviton Dust
- **Void Pockets**: Areas of zero gravity — entities float freely
- **Tachyon Geodes**: Hollow structures containing concentrated Tachyon Shard clusters
- **Antimatter Lakes**: Pools of liquid antimatter (instant death on contact without Exotic Armor)

### Unique Resources
| Resource | Found In | Use |
|----------|----------|-----|
| Exotic Stone | Islands | Building material, decorative |
| Graviton Dust | Crystal Spires | Crafting component |
| Void Crystal | Rare geodes | Void Prism upgrades |
| Tachyon Ore (Dense) | Islands | 4x Tachyon Shard yield |
| Stabilized Antimatter | Containment nodes | Safe antimatter, no explosion risk |

### Hostile Mobs (Custom)
| Mob | Behavior | Drops |
|-----|----------|-------|
| Void Wraith | Phases through blocks, melee attack | Void Essence |
| Tachyon Golem | Slow, tanky, heavy melee | Tachyon Alloy Ingot, Exotic Shard |
| Photon Swarm | Fast, flying, shoots light projectiles | Excited Photon, Glowstone Dust |
| Entropy Sentinel | Mini-boss, rare spawn, gravity attacks | Graviton Crystal, Quantum Processor |

---

## Portal Structure

### Shape (5x5x1 portal frame)

```
     [E] [E] [E] [E] [E]
     [E] [?] [?] [?] [E]
     [E] [?] [?] [?] [E]
     [E] [?] [?] [?] [E]
     [E] [C] [E] [E] [E]

E = Exotic Portal Frame (new block)
C = Rift Controller (multiblock controller)
? = Air (portal fills when activated)
```

### Activation
1. Build the 5x5 frame from Exotic Portal Frame blocks
2. Place the Rift Controller at the bottom center
3. Insert a Rift Key into the controller
4. Supply 1,000,000 RF to the controller (takes ~20 seconds at 50k RF/t)
5. Portal ignites with a dramatic visual effect
6. Portal stays open as long as the controller has RF (drains 1,000 RF/t)

### Portal Behavior
- Walk into the portal area to teleport (like Nether portal)
- 3-second transition (can be cancelled by stepping back)
- Spawns at a safe platform in the Tachyon Dimension
- Return portal generates automatically at the spawn point
- Closing the portal (removing power or frame) strands players until they die or use a Null Capsule

---

## New Blocks

### Exotic Portal Frame
| Property | Value |
|----------|-------|
| Registry | `exotic_portal_frame` |
| Hardness | 50.0 (obsidian-level) |
| Blast Resistance | 1200.0 (indestructible by explosions) |
| Light Level | 5 (ambient glow) |

#### Crafting Recipe
```
[Exotic Shard]   [Obsidian]       [Exotic Shard]
[Obsidian]       [Tachyon Alloy]  [Obsidian]
[Exotic Shard]   [Obsidian]       [Exotic Shard]
```

### Rift Controller
| Property | Value |
|----------|-------|
| Registry | `rift_controller` |
| Energy Buffer | 1,000,000 RF |
| Activation Cost | 1,000,000 RF |
| Maintenance | 1,000 RF/t while portal is open |
| Key Slot | 1 (Rift Key) |

### Rift Key
| Property | Value |
|----------|-------|
| Registry | `rift_key` |
| Type | Consumable (consumed on portal activation) |
| Stack Size | 1 |

#### Crafting Recipe
```
[Exotic Matter]    [Nether Star]      [Exotic Matter]
[Graviton Crystal] [Ender Eye]        [Graviton Crystal]
[Exotic Matter]    [Quantum Processor] [Exotic Matter]
```

---

## Dimension Technical Details

### Registration
- Custom dimension type via `dimension_type` JSON
- Custom biome via `worldgen/biome` JSON
- Custom chunk generator (or noise-based with custom settings)
- Portal block similar to `NetherPortalBlock`

### World Generation
- Floating island generator: noise-based with high cutoff for islands
- Crystal spire structures: structure features
- Geodes: similar to amethyst geodes
- Antimatter lakes: fluid placement

---

## Implementation Notes

### This is a STRETCH GOAL
This is the most complex feature in the mod. Recommended implementation order:
1. Portal frame blocks + controller (without actual dimension)
2. Custom dimension registration + basic terrain
3. Return portal mechanics
4. Unique resources + blocks
5. Custom mobs
6. Structures and world gen

### New Files (Dimension)
- `world/TachyonDimension.java` — Dimension registration
- `world/TachyonChunkGenerator.java` — Custom terrain generation
- `world/TachyonBiome.java` — Biome definition
- `block/ExoticPortalFrameBlock.java`
- `block/RiftControllerBlock.java`
- `block/entity/RiftControllerBlockEntity.java`
- `block/TachyonPortalBlock.java`
- Entity classes for custom mobs (4+)

### Scope Warning
This feature alone is equivalent to a small mod. Consider implementing it last, after all other features are stable. The portal structure and dimension registration are the core; mobs and structures can be added incrementally.
