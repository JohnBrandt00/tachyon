# Tachyon Battery

**Tier**: 2 — Energy Storage
**Type**: Single-block utility
**Category**: Energy Infrastructure

---

## Overview

A simple high-capacity energy storage block. Accepts and provides RF from all sides. Retains energy when broken (stored in item NBT), making it portable. The standard energy buffer for any Tachyon setup.

---

## Specifications

| Property | Value |
|----------|-------|
| Capacity | 10,000,000 RF (10M) |
| Max Input | 50,000 RF/t (per side) |
| Max Output | 50,000 RF/t (per side) |
| Total Max I/O | 300,000 RF/t (all 6 sides) |
| Retains Energy | Yes (item NBT on break) |
| Comparator Output | 0-15 based on fill level |
| Hardness/Resistance | 4.0 / 6.0 |
| Tool | Pickaxe |

---

## Behavior

### Energy Flow
- Accepts energy from all 6 sides simultaneously
- Auto-pushes energy to all 6 sides simultaneously
- Acts as a buffer — receives and distributes
- Priority: fills internal buffer first, then pushes to neighbors

### Comparator
- Emits comparator signal 0-15 based on stored energy
- 0 = empty, 15 = full
- Useful for redstone automation (e.g., "turn off reactor when battery full")

### Portable Energy
- When broken with pickaxe, drops as item with stored RF in NBT
- Item tooltip shows: `"Stored: 7,500,000 / 10,000,000 RF"`
- When placed back, restores stored energy
- Silk touch NOT required — always retains energy

---

## Visual

- **Blockstate**: `LEVEL` int property (0-4)
  - Level 0: 0% — Empty (dark)
  - Level 1: 1-25% — Single LED lit
  - Level 2: 26-50% — Two LEDs
  - Level 3: 51-75% — Three LEDs
  - Level 4: 76-100% — All four LEDs, slight glow
- Light emission: 0 / 2 / 4 / 6 / 8 based on level
- Model: Industrial battery block with vertical LED strip on each side

---

## GUI Layout (Simple)

```
┌──────────────────────────────────────┐
│         Tachyon Battery              │
│                                      │
│        [Energy Bar - Large]          │
│         ██████████████████          │
│         ██████████████████          │
│         ██████████████████          │
│         ██████████████████          │
│         ██████████████████          │
│                                      │
│        7,500,000 / 10,000,000 RF     │
│        I/O: 50,000 RF/t              │
│                                      │
│  ┌──────────────────────────────┐    │
│  │     Player Inventory         │    │
│  └──────────────────────────────┘    │
└──────────────────────────────────────┘
```

No item slots — pure energy display.

---

## Crafting Recipe

```
[Iron Ingot]    [Tachyon Alloy]  [Iron Ingot]
[Tachyon Alloy] [Redstone Block] [Tachyon Alloy]
[Iron Ingot]    [Tachyon Alloy]  [Iron Ingot]
```

---

## Tiered Variants (Optional)

| Variant | Capacity | Max I/O | Recipe Upgrade |
|---------|----------|---------|---------------|
| Basic Tachyon Battery | 10M RF | 50k RF/t | Base recipe |
| Advanced Tachyon Battery | 50M RF | 250k RF/t | Battery + Energy Crystals |
| Ultimate Tachyon Battery | 500M RF | 2.5M RF/t | Adv Battery + Exotic Shards |

*Each tier is crafted by surrounding the previous tier with upgrade materials.*

---

## Implementation Notes

### New Files
- `block/TachyonBatteryBlock.java` — LEVEL blockstate (0-4), EntityBlock
- `block/entity/TachyonBatteryBlockEntity.java` — EnergyStorage, auto-push, NBT save
- `menu/TachyonBatteryMenu.java` — No item slots, just ContainerData
- `screen/TachyonBatteryScreen.java` — Large energy bar display

### Technical Details
- Override `playerDestroy()` to drop item with NBT energy
- Override `setPlacedBy()` to restore energy from item NBT
- Comparator: `getAnalogOutputSignal()` returns 0-15 based on fill
- Auto-push: each tick, iterate 6 directions, push min(50000, stored) to each neighbor
- Blockstate updates on threshold crossings only (not every tick)
