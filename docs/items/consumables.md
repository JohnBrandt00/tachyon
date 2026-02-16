# Consumables & Potions

**Category**: Single-use items with powerful effects
**Range**: Tier 2 through Tier 5

---

## Overview

Consumables provide temporary buffs, utility effects, and emergency tools. They fill the gap between permanent equipment and situation-specific needs. Most are crafted at a crafting table.

---

## Tachyon Serum

| Property | Value |
|----------|-------|
| Tier | 2 |
| Type | Drinkable (like potion) |
| Effects | Speed III + Haste II |
| Duration | 60 seconds |
| Stackable | 16 |

### Recipe
```
[              ] [Tachyon Shard]   [              ]
[              ] [Glowstone Dust]  [              ]
[              ] [Glass Bottle]    [              ]
```
*Shapeless recipe.*

### Behavior
- Right-click to drink (standard drinking animation)
- Applies Speed III and Haste II simultaneously
- Useful for mining runs and exploration
- No negative side effects

---

## Graviton Pill

| Property | Value |
|----------|-------|
| Tier | 4 |
| Type | Edible (instant, like golden apple) |
| Effects | Slow Falling + Jump Boost III |
| Duration | 90 seconds |
| Stackable | 16 |

### Recipe
```
[              ] [Graviton Crystal] [              ]
[              ] [Sugar]            [              ]
[              ] [Glass Bottle]     [              ]
```
*Shapeless recipe.*

### Behavior
- Right-click to consume (instant eat)
- Slow Falling prevents all fall damage and reduces fall speed
- Jump Boost III allows massive jumps (6+ blocks high)
- Combination creates a pseudo-flight: jump high, fall slowly
- Great for navigating terrain and building

---

## Photon Charge

| Property | Value |
|----------|-------|
| Tier | 2 |
| Type | Throwable (like snowball) |
| Effect | Flash + Blind in radius |
| Radius | 8 blocks |
| Stackable | 16 |

### Recipe
```
[              ] [Excited Photon]   [              ]
[              ] [Gunpowder]        [              ]
[              ] [              ]   [              ]
```
*Shapeless recipe. Produces 4 charges.*

### Behavior
- Right-click to throw (snowball physics)
- On impact: bright flash effect (screen goes white for 0.5s for all nearby)
- Applies Blindness III for 5 seconds to all mobs within 8-block radius
- Player who threw it is NOT affected
- Useful for crowd control and escaping
- Creates light particles at impact point

---

## Exotic Elixir

| Property | Value |
|----------|-------|
| Tier | 5 |
| Type | Drinkable |
| Effects | Absorption IV + Resistance II + Regeneration II |
| Duration | 30 seconds |
| Stackable | 4 |

### Recipe
```
[              ] [Exotic Shard]     [              ]
[              ] [Golden Apple]     [              ]
[              ] [Glass Bottle]     [              ]
```
*Shapeless recipe.*

### Behavior
- Right-click to drink
- Grants 8 absorption hearts (16 HP shield)
- Resistance II reduces damage by 40%
- Regeneration II heals 1 heart per 1.5 seconds
- Essentially makes you nearly unkillable for 30 seconds
- Expensive to craft — emergency use only

---

## Null Capsule

| Property | Value |
|----------|-------|
| Tier | 4 |
| Type | Right-click instant use |
| Effect | Teleport to last death location |
| Uses | 1 (consumed) |
| Stackable | 1 |

### Recipe
```
[              ] [Exotic Matter]    [              ]
[              ] [Ender Pearl]      [              ]
[              ] [              ]   [              ]
```
*Shapeless recipe.*

### Behavior
- Right-click: instantly teleports player to their last death location
- Works cross-dimensionally
- Consumed on use
- If the player has never died: shows message "No death location recorded"
- If death location is in unloaded chunks: forces chunk load temporarily
- 5-second cooldown after teleport (prevents spam)
- Plays dimensional warp sound + particle effect

### Implementation
- Track last death location using `PlayerEvent.PlayerRespawnEvent`
- Store in persistent player data (survives death)
- On use: teleport player to stored location

---

## Stabilizer Injection

| Property | Value |
|----------|-------|
| Tier | 3 |
| Type | Right-click instant use |
| Effect | Clears ALL negative effects + 10s fire resistance |
| Uses | 1 (consumed) |
| Stackable | 16 |

### Recipe
```
[              ] [Photonic Matrix]    [              ]
[              ] [Glistering Melon]   [              ]
[              ] [Glass Bottle]       [              ]
```
*Shapeless recipe.*

