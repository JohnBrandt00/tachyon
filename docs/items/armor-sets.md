# Armor Sets

**Category**: Equipment — Tachyon Alloy (Mid) + Exotic (Endgame)
**Both sets have set bonuses**

---

## Tier 1: Tachyon Alloy Armor (Mid-Game)

Between diamond and netherite protection. Crafted from Tachyon Alloy Ingots using standard armor patterns.

### Stats

| Piece | Armor | Toughness | Knockback Resist | Special |
|-------|-------|-----------|------------------|---------|
| Helmet | 3 | 2.5 | 0 | Night Vision while worn |
| Chestplate | 8 | 2.5 | 0 | — |
| Leggings | 6 | 2.5 | 0 | +10% movement speed |
| Boots | 3 | 2.5 | 0 | No fall damage |

**Comparison:**

| Stat | Iron (Full) | Diamond (Full) | Tachyon (Full) | Netherite (Full) |
|------|-------------|----------------|----------------|------------------|
| Armor | 15 | 20 | 20 | 20 |
| Toughness | 0 | 8 | 10 | 12 |
| Durability (Chest) | 240 | 528 | 650 | 592 |

### Individual Piece Abilities

#### Helmet — Night Vision
- Applies Night Vision effect while worn
- No particles (ambient = true)
- Refreshed every 10 seconds (no flickering)
- Togglable: Sneak + Right-click helmet in inventory to toggle on/off

#### Chestplate — None
- Pure stats, no special ability
- Highest armor value in the set

#### Leggings — Speed Boost
- +10% movement speed (AttributeModifier)
- Applies as equipment attribute, stacks with Speed potions
- Visual: Faint speed lines when sprinting

#### Boots — No Fall Damage
- Completely negates fall damage
- No particle effect, just silent landing
- Also protects from stalagmite damage

### Full Set Bonus
When ALL 4 pieces are worn:
- **+15% attack speed** (AttributeModifier on all items, active only when full set detected)
- **Step Assist**: Automatically walk up 1-block heights (like a horse)
  - Set `player.maxUpStep()` to 1.0 when full set is worn
- Visual indicator: Faint tachyon particle trail when moving with full set

### Durability

| Piece | Durability |
|-------|-----------|
| Helmet | 450 |
| Chestplate | 650 |
| Leggings | 600 |
| Boots | 500 |

### Crafting Recipes

Standard armor patterns with Tachyon Alloy Ingots (T):

```
Helmet:      [T] [T] [T]     Chestplate:  [T] [ ] [T]
             [T] [ ] [T]                  [T] [T] [T]
                                          [T] [T] [T]

Leggings:    [T] [T] [T]     Boots:       [T] [ ] [T]
             [T] [ ] [T]                  [T] [ ] [T]
             [T] [ ] [T]
```

### Repair
- Repaired with Tachyon Alloy Ingots at an anvil

---

## Tier 2: Exotic Armor (Endgame)

The best armor in the game. Crafted in the Exotic Matter Infuser by upgrading Tachyon Armor with Exotic Shards. RF-powered — abilities drain energy from the chestplate's internal battery.

### Stats

| Piece | Armor | Toughness | Knockback Resist | Special |
|-------|-------|-----------|------------------|---------|
| Helmet | 4 | 4.0 | 0.1 | X-Ray vision (ore + mob outlines) |
| Chestplate | 9 | 4.0 | 0.1 | 50% damage → RF drain (5M RF buffer) |
| Leggings | 7 | 4.0 | 0.1 | +30% speed, sprint = near-flight speed |
| Boots | 4 | 4.0 | 0.1 | No fall damage + double jump + step assist |

### Individual Piece Abilities

#### Helmet — X-Ray Vision
- Outlines all ores within 32-block radius through walls
- Also outlines all mobs/players within 32-block radius
- Uses the vanilla glowing effect rendering (colored outlines)
- Ore outlines: colored by ore type
- Mob outlines: red for hostile, green for passive, yellow for players
- RF Cost: 50 RF/t (drawn from chestplate)
- Toggle: Sneak + Right-click

#### Chestplate — Energy Shield
- Internal RF buffer: 5,000,000 RF
- Absorbs 50% of incoming damage as RF drain
- 1 damage absorbed = 5,000 RF consumed
- At 0 RF: no absorption, functions as normal armor
- Charges at Charging Station or from Tachyon Battery
- Shows RF level as additional durability bar (blue)
- Damage absorption stacks with armor reduction (damage → armor reduces → then 50% of remainder → RF)

#### Leggings — Hyperspeed
- +30% movement speed (AttributeModifier)
- Sprint speed: 2x normal sprint speed (near-creative-flight speed)
- RF Cost: 20 RF/t while sprinting at hyperspeed (drawn from chestplate)
- At 0 RF: +30% speed still works, hypersprint disabled
- Visual: Speed blur effect when hypersprinting

#### Boots — Advanced Movement
- No fall damage (any height)
- Double jump: Press jump while airborne for a second jump
  - Height: 1.5 blocks
  - RF Cost: 500 RF per double jump
- Step assist: Walk up 1-block heights automatically
- Safe landing: Landing from any height creates a shockwave
  - Falls > 10 blocks: shockwave pushes nearby entities away
  - Cosmetic: crack particles at landing point

### Full Set Bonus
When ALL 4 Exotic pieces are worn:
- **Creative-style flight** (100 RF/t from chestplate)
- **Immunity to Wither and Poison effects**
- **90% total damage reduction** (after all armor calculations)
- **Glowing aura effect**: Cosmetic purple particle aura around the player
- **Auto-heal**: Regeneration I while RF > 50% in chestplate

### Durability
- **Infinite**: Exotic armor cannot break
- No durability bar (shows RF bar instead)

### Crafting (Exotic Matter Infuser)

| Input | Exotic Shards | Output |
|-------|--------------|--------|
| Tachyon Helmet | 4 | Exotic Helmet |
| Tachyon Chestplate | 4 | Exotic Chestplate |
| Tachyon Leggings | 4 | Exotic Leggings |
| Tachyon Boots | 4 | Exotic Boots |

All require Graviton Crystal catalyst (not consumed).

---

## Implementation Notes

### New Files
- `item/TachyonArmorItem.java` — Custom ArmorItem with per-piece abilities
- `item/ExoticArmorItem.java` — Custom ArmorItem + IEnergyStorage + RF abilities
- `item/ModArmorMaterial.java` — Custom ArmorMaterial for both tiers

### Technical Details

#### Tachyon Armor
- Custom `ArmorMaterial` with specified protection/toughness values
- Abilities in `inventoryTick()` checking equipped slot
- Night Vision: `player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false))`
- Fall damage: Handle `LivingFallEvent`, cancel if wearing boots
- Set bonus detection: Check all 4 armor slots for Tachyon armor items
- Step assist: Modify `player.setMaxUpStep(1.0f)` when full set, reset to 0.6f when not

#### Exotic Armor
- Extends TachyonArmorItem with additional IEnergyStorage capability
- Energy stored in item stack NBT
- Damage absorption: Handle `LivingDamageEvent`, reduce damage, consume RF
- Double jump: Client-side input detection → send packet → server applies velocity
- Flight: Set `player.getAbilities().mayfly = true` when full set + RF available
- X-Ray: Client-side rendering in `RenderLevelStageEvent`
- Full set 90% reduction: Handle `LivingDamageEvent` with high priority
