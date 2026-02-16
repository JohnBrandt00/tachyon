# Condensed Light Production Pipeline

## Overview

A 3-step industrial production chain for Condensed Light, the primary fuel for the Singularity Engine's Photonic Injectors.

```
Sunlight --> [Solar Collector] --> Raw Photons
Raw Photons --> [Particle Accelerator] --> Excited Photons  (uses helium + RF + momentum)
Excited Photons + Glowstone Dust --> [Photon Compressor] --> Condensed Light  (uses RF)
```

Replaces the old shapeless crafting recipe (Photonic Matrix + Ender Pearl). The new pipeline creates a real automation challenge with three distinct bottlenecks.

---

## New Items

| Item | Registry Name | Description |
|------|--------------|-------------|
| Raw Photon | `raw_photon` | Passively collected from sunlight by Solar Collectors |
| Excited Photon | `excited_photon` | Raw Photons accelerated to near-light speed in the Particle Accelerator |

---

## Step 1: Solar Collector

A passive block that harnesses sunlight to produce Raw Photons. No GUI -- right-click to extract items and see status, or automate with hoppers/pipes.

### Mechanics
- **Sky access required**: Block above must see sky
- **Daytime only**: Produces during Minecraft daytime
- **Weather affects output**: Clear sky = 100%, Rain = 30%, Thunderstorm = 0%
- **Time-of-day curve**: Sine wave peaking at noon for variable output rate
- **Base rate**: ~1 Raw Photon per 200 ticks (10 seconds) at peak noon
- **Internal buffer**: Stores up to 16 Raw Photons
- **Extraction**: Right-click gives 1 item to player with status message. Hoppers/pipes pull from item capability
- **Light emission**: Emits light level 8 when actively producing (ACTIVE blockstate)
- **No energy required**: Purely passive, sunlight-powered

### Automation Notes
- Place in a clear area with no blocks above
- Use hoppers or item pipes to continuously extract
- Multiple collectors can feed into a single accelerator
- Rain/night creates natural downtime -- buffer accordingly

---

## Step 2: Particle Accelerator (Modified)

The existing Particle Accelerator gains a new recipe alongside its existing thorium and ender pearl recipes.

### New Recipe
- **Input**: 1 Raw Photon
- **Output**: 1 Excited Photon
- **Helium cost**: 200 mB per craft
- **Energy**: 500 RF/t (same as existing recipes)
- **Base time**: 200 ticks (10 seconds), scaled by momentum windup
- **Momentum**: Uses existing windup mechanic (0.5x-2.0x speed)

### Existing Recipes (Unchanged)
- Thorium Ingot --> Tachyon Shard (1000 mB Helium)
- Ender Pearl --> Tachyon Shard (100 mB Helium)

---

## Step 3: Photon Compressor

A single-block machine with GUI that compresses Excited Photons into Condensed Light using RF power and Glowstone Dust as a catalyst.

### Mechanics
- **Input Slot 1**: Excited Photon
- **Input Slot 2**: Glowstone Dust (consumed as compression catalyst)
- **Output Slot**: Condensed Light
- **Energy consumption**: 2,000 RF/t
- **Energy buffer**: 500,000 RF
- **Process time**: 100 ticks (5 seconds) per Condensed Light
- **Recipe**: 1 Excited Photon + 1 Glowstone Dust --> 1 Condensed Light

### GUI
Standard machine GUI with energy bar (left side), two input slots, progress arrow, and output slot.

---

## Throughput Analysis

At peak efficiency (noon, clear sky, max accelerator momentum):
- **Solar Collector**: ~1 Raw Photon / 10 seconds
- **Accelerator at 2x speed**: ~1 Excited Photon / 5 seconds
- **Compressor**: ~1 Condensed Light / 5 seconds

**Bottleneck**: Solar collection. To keep the pipeline running continuously:
- 1 Collector feeds 1 Accelerator (with idle time)
- 2-3 Collectors can keep 1 Accelerator + 1 Compressor running near-continuously
- Night and weather create natural gaps -- buffer Raw Photons

**Condensed Light consumption** in Photonic Injector:
- At injection rate 1.00/t: 1 Condensed Light per 20 seconds
- At injection rate 0.10/t: 1 Condensed Light per 200 seconds
- Multiple collectors are needed for sustained high injection rates

---

## Material Dependencies

```
Glowstone Dust + Redstone + Amethyst Shard --> Photonic Matrix (crafting table, unchanged)
Sunlight --> Raw Photon (Solar Collector)
Raw Photon + Helium --> Excited Photon (Particle Accelerator)
Excited Photon + Glowstone Dust --> Condensed Light (Photon Compressor)
Condensed Light --> Photonic Injector --> Singularity Engine
```