### Behavior
- Right-click: instantly consumed
- Removes ALL negative potion effects (Poison, Wither, Slowness, Mining Fatigue, etc.)
- Applies Fire Resistance for 10 seconds
- Plays a "cure" sound effect + green particles
- Does not affect positive effects
- Useful as a panic button

---

## Plasma Grenade (NEW)

| Property | Value |
|----------|-------|
| Tier | 3 |
| Type | Throwable (like snowball) |
| Effect | Energy explosion (damages entities, NOT blocks) |
| Damage | 15 hearts in 4-block radius, falls off with distance |
| Stackable | 8 |

### Recipe
```
[              ] [Tachyon Shard]     [              ]
[Gunpowder]      [Energy Crystal]    [Gunpowder]
[              ] [Iron Ingot]        [              ]
```
*Produces 2 grenades.*

### Behavior
- Right-click to throw (grenade arc physics, not straight like snowball)
- Explodes on impact with block or entity
- Deals 15 hearts damage at center, scaling down to 3 hearts at 4-block radius
- Does NOT destroy blocks (pure energy damage)
- Sets entities on fire for 3 seconds
- Creates a blue plasma burst particle effect
- Useful for mob clearing without terrain destruction

---

## Void Pearl (NEW)

| Property | Value |
|----------|-------|
| Tier | 4 |
| Type | Throwable (like ender pearl) |
| Effect | Teleport + leave behind a void zone |
| Stackable | 4 |

### Recipe
```
[              ] [Exotic Matter]     [              ]
[              ] [Ender Pearl]       [              ]
[              ] [Tachyon Shard]     [              ]
```

### Behavior
- Right-click to throw (ender pearl physics)
- Player teleports to impact point (like ender pearl, but no damage)
- Leaves behind a "Void Zone" at the ORIGIN point (where the player was):
  - 3-block radius sphere of darkness particles
  - Lasts 10 seconds
  - Any mob in the zone takes 3 damage/tick
  - Players are unaffected
- Useful for teleporting away while punishing pursuers

---

## Chrono Capsule (NEW)

| Property | Value |
|----------|-------|
| Tier | 5 |
| Type | Right-click activation |
| Effect | Rewinds player position 5 seconds |
| Stackable | 1 |

### Recipe
```
[              ] [Graviton Crystal]  [              ]
[Tachyon Shard]   [Clock]            [Tachyon Shard]
[              ] [Exotic Matter]     [              ]
```

### Behavior
- Passively records player position every tick while in inventory (rolling 100-tick buffer)
- Right-click: teleports player to their position from 5 seconds ago
- Also restores health to what it was 5 seconds ago (if higher)
- Does NOT restore inventory
- Consumed on use
- 30-second internal cooldown
- Visual: Time-rewind particle trail (shows the path backwards)
- Sound: Reverse-playback swoosh

---

## Summary Table

| Consumable | Tier | Type | Stack | Key Effect |
|-----------|------|------|-------|------------|
| Tachyon Serum | 2 | Drink | 16 | Speed III + Haste II (60s) |
| Photon Charge | 2 | Throw | 16 | Flash + Blind (8 blocks) |
| Stabilizer Injection | 3 | Use | 16 | Cure all negative effects |
| Plasma Grenade | 3 | Throw | 8 | 15 hearts AoE (no block damage) |
| Graviton Pill | 4 | Eat | 16 | Slow Falling + Jump III (90s) |
| Null Capsule | 4 | Use | 1 | Teleport to death location |
| Void Pearl | 4 | Throw | 4 | Teleport + void zone |
| Exotic Elixir | 5 | Drink | 4 | Near-invincibility (30s) |
| Chrono Capsule | 5 | Use | 1 | Rewind position 5 seconds |

---

## Implementation Notes

### New Files
- `item/TachyonSerumItem.java` — extends PotionItem-like behavior
- `item/GravitonPillItem.java` — instant eat item
- `item/PhotonChargeItem.java` — throwable with custom entity
- `item/ExoticElixirItem.java` — drinkable
- `item/NullCapsuleItem.java` — use item with teleport
- `item/StabilizerInjectionItem.java` — instant use, clears effects
- `item/PlasmaGrenadeItem.java` — throwable projectile
- `item/VoidPearlItem.java` — throwable with zone effect
- `item/ChronoCapsuleItem.java` — position recording + rewind
- Entity classes for throwable projectiles

### Technical Details
- Drinkable items: override `use()`, return `InteractionResultHolder.consume()`
- Throwable: spawn custom projectile entity on use
- Projectile entities: extend `ThrowableProjectile`, handle `onHitBlock()` and `onHitEntity()`
- Null Capsule death tracking: use `PlayerEvent.Clone` to persist death location
- Chrono Capsule: use `inventoryTick()` to record positions in a circular buffer (stored in item NBT)
