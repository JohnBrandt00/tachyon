# Spatial Anchor

**Tier**: 5 — Chunk Loading
**Type**: Single-block utility
**Category**: Infrastructure

---

## Overview

An Exotic Matter-powered chunk loader. Keeps a configurable area of chunks loaded even when no players are nearby. Essential for keeping your Singularity Engine and automation running while you're off exploring.

---

## Specifications

| Property | Value |
|----------|-------|
| Fuel | Exotic Matter (1 per Minecraft day = 24,000 ticks) |
| Range | 1x1 to 5x5 chunks (configurable) |
| Fuel Slot | 1 |
| Hardness/Resistance | 8.0 / 12.0 |
| Tool | Pickaxe (diamond+) |

---

## Fuel Consumption

| Range | Exotic Matter / Day | Chunks Loaded |
|-------|-------------------|---------------|
| 1x1 | 1 | 1 chunk |
| 2x2 | 2 | 4 chunks |
| 3x3 | 3 | 9 chunks |
| 4x4 | 5 | 16 chunks |
| 5x5 | 8 | 25 chunks |

*Cost scales super-linearly — loading large areas is expensive.*

---

## GUI Layout

```
┌──────────────────────────────────────────────┐
│            Spatial Anchor                    │
│                                              │
│  [Fuel Slot]    Range: [3x3 ▼]              │
│   ┌──┐                                      │
│   │EM│          Loading: 9 chunks            │
│   └──┘          Fuel: 18h 32m remaining      │
│                                              │
│   ┌─────────────────┐                        │
│   │  □ □ □ □ □      │  □ = unloaded         │
│   │  □ ■ ■ ■ □      │  ■ = loaded           │
│   │  □ ■ ★ ■ □      │  ★ = anchor position  │
│   │  □ ■ ■ ■ □      │                       │
│   │  □ □ □ □ □      │                       │
│   └─────────────────┘                        │
│                                              │
│  ┌──────────────────────────────────┐        │
│  │     Player Inventory             │        │
│  └──────────────────────────────────┘        │
└──────────────────────────────────────────────┘
```

- Chunk grid visualization showing loaded area
- Range selector buttons (- / +)
- Fuel time remaining display

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | fuel ticks remaining |
| 1 | max fuel ticks (24000 per exotic matter) |
| 2 | range (1-5) |
| 3 | active (0/1) |

---

## Behavior

### Chunk Loading
- Uses `ForgeChunkManager` / NeoForge chunk loading API
- Registers force-loaded chunks centered on the anchor's chunk
- Updates chunk tickets when range changes
- Removes all tickets when fuel runs out or block is broken

### Fuel
- Auto-consumes from fuel slot when timer hits 0
- If no fuel available, stops loading chunks immediately
- Fuel rate depends on range setting
- Remaining time displayed in GUI

### Range
- Configurable 1x1 to 5x5 via GUI buttons
- Centered on the anchor's chunk position
- Changing range immediately updates loaded chunks
- Higher range = faster fuel consumption

---

## Visual

- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Faint dimensional anchor beam upward (like beacon, but smaller and purple), light level 10
  - `active=false`: Dark
- Particle: Thin purple beam shooting straight up when active
- Sound: Low ambient hum

---

## Crafting Recipe

```
[Obsidian]      [Ender Pearl]       [Obsidian]
[Exotic Matter] [Quantum Processor] [Exotic Matter]
[Obsidian]      [Ender Pearl]       [Obsidian]
```

---

## Implementation Notes

### New Files
- `block/SpatialAnchorBlock.java`
- `block/entity/SpatialAnchorBlockEntity.java`
- `menu/SpatialAnchorMenu.java`
- `screen/SpatialAnchorScreen.java`

### Technical Details
- NeoForge chunk loading: `RegisterTicketControllersEvent` + `ForgeChunkManager.forceChunk()`
- Store loaded chunk positions in NBT for cleanup on unload
- Must handle: block break, dimension unload, fuel exhaustion
- Chunk grid rendering in screen: simple colored rectangles
