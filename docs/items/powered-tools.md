# Powered Tools (RF-Powered)

**Tier**: 2 — RF-Powered Equipment
**Energy Source**: Internal RF battery (charged at Charging Station)
**Category**: Advanced Tools

---

## Overview

Rechargeable tools powered by RF energy. Each has a massive internal battery and powerful abilities that consume RF per use. When depleted, they still function as basic tools but lose their special abilities. Must be charged at a Charging Station or adjacent to any RF source.

---

## Tachyon Drill

| Property | Value |
|----------|-------|
| RF Capacity | 500,000 RF |
| RF per Use | 100 RF per block mined |
| Attack Damage | +5 (8 total) |
| Mining Speed | 20.0 (extremely fast) |
| Special | **3x3 Mining + Vein Mine** |

### 3x3 Mining
- Mines a 3x3 area centered on the targeted block
- Only mines blocks at or below the hardness of the target block
- Costs 100 RF per block broken (900 RF for full 3x3)
- Sneak to mine single block
- Works on stone, ores, dirt — anything a pickaxe or shovel can mine

### Vein Mining
- When mining an ore block, also mines all connected ore blocks of the same type
- Max vein size: 64 blocks
- Each block costs 100 RF
- Stacks with 3x3 — mine a 3x3 that hits ore, and the ore vein-mines outward

### Depleted Mode
- At 0 RF: mines at speed 2.0 (slower than iron), single block only, no special abilities
- Still functional — never "breaks", just becomes inefficient

---

## Photon Blade

| Property | Value |
|----------|-------|
| RF Capacity | 250,000 RF |
| RF per Use | 50 RF per attack |
| Attack Damage | +12 (15 total) |
| Attack Speed | 1.8 |
| Special | **Sweeping Beam + Ignite** |

### Sweeping Beam
- Left-click attacks fire a beam in a 5-block line in front of the player
- All entities in the beam take full attack damage
- Beam is visible as a streak of light particles
- Costs 50 RF per entity hit

### Ignite
- All entities hit are set on fire for 5 seconds
- Fire aspect that doesn't need the enchantment
- Stacks with Fire Aspect enchantment for longer burn

### Depleted Mode
- At 0 RF: +4 damage (7 total), no beam, no ignite
- Functions as a basic sword

---

## Graviton Launcher

| Property | Value |
|----------|-------|
| RF Capacity | 1,000,000 RF |
| RF per Use | 500 RF |
| Attack Damage | +2 (5 total) |
| Special | **Item Magnet + Entity Push** |

### Right-Click: Item Magnet
- Pulls ALL dropped items within 16-block radius toward the player
- Items accelerate toward the player over 1 second
- Costs 500 RF per activation
- 10-tick cooldown between uses
- Works through walls (items fly toward player, clipping through blocks)

### Shift + Right-Click: Entity Push
- Pushes ALL entities (except items) within 8-block radius away from the player
- Push force: 2.0 blocks/tick velocity away from player
- Costs 1,000 RF per activation
- 20-tick cooldown
- Knocks back mobs, players, minecarts, boats, etc.

### Depleted Mode
- At 0 RF: No abilities, functions as a weak melee weapon only

---

## Quantum Scanner

| Property | Value |
|----------|-------|
| RF Capacity | 100,000 RF |
| RF per Use | 1,000 RF per scan |
| Attack Damage | +0 (3 total) |
| Special | **Ore Highlighting** |

### Right-Click: Scan
- Highlights all ore blocks within a 16-block radius through walls
- Ores appear as colored particle outlines visible through blocks
- Effect lasts 10 seconds
- Different colors per ore type:
  - Iron: Orange
  - Gold: Yellow
  - Diamond: Cyan
  - Tachyon ores: Purple
  - Coal: Dark gray
  - Copper: Brown
  - Redstone: Red
  - Lapis: Blue
  - Emerald: Green
- 60-tick cooldown between scans

### Implementation
- Server scans for ore blocks in radius
- Sends packet to client with list of BlockPos + ore type
- Client renders colored particle outlines at each position for 200 ticks
- Uses `RenderLevelStageEvent` for custom block highlighting

---

## Crafting Recipes

### Tachyon Drill
```
[Tachyon Alloy]   [Tachyon Alloy]   [Tachyon Alloy]
[              ]  [Tachyon Core]    [              ]
[              ]  [Energy Crystal]  [              ]
```

### Photon Blade
```
[              ]  [Tachyon Shard]   [              ]
[              ]  [Tachyon Shard]   [              ]
[              ]  [Tachyon Core]    [              ]
```

### Graviton Launcher
```
[Tachyon Alloy]   [Ender Pearl]     [Tachyon Alloy]
[Energy Crystal]  [Tachyon Core]    [Energy Crystal]
[              ]  [Tachyon Alloy]   [              ]
```

### Quantum Scanner
```
[              ]  [Tachyon Shard]   [              ]
[Tachyon Alloy]   [Tachyon Core]    [Tachyon Alloy]
[              ]  [Energy Crystal]  [              ]
```

---

## Energy Display

All powered tools show their charge in the item tooltip:
```
Tachyon Drill
375,000 / 500,000 RF
████████████░░░░ 75%
```

Also show a durability bar colored by charge level:
- Green: >50%
- Yellow: 25-50%
- Red: <25%
- Gray: 0% (depleted)

---

## Implementation Notes

### New Files
- `item/TachyonDrillItem.java` — DiggerItem + IEnergyStorage + 3x3 + vein mine
- `item/PhotonBladeItem.java` — SwordItem + IEnergyStorage + beam attack
- `item/GravitonLauncherItem.java` — Item + IEnergyStorage + magnet/push
- `item/QuantumScannerItem.java` — Item + IEnergyStorage + ore scan

### Technical Details
- All implement `ICapabilityProvider` for `IEnergyStorage` capability
- Energy stored in item NBT: `"Energy"` integer tag
- Durability bar override: `getBarWidth()` and `getBarColor()` based on energy
- Charging: any IEnergyStorage-capable machine can charge them
- 3x3 mining: calculate 8 surrounding BlockPos based on player look direction and hit face
- Vein mine: BFS from ore block, matching block type, max 64
