# Photon Compressor

**Tier**: 3 — Condensed Light Pipeline (Step 3 of 3)
**Type**: Single-block machine
**Category**: Processing

---

## Overview

The Photon Compressor crushes Excited Photons into Condensed Light using RF power and Glowstone Dust as a catalyst. This is the final step in the Condensed Light production pipeline — the output fuels the Singularity Engine's Photonic Injectors.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 2,000 RF/t |
| Internal Buffer | 500,000 RF |
| Process Time | 100 ticks (5 seconds) |
| Input Slots | 2 (Excited Photon + Glowstone Dust) |
| Output Slot | 1 (Condensed Light) |
| Hardness/Resistance | 5.0 / 6.0 |
| Tool | Pickaxe |

---

## Recipe

| Input 1 | Input 2 | Output | Total RF |
|---------|---------|--------|----------|
| 1 Excited Photon | 1 Glowstone Dust | 1 Condensed Light | 200,000 RF |

---

## GUI Layout

```
┌──────────────────────────────────────────┐
│          Photon Compressor               │
│                                          │
│  [Energy]   [In 1]              [Out]    │
│   ██████     ┌──┐    ──►        ┌──┐    │
│   ██████     │EP│   Progress     │CL│    │
│   ██████     └──┘    ──►        └──┘    │
│   ██████     ┌──┐                        │
│   ██████     │GD│                        │
│              └──┘                        │
│             [In 2]                       │
│                                          │
│  ┌──────────────────────────────┐        │
│  │     Player Inventory         │        │
│  └──────────────────────────────┘        │
└──────────────────────────────────────────┘
```

EP = Excited Photon, GD = Glowstone Dust, CL = Condensed Light

---

## ContainerData (4 slots)

| Index | Data |
|-------|------|
| 0 | progress (ticks) |
| 1 | maxProgress (100) |
| 2 | energy stored |
| 3 | energy capacity |

---

## Behavior

### Processing
- Requires both Excited Photon AND Glowstone Dust to start
- Consumes 1 of each input per craft
- Produces 1 Condensed Light
- Pauses if energy runs out (progress preserved)
- Resets if inputs removed mid-craft

### Energy
- Accepts energy from all sides
- 2,000 RF/t while processing
- 0 RF/t when idle

### Visual
- **Blockstate**: `ACTIVE` boolean + `FACING` horizontal
  - `active=true`: Front shows compressed light particles, light level 12
  - `active=false`: Dormant
- Particle: Bright white compression particles when active

---

## Crafting Recipe

```
[Iron Ingot]       [Glowstone Block]  [Iron Ingot]
[Tachyon Alloy]    [Piston]           [Tachyon Alloy]
[Reactor Plating]  [Redstone Block]   [Reactor Plating]
```

---

## Pipeline Context

```
Sunlight → [Solar Collector] → Raw Photons
                                    ↓
                          [Particle Accelerator] → Excited Photons
                                                       ↓
                                             [Photon Compressor] → Condensed Light
                                                                       ↓
                                                             [Photonic Injector] → Singularity Engine
```

---

## Automation

- Top: Input slot 1 (Excited Photon)
- Sides: Input slot 2 (Glowstone Dust)
- Bottom: Output extraction (Condensed Light)
- Energy from all directions

---

## Implementation Notes

### New Files
- `block/PhotonCompressorBlock.java` — FACING + ACTIVE, EntityBlock
- `block/entity/PhotonCompressorBlockEntity.java` — Processing + energy
- `menu/PhotonCompressorMenu.java` — 2 input + 1 output + ContainerData
- `screen/PhotonCompressorScreen.java` — Energy bar + progress

### New Items
- `EXCITED_PHOTON` in ModItems (simple item)

### Registration
- Standard block/entity/menu/screen/capability registration
