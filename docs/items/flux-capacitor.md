# Flux Capacitor

**Tier**: 2 — Portable Energy Storage
**Type**: Held item (curio/bauble slot or inventory)
**Category**: Utility

---

## Overview

A portable RF battery you carry in your inventory. Automatically charges RF-powered tools and armor in your inventory. Three tiers with increasing capacity. The essential companion to any powered equipment.

---

## Tiers

| Tier | Capacity | Charge Rate | Recipe Cost |
|------|----------|-------------|-------------|
| Basic | 1,000,000 RF | 1,000 RF/t | Energy Crystal + Iron |
| Advanced | 5,000,000 RF | 5,000 RF/t | Basic + Tachyon Alloy |
| Ultimate | 25,000,000 RF | 25,000 RF/t | Advanced + Exotic Shard |

---

## Behavior

### Auto-Charging
- Each tick, scans player inventory for items with IEnergyStorage capability
- Charges found items at the tier's charge rate
- Priority: hotbar first (left to right), then inventory (top-left to bottom-right)
- Distributes charge evenly if multiple items need charging
- Only charges items, never charges other Flux Capacitors (prevents loops)

### Charging the Capacitor
- Place in a Charging Station to charge
- Can also be charged by holding it and standing on any RF-outputting block
- Retains charge in item NBT (doesn't lose charge on death/drop)

### Toggle
- Sneak + Right-click: Toggle auto-charge on/off
- When off: stores energy but doesn't distribute it
- Useful for preserving charge

---

## Visual

### Item Appearance
- Cylindrical/barrel shape with glowing energy core
- Basic: Iron housing, blue core
- Advanced: Tachyon Alloy housing, purple core
- Ultimate: Exotic housing, white/gold core

### Durability Bar
- Shows charge level as colored bar:
  - Green: >50%
  - Yellow: 25-50%
  - Red: <25%

### Tooltip
```
Advanced Flux Capacitor
3,250,000 / 5,000,000 RF
████████████░░░░░░ 65%
Charge Rate: 5,000 RF/t
Mode: Auto-Charge ON
```

---

## Crafting Recipes

### Basic Flux Capacitor
```
[Iron Ingot]     [Redstone]       [Iron Ingot]
[Redstone]       [Energy Crystal] [Redstone]
[Iron Ingot]     [Redstone]       [Iron Ingot]
```

### Advanced Flux Capacitor
```
[Tachyon Alloy]  [Energy Crystal] [Tachyon Alloy]
[Energy Crystal] [Basic Flux Cap] [Energy Crystal]
[Tachyon Alloy]  [Energy Crystal] [Tachyon Alloy]
```

### Ultimate Flux Capacitor
```
[Exotic Shard]   [Tachyon Core]   [Exotic Shard]
[Tachyon Core]   [Adv Flux Cap]   [Tachyon Core]
[Exotic Shard]   [Tachyon Core]   [Exotic Shard]
```

---

## Implementation Notes

### New Files
- `item/FluxCapacitorItem.java` — Item + IEnergyStorage + inventory tick logic

### Technical Details
- Implements `ICapabilityProvider` for energy storage
- `inventoryTick()`: scan player inventory, charge items
- Three separate item registrations (basic, advanced, ultimate)
- Energy stored in `"Energy"` NBT tag
- Toggle state in `"Active"` boolean NBT tag
- Durability bar: override `getBarWidth()`, `getBarColor()`, `isBarVisible()`
