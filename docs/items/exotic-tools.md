# Exotic Tools

**Tier**: 3 — Endgame Equipment
**Source**: Exotic Matter Infuser
**Category**: Ultimate Tools

---

## Overview

The ultimate tools. Effectively infinite durability, insane abilities, and game-changing power. Each is crafted in the Exotic Matter Infuser by infusing a base item with Exotic Shards. These are the reward for mastering the full tech tree.

---

## Singularity Pick

| Property | Value |
|----------|-------|
| Durability | Infinite (cannot break) |
| Mining Speed | 50.0 |
| Attack Damage | +6 (9 total) |
| Mining Level | 5 (mines everything) |
| Special | **5x5 Mining + Auto-Smelt + Auto-Collect** |

### Abilities
- **5x5 Mining**: Breaks a 5x5 area centered on the targeted block face
- **Auto-Smelt**: All ores drop smelted ingots (like Tachyon Pickaxe)
- **Auto-Collect**: All drops teleport directly to player inventory (no ground items)
- **Universal**: Mines ANY block (picks + axes + shovels combined)
- **Fortune V** built-in (stacks with enchantment)
- Sneak to mine single block

### Crafting (Exotic Matter Infuser)
- Base: Tachyon Pickaxe
- Cost: 4 Exotic Shards
- Catalyst: Graviton Crystal
- Energy: 6,000,000 RF
- Time: 30 seconds

---

## Entropy Blade

| Property | Value |
|----------|-------|
| Durability | Infinite |
| Attack Damage | +20 (23 total) |
| Attack Speed | 2.0 |
| Special | **Armor Pierce + Double Loot** |

### Abilities
- **Armor Pierce**: Attacks bypass ALL armor and protection enchantments
  - Damage is applied as "true damage" (like `/damage`)
  - Shield blocking is also bypassed
- **Double Loot**: Entities killed drop 2x their normal loot
  - Stacks with Looting enchantment
  - Applies to XP drops too
- **Decay Aura**: Undead mobs within 5 blocks take 2 damage/sec passively while held
- **Sweep**: Standard sword sweep at full damage (not reduced)

### Crafting (Exotic Matter Infuser)
- Base: Tachyon Sword
- Cost: 4 Exotic Shards
- Catalyst: Graviton Crystal
- Energy: 6,000,000 RF
- Time: 30 seconds

---

## Gravity Staff

| Property | Value |
|----------|-------|
| Durability | Infinite |
| RF Capacity | 2,000,000 RF |
| RF per Tick (flight) | 100 RF/t |
| Special | **Flight + Gravity Control** |

### Abilities

#### Passive: Flight
- While held in either hand: grants creative-style flight
- Costs 100 RF/t while flying
- At 0 RF: flight disabled, player falls normally
- Flight speed: 1.5x creative flight speed

#### Right-Click: Gravity Pull
- Pulls ALL items in a 32-block radius toward the player
- Items fly toward player at high speed
- Also pulls XP orbs
- Costs 200 RF per activation
- 5-tick cooldown

#### Shift + Right-Click: Gravity Push
- Pushes ALL entities (mobs, players, items) in a 16-block radius away
- Massive knockback force (4.0 velocity)
- Costs 500 RF per activation
- 20-tick cooldown
- Entities take 5 damage on impact with blocks

#### Hold Right-Click: Gravity Lift
- Hold right-click while aiming at an entity
- Target entity floats upward, suspended in mid-air
- Release to drop them
- Costs 50 RF/t while holding
- Range: 16 blocks
- Cannot lift boss mobs

### Crafting (Exotic Matter Infuser)
- Base: Tachyon Core
- Cost: 8 Exotic Shards
- Catalyst: Graviton Crystal
- Energy: 6,000,000 RF
- Time: 30 seconds

---

## Void Prism

| Property | Value |
|----------|-------|
| Durability | Infinite |
| Special | **Block Capture + Placement** |

### Abilities

#### Right-Click Block: Capture
- Picks up ANY block including its block entity (tile entity) data
- Stores the block inside the prism
- Works on ANYTHING: chests with contents, spawners, machines, even bedrock
- Block disappears from the world, stored in item NBT
- Prism item shows stored block name in tooltip
- Only one block stored at a time
- Exceptions: blocks with active multiblock formations (prevents breaking multiblocks)

#### Right-Click Air: Place
- Places the stored block at the target position
- Restores all block entity data (chest contents, machine state, etc.)
- Block appears in world, prism becomes empty
- Standard placement rules (needs solid surface for most blocks)

### Tooltip Display
```
Void Prism
Stored: Diamond Ore
Right-click to place
```
Or when empty:
```
Void Prism
Empty
Right-click a block to capture
```

### Crafting (Exotic Matter Infuser)
- Base: Diamond
- Cost: 2 Exotic Shards
- Catalyst: Graviton Crystal
- Energy: 6,000,000 RF
- Time: 30 seconds

---

## Visual Effects

### Singularity Pick
- Black/purple gradient texture
- Small gravitational distortion particles when mining
- Enchantment glint (purple-shifted)

### Entropy Blade
- Red/black blade texture with entropy decay patterns
- Red particle trail when swinging
- Entities flash red when hit

### Gravity Staff
- Floating orb model (like End Rod but with a crystal)
- Purple gravitational field particles when abilities are used
- Ambient floating particles around the held item

### Void Prism
- Translucent crystalline model
- When holding a block: shows a tiny version of the stored block inside
- Dimensional void particles when capturing/placing

---

## Implementation Notes

### New Files
- `item/SingularityPickItem.java`
- `item/EntropyBladeItem.java`
- `item/GravityStaffItem.java`
- `item/VoidPrismItem.java`

### Technical Details
- Infinite durability: `getMaxDamage()` returns 0, or override `isDamageable()` to return false
- Armor pierce: Use `DamageSource` with bypass armor flag
- Block capture: Save `BlockState` + `CompoundTag` (from `BlockEntity.saveWithFullMetadata()`) to item NBT
- Block placement: Restore from NBT with `level.setBlockAndUpdate()` + `BlockEntity.loadWithComponents()`
- Flight: Use `PlayerEvent.LivingTickEvent`, check for staff in hand, set `player.getAbilities().flying = true`
- Gravity effects: Apply motion vectors to entities in radius
