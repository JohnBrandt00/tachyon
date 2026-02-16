# Warp Pad

**Tier**: 5 — Teleportation
**Type**: Single-block utility (paired)
**Category**: Transportation

---

## Overview

Paired teleportation platforms. Step on one, teleport to the other. Works across dimensions. Consumes Exotic Matter as fuel. The endgame fast-travel system.

---

## Specifications

| Property | Value |
|----------|-------|
| Fuel | 1 Exotic Matter per teleport |
| Cooldown | 30 seconds between uses |
| Range | Unlimited (cross-dimensional) |
| Max Players | 1 per teleport (entity on the pad) |
| Hardness/Resistance | 8.0 / 12.0 |
| Tool | Pickaxe (diamond+) |

---

## Linking System

### How to Link
1. Craft two Warp Pads
2. Hold an Exotic Shard and right-click Pad A → "Warp Pad registered (A)"
3. Right-click Pad B with the same Exotic Shard → "Warp Pads linked!"
4. Exotic Shard is consumed on successful linking
5. Both pads now show linked status

### Link Properties
- Bidirectional — stepping on either pad teleports to the other
- Only one link per pad
- Breaking either pad breaks the link
- Links persist through chunk unloading (stored in NBT)
- Cross-dimensional links work (Overworld ↔ Nether ↔ End)

---

## Teleportation

### How It Works
1. Player steps onto a linked, fueled Warp Pad
2. 3-second charge-up (stand still on the pad)
3. If player moves off the pad, charge resets
4. After 3 seconds: teleport to linked pad, consume 1 Exotic Matter
5. 30-second cooldown on both pads before next use

### Cross-Dimensional
- Works between any dimensions
- Target chunk must be loaded OR the pad forces a temporary chunk load
- Teleportation handles dimension transition seamlessly

### Safety
- Target pad must not be obstructed (2-block clearance above)
- If target is obstructed: teleport fails, no fuel consumed, error message
- If target pad is destroyed: "Link broken — destination no longer exists"

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│              Warp Pad                        │
│                                              │
│  [Fuel Slot]     Status: LINKED              │
│   ┌──┐                                      │
│   │EM│           Destination:                │
│   └──┘           (123, 64, -456)             │
│                  minecraft:the_nether        │
│  Fuel: 5 teleports remaining                 │
│                                              │
│  Cooldown: Ready                             │
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
| 0 | fuel count (exotic matter in slot) |
| 1 | cooldown ticks remaining |
| 2 | linked (0/1) |
| 3 | charge progress (0-60 ticks = 3 seconds) |

---

## Visual

### Idle (Linked, Ready)
- Swirling particle vortex on pad surface (purple/cyan particles)
- Light level 8
- Slow rotation animation

### Charging (Player Standing)
- Particle vortex intensifies, spins faster
- Rising pitch sound effect
- Player gets glowing effect during charge

### Teleporting
- Flash of light, implosion particle effect at source
- Explosion particle effect at destination
- Sound: Dimensional warp sound

### Unlinked
- No particles, dark surface
- Light level 0

---

## Crafting Recipe

```
[Exotic Shard]    [Ender Pearl]      [Exotic Shard]
[Tachyon Alloy]   [Exotic Matter]    [Tachyon Alloy]
[Obsidian]        [Obsidian]         [Obsidian]
```

---

## Implementation Notes

### New Files
- `block/WarpPadBlock.java` — carpet-height slab, EntityBlock, stepping detection
- `block/entity/WarpPadBlockEntity.java` — linking, fuel, teleport logic

### Technical Details
- Use `entityInside()` or `stepOn()` to detect player
- Track charge progress per player UUID (in case multiple players approach)
- Teleportation: `player.teleportTo(serverLevel, x, y, z, yaw, pitch)`
- Cross-dimensional: `player.changeDimension(serverLevel)`
- Cooldown: simple tick counter in block entity
- Link storage: partner BlockPos + ResourceKey<Level> in NBT
- Block shape: like a pressure plate / carpet (short, 1-2 pixel tall)
