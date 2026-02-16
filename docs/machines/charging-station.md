# Charging Station

**Tier**: 2 — Tool Charging
**Type**: Single-block machine
**Category**: Utility

---

## Overview

The Charging Station charges RF-powered tools and armor. Place your depleted Tachyon Drill or Exotic Chestplate inside and it fills them up from connected power sources. Essential infrastructure once you start using powered equipment.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Buffer | 500,000 RF |
| Max Input Rate | Unlimited (accepts as fast as sources push) |
| Charge Rate | Up to 10,000 RF/t into held item |
| Item Slots | 1 (tool/armor to charge) |
| Hardness/Resistance | 3.5 / 6.0 |
| Tool | Pickaxe |

---

## GUI Layout

```
┌──────────────────────────────────────────┐
│          Charging Station                │
│                                          │
│  [Energy]        [Item Slot]             │
│   ██████          ┌──┐                  │
│   ██████          │⚡│                  │
│   ██████          └──┘                  │
│   ██████                                 │
│   ██████     Charge: 245,000 / 500,000   │
│              Rate: 10,000 RF/t           │
│                                          │
│  ┌──────────────────────────────┐        │
│  │     Player Inventory         │        │
│  └──────────────────────────────┘        │
└──────────────────────────────────────────┘
```

- **Energy Bar** (left): Station's internal buffer
- **Item Slot** (center): Accepts only items with IEnergyStorage capability
- **Charge Display**: Shows item's current / max RF
- **Rate Display**: Shows actual charge rate being delivered

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | station energy stored |
| 1 | station energy capacity |
| 2 | item energy stored (0 if empty) |
| 3 | item energy capacity (0 if empty) |

---

## Behavior

### Charging Logic
- Each tick, transfers min(10,000, stationEnergy, itemCapacity - itemEnergy) RF to item
- Only accepts items that implement IEnergyStorage (ForgeEnergy)
- Item is fully charged → stops transferring, item can be removed
- Multiple charging stations can be placed; each is independent

### Energy Input
- Accepts energy from all 6 sides
- No maximum input rate — receives as fast as neighbors push
- Internal buffer acts as a reservoir between input bursts and steady charging

### Visual
- **Blockstate**: `CHARGING` boolean
  - `charging=true`: Top surface shows energy arc particles, light level 7
  - `charging=false`: Idle appearance
- Particle: Electric spark particles rising from the block when charging

---

## Crafting Recipe

```
[Iron Ingot]      [Redstone]       [Iron Ingot]
[Tachyon Alloy]   [Diamond]        [Tachyon Alloy]
[Iron Ingot]      [Redstone Block]  [Iron Ingot]
```

---

## Automation

- Hoppers/pipes can insert and extract items
- Useful for automated tool charging stations
- Can be combined with item filters to charge and return items

---

## Implementation Notes

### New Files
- `block/ChargingStationBlock.java` — CHARGING blockstate, EntityBlock
- `block/entity/ChargingStationBlockEntity.java` — Energy buffer + item charging
- `menu/ChargingStationMenu.java` — 1 slot + ContainerData
- `screen/ChargingStationScreen.java` — Energy bar + item charge bar

### Registration
- `ModBlocks.java` — CHARGING_STATION
- `ModBlockEntities.java` — CHARGING_STATION
- `ModMenuTypes.java` — CHARGING_STATION
- `ModCapabilities.java` — Energy + Item handler
- `tachyonClient.java` — Screen
