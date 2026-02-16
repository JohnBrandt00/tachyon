# Tachyon Wrench

**Tier**: 1 — Utility Tool
**Type**: Held tool
**Category**: Utility

---

## Overview

The universal tool for interacting with Tachyon machines. Rotates blocks, configures machine sides, and safely dismantles machines without losing contents. Every tech mod player's best friend.

---

## Specifications

| Property | Value |
|----------|-------|
| Durability | Infinite (cannot break) |
| Stack Size | 1 |
| Enchantable | No |

---

## Functions

### Right-Click: Rotate Block
- Rotates the target block's FACING property by 90 degrees
- Works on any block with a FACING or HORIZONTAL_FACING property
- Works on all Tachyon machines, vanilla furnaces, etc.
- Sound: Mechanical click

### Shift + Right-Click: Instant Dismantle
- Instantly breaks the target machine block
- Drops the block as an item (preserving energy/contents in NBT)
- Works on all Tachyon machines
- No tool tier required — wrench works on everything
- Machines with stored energy retain it in the dropped item
- Does NOT work on multiblock controllers (safety — prevents partial destruction)
- Sound: Mechanical unscrew

### Left-Click: Side Configuration (Future)
- Opens a side configuration GUI for compatible machines
- Allows setting each side to: Input / Output / Both / None
- Not all machines support side config — shows message if unsupported

---

## Visual

### Item Model
- T-shaped wrench with a tachyon alloy head
- Purple-tinted metallic texture
- Slight glow on the business end

---

## Crafting Recipe

```
[Tachyon Alloy]  [              ] [Tachyon Alloy]
[              ] [Iron Ingot]     [              ]
[              ] [Iron Ingot]     [              ]
```

---

## Implementation Notes

### New Files
- `item/TachyonWrenchItem.java`

### Technical Details
- `useOn()`: Check if target block has FACING property → cycle it
- Shift + `useOn()`: Check if target block is a Tachyon machine → drop with NBT
- Use block tags to identify wrenchable/dismantleable blocks
- Dismantle: `level.getBlockEntity(pos).saveWithFullMetadata()` → store in item NBT
- Place back: restore from item NBT on block placement
