# Solar Collector

**Tier**: 3 — Condensed Light Pipeline (Step 1 of 3)
**Type**: Single-block passive machine
**Category**: Resource Generation

---

## Overview

A passive block that harnesses sunlight to produce Raw Photons. No GUI — right-click to extract items and see status, or automate with hoppers/pipes. The first step in the Condensed Light production pipeline.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Required | None (passive, sunlight-powered) |
| Output | Raw Photons |
| Base Rate | ~1 Raw Photon per 200 ticks (10 sec) at peak noon |
| Internal Buffer | 16 Raw Photons |
| Sky Access | Required (block above must see sky) |
| Light Emission | 8 when actively producing |
| Hardness/Resistance | 3.0 / 6.0 |
| Tool | Pickaxe |

---

## Production Mechanics

### Sky & Time Requirements
- `level.canSeeSky(pos.above())` must be true
- Only produces during Minecraft daytime

### Weather Multiplier
| Weather | Multiplier |
|---------|-----------|
| Clear Sky | 1.00 (100%) |
| Rain | 0.30 (30%) |
| Thunderstorm | 0.00 (0%) |

### Time-of-Day Curve
Production follows a sine wave peaking at solar noon:
```java
float dayFraction = (gameTime % 24000) / 24000.0f;
float solarMultiplier = Math.max(0, (float) Math.sin(Math.PI * dayFraction));
```
- Dawn (~0 ticks): 0% production
- Noon (~6000 ticks): 100% production
- Dusk (~12000 ticks): 0% production
- Night: 0% production

### Accumulation
- Each tick adds `solarMultiplier * weatherMultiplier * (1.0 / 200.0)` to a fractional counter
- When counter >= 1.0, produces 1 Raw Photon and subtracts 1.0
- Smoothly handles variable rates without wasting partial progress

---

## Interaction

### Right-Click (No GUI)
- If buffer has items: Gives 1 Raw Photon to player, shows status message
  - `"Solar Collector: 12 Raw Photons stored (Producing)"`
  - `"Solar Collector: 0 Raw Photons stored (Idle - no sunlight)"`
- If buffer is empty: Shows status only

### Automation
- Exposes `IItemHandler` capability on all sides
- Hoppers extract from bottom
- Item pipes/conduits extract from any side
- No insertion — output only

---

## Visual

### Blockstate
- `ACTIVE` boolean property
  - `active=true`: Top glass glows, light level 8, sun-catching particle effect
  - `active=false`: Dormant appearance

### Particle Effects
- When active: Golden sparkle particles drift upward from the glass surface
- Rate scales with production rate (more particles at noon)

### Model
- Base: Stone/metal frame
- Top: Translucent glass panel (like a solar panel)
- Sides: Metal housing with vents

---

## Crafting Recipe

```
[Glass]          [Glass]          [Glass]
[Glowstone Dust] [Photonic Matrix] [Glowstone Dust]
[Iron Ingot]     [Redstone Block]  [Iron Ingot]
```

---

## Automation Notes

- Place in a clear area with no blocks above
- Use hoppers or item pipes to continuously extract
- Multiple collectors feed into a single Particle Accelerator
- Rain/night creates natural downtime — buffer Raw Photons
- At peak noon, clear sky: 1 Raw Photon every ~10 seconds
- Average production over a full day/night cycle: ~6-8 Raw Photons per day

---

## Pipeline Context

```
Sunlight → [Solar Collector] → Raw Photons
                                    ↓
                          [Particle Accelerator] → Excited Photons
                                                       ↓
                                             [Photon Compressor] → Condensed Light
```

See also: [Condensed Light Pipeline](../condensed-light-pipeline.md)

---

## Implementation Notes

### New Files
- `block/SolarCollectorBlock.java` — ACTIVE property, `useWithoutItem()` for right-click
- `block/entity/SolarCollectorBlockEntity.java` — ItemStackHandler(1), production tick logic

### Registration
- `ModBlocks.java` — SOLAR_COLLECTOR
- `ModBlockEntities.java` — SOLAR_COLLECTOR
- `ModCapabilities.java` — Item handler (output only)
- `en_us.json` — "Solar Collector"
- Tags: `mineable/pickaxe`

### New Items
- `RAW_PHOTON` in ModItems (simple item, no special behavior)
