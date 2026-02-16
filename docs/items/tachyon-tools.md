# Tachyon Tools

**Tier**: 1 — Basic Tachyon Equipment
**Material**: Tachyon Alloy Ingot
**Category**: Tools

---

## Overview

The first custom tool set. Between iron and diamond tier in raw stats, but each tool has a unique special ability that makes it genuinely better than diamond. Crafted with standard tool patterns using Tachyon Alloy Ingots.

---

## Tool Tier Stats

| Property | Iron | Tachyon Alloy | Diamond | Netherite |
|----------|------|--------------|---------|-----------|
| Durability | 250 | 1800 | 1561 | 2031 |
| Mining Speed | 6.0 | 8.5 | 8.0 | 9.0 |
| Attack Damage (base) | +2 | +3 | +3 | +4 |
| Mining Level | 2 | 3 | 3 | 4 |
| Enchantability | 14 | 18 | 10 | 15 |

*Better durability than diamond, slightly faster, same mining level, much more enchantable.*

---

## Tachyon Pickaxe

| Property | Value |
|----------|-------|
| Attack Damage | +4 (7 total) |
| Attack Speed | 1.2 |
| Special | **Auto-Smelt**: Ores drop smelted ingots directly |

### Auto-Smelt Behavior
- Mining iron ore drops iron ingot instead of raw iron
- Mining gold ore drops gold ingot instead of raw gold
- Mining copper ore drops copper ingot instead of raw copper
- Mining mod ores (titanium, etc.) drops their ingots
- Respects Fortune enchantment (Fortune applied to smelted output)
- Does NOT work with Silk Touch (Silk Touch overrides auto-smelt)
- Works on any block that has a smelting recipe

### Implementation
- Override `mineBlock()` or subscribe to `BlockEvent.BreakEvent`
- Check if block's drop has a smelting recipe
- If yes, replace drops with smelted result
- Apply Fortune multiplier to smelted output

---

## Tachyon Sword

| Property | Value |
|----------|-------|
| Attack Damage | +6 (9 total) |
| Attack Speed | 1.9 (20% faster than diamond's 1.6) |
| Special | **Rapid Strike**: 20% faster attack speed |

### Rapid Strike Behavior
- Simply has higher attack speed value (1.9 vs 1.6)
- Translates to faster full-damage swings
- Stacks with attack speed enchantments/effects
- Sweep attack included (standard sword behavior)

---

## Tachyon Axe

| Property | Value |
|----------|-------|
| Attack Damage | +8 (11 total) |
| Attack Speed | 1.0 |
| Special | **Timber**: Fells entire trees (connected log breaking) |

### Timber Behavior
- When a log block is broken, recursively breaks all connected logs above
- Connected = adjacent on the same XZ column or 1 block diagonally in XZ
- Max logs per tree: 256 (prevents infinite loops on modded trees)
- All drops go to the player's inventory or drop at the player's feet
- Durability cost: 1 per log broken (not 1 per tree)
- Works on all log types (oak, birch, spruce, jungle, etc.)
- Checks for leaf blocks above to confirm it's a tree (prevents breaking log buildings)

### Implementation
- On block break: if block is a log type, start BFS/DFS upward
- Check for leaf blocks in a 5-block radius of the top — if found, it's a tree
- Break all connected logs, consume durability per log
- Collect drops to player inventory

---

## Tachyon Shovel

| Property | Value |
|----------|-------|
| Attack Damage | +3 (6 total) |
| Attack Speed | 1.0 |
| Special | **Excavate**: 3x1 digging (row of 3 blocks in facing direction) |

### Excavate Behavior
- When digging, breaks the target block + 2 adjacent blocks in the row perpendicular to the player's look direction
- Looking at a wall: breaks 3 blocks in a horizontal row
- Looking at the ground: breaks 3 blocks in a row left-to-right relative to player facing
- Only breaks blocks the shovel can mine (dirt, sand, gravel, clay, etc.)
- Durability cost: 1 per block broken
- Sneak to disable (mine single block)

### Implementation
- Determine player facing and calculate the two adjacent block positions
- Break center block normally, then break adjacent blocks if they're shovel-mineable
- Use `player.gameMode.destroyBlock()` for proper event firing

---

## Crafting Recipes

All use standard tool patterns with Tachyon Alloy Ingots (T) and sticks (S):

### Pickaxe
```
[T] [T] [T]
[ ] [S] [ ]
[ ] [S] [ ]
```

### Sword
```
[ ] [T] [ ]
[ ] [T] [ ]
[ ] [S] [ ]
```

### Axe
```
[T] [T] [ ]
[T] [S] [ ]
[ ] [S] [ ]
```

### Shovel
```
[ ] [T] [ ]
[ ] [S] [ ]
[ ] [S] [ ]
```

---

## Repair

- Repaired with Tachyon Alloy Ingots at an anvil
- Can also combine two damaged Tachyon tools in crafting grid

---

## Implementation Notes

### New Files
- `item/TachyonPickaxeItem.java` — DiggerItem + auto-smelt override
- `item/TachyonAxeItem.java` — AxeItem + timber override
- `item/TachyonShovelItem.java` — ShovelItem + excavate override
- `item/ModToolTier.java` — Custom Tier enum for Tachyon Alloy

### Registration
- `ModItems.java` — TACHYON_PICKAXE, TACHYON_SWORD, TACHYON_AXE, TACHYON_SHOVEL
- For the sword: no custom class needed, just set attack speed in properties

### Technical Details
- Custom Tier: implements `net.minecraft.world.item.Tier`
- `getUses()` = 1800, `getSpeed()` = 8.5, `getAttackDamageBonus()` = 3.0
- `getLevel()` = 3, `getEnchantmentValue()` = 18
- `getRepairIngredient()` = Ingredient.of(ModItems.TACHYON_ALLOY_INGOT)
