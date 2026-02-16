# Crafting Materials & Intermediates

**Category**: Items used as ingredients in crafting recipes
**Range**: All tiers

---

## Overview

These items form the backbone of the mod's crafting economy. Each material gates a specific tier of content and creates meaningful progression bottlenecks.

---

## Tier 1 Materials

### Tachyon Alloy Ingot
| Property | Value |
|----------|-------|
| Registry | `tachyon_alloy_ingot` |
| Source | Alloy Forge (Titanium + Tungsten) |
| Stack Size | 64 |
| Used In | Tachyon tools, Tachyon armor, machine recipes, Tachyon Core |

The mid-game workhorse material. Gates access to the first custom tool and armor tier.

### Reactor Plating
| Property | Value |
|----------|-------|
| Registry | `reactor_plating` |
| Source | Alloy Forge (Lithium + Thorium) |
| Stack Size | 64 |
| Used In | Thorium Reactor, advanced machine casings, Photon Compressor |

Heavy-duty material for machine construction. Required for most Tier 2+ machines.

### Crushed Ores (7 types)
| Item | Registry | Source |
|------|----------|--------|
| Crushed Titanium | `crushed_titanium` | Ore Crusher |
| Crushed Tungsten | `crushed_tungsten` | Ore Crusher |
| Crushed Lithium | `crushed_lithium` | Ore Crusher |
| Crushed Thorium | `crushed_thorium` | Ore Crusher |
| Crushed Iron | `crushed_iron` | Ore Crusher |
| Crushed Gold | `crushed_gold` | Ore Crusher |
| Crushed Copper | `crushed_copper` | Ore Crusher |

All smelt into their respective ingots. The Ore Crusher produces 2x dust per raw ore.

---

## Tier 2 Materials

### Tachyon Core
| Property | Value |
|----------|-------|
| Registry | `tachyon_core` |
| Source | Crafting Table |
| Stack Size | 16 |
| Used In | Powered tools, Charging Station, Temporal Stabilizer |

The central component for RF-powered tools and advanced machines.

#### Crafting Recipe
```
[Tachyon Shard]     [Tachyon Alloy]    [Tachyon Shard]
[Tachyon Alloy]     [Diamond]          [Tachyon Alloy]
[Tachyon Shard]     [Tachyon Alloy]    [Tachyon Shard]
```

### Energy Crystal
| Property | Value |
|----------|-------|
| Registry | `energy_crystal` |
| Source | Alloy Forge (Iron + Tachyon Shard) |
| Stack Size | 64 |
| Used In | Powered tools, Tachyon Battery, Temporal Stabilizer |

A crystallized form of tachyon energy used in RF-capable items.

### Raw Photon
| Property | Value |
|----------|-------|
| Registry | `raw_photon` |
| Source | Solar Collector |
| Stack Size | 64 |
| Used In | Particle Accelerator → Excited Photon |

Captured sunlight in item form. First step of the Condensed Light pipeline.

### Excited Photon
| Property | Value |
|----------|-------|
| Registry | `excited_photon` |
| Source | Particle Accelerator (from Raw Photon) |
| Stack Size | 64 |
| Used In | Photon Compressor → Condensed Light, Photon Charge |

Photons accelerated to near-light speed. Glows with a bright white shimmer.

---

## Tier 3 Materials

### Quantum Processor
| Property | Value |
|----------|-------|
| Registry | `quantum_processor` |
| Source | Alloy Forge (Tachyon Alloy + Graviton Crystal) |
| Stack Size | 16 |
| Used In | Void Miner, Spatial Anchor, Null Field Generator, Matter Fabricator |

An advanced computational component required for the most sophisticated machines.

### Quantum Link Card
| Property | Value |
|----------|-------|
| Registry | `quantum_link_card` |
| Source | Crafting Table |
| Stack Size | 16 |
| Used In | Quantum Entangler (linking) |

Stores paired positions for the Quantum Entangler. Consumed on use.

---

## Tier 4 Materials

### Graviton Crystal
| Property | Value |
|----------|-------|
| Registry | `graviton_crystal` |
| Source | Graviton Condenser (Singularity Engine byproduct) |
| Stack Size | 16 |
| Rarity | EPIC (purple name) |
| Used In | Exotic Matter Infuser (catalyst), Graviton Pill, Gravity Staff, Quantum Processor |

Rare crystallized gravity. The primary gate between Tier 4 and Tier 5 content.

### Exotic Shard
| Property | Value |
|----------|-------|
| Registry | `exotic_shard` |
| Source | Alloy Forge (Tachyon Alloy + Exotic Matter) |
| Stack Size | 64 |
| Used In | Exotic Matter Infuser recipes, Exotic Elixir, Warp Pad, Null Field Generator |

Stabilized fragment of exotic matter, safe to handle. The universal endgame ingredient.

### Antimatter
| Property | Value |
|----------|-------|
| Registry | `antimatter` |
| Source | Singularity Engine (mass > 1800) |
| Stack Size | 1 |
| Rarity | EPIC (purple name) |
| Special | Explodes if dropped! |
| Used In | Antimatter Containment, Antimatter Bomb, special recipes |

Extremely dangerous material. See [Antimatter Containment](../machines/antimatter-containment.md) for safety details.

---

## Tier 5 Materials

### UU-Matter (Fluid)
| Property | Value |
|----------|-------|
| Registry | `uu_matter` |
| Source | Matter Fabricator |
| Type | Fluid (bucket-able) |
| Used In | Matter Replicator |

Pure condensed energy-matter that can be shaped into any base material.

### Tachyon Relay Linker (Tool)
| Property | Value |
|----------|-------|
| Registry | `tachyon_relay_linker` |
| Source | Crafting Table |
| Stack Size | 1 |
| Used In | Linking Tachyon Relay pairs |

Reusable tool for creating wireless energy links.

---

## Material Flow Diagram

```
RAW ORES ──► INGOTS ──► ALLOY FORGE ──► Tachyon Alloy
                                    ──► Reactor Plating
                                    ──► Energy Crystal
                                    ──► Exotic Shard
                                    ──► Quantum Processor

RAW ORES ──► ORE CRUSHER ──► CRUSHED DUST ──► FURNACE ──► 2x INGOTS

TACHYON SHARD + TACHYON ALLOY ──► TACHYON CORE ──► POWERED TOOLS

SUNLIGHT ──► RAW PHOTON ──► EXCITED PHOTON ──► CONDENSED LIGHT

SINGULARITY ENGINE ──► EXOTIC MATTER ──► EXOTIC SHARD
                  ──► GRAVITON CRYSTAL (condenser)
                  ──► ANTIMATTER (high mass)

EXOTIC SHARD + BASE ITEM ──► EXOTIC TOOLS / ARMOR (Infuser)

MASSIVE RF ──► UU-MATTER (Fabricator) ──► ANY ITEM (Replicator)
```

---

## Registration Summary

### New Items to Register in ModItems.java

| Item | Simple? | Notes |
|------|---------|-------|
| `TACHYON_ALLOY_INGOT` | Yes | Simple item |
| `REACTOR_PLATING` | Yes | Simple item |
| `CRUSHED_TITANIUM` | Yes | Simple item |
| `CRUSHED_TUNGSTEN` | Yes | Simple item |
| `CRUSHED_LITHIUM` | Yes | Simple item |
| `CRUSHED_THORIUM` | Yes | Simple item |
| `CRUSHED_IRON` | Yes | Simple item |
| `CRUSHED_GOLD` | Yes | Simple item |
| `CRUSHED_COPPER` | Yes | Simple item |
| `TACHYON_CORE` | Yes | Stack 16 |
| `ENERGY_CRYSTAL` | Yes | Simple item |
| `RAW_PHOTON` | Yes | Simple item |
| `EXCITED_PHOTON` | Yes | Simple item |
| `QUANTUM_PROCESSOR` | Yes | Stack 16 |
| `QUANTUM_LINK_CARD` | Yes | Stack 16 |
| `GRAVITON_CRYSTAL` | Yes | Stack 16, EPIC rarity |
| `EXOTIC_SHARD` | Yes | Simple item |
| `ANTIMATTER` | No | Custom item class (explosion behavior) |
| `TACHYON_RELAY_LINKER` | No | Custom item class (right-click behavior) |
| `UU_MATTER_BUCKET` | No | Bucket item for UU-Matter fluid |

**Total: 20 new material items**

---

## Texture Style Notes

- **Alloys/Plating**: Metallic, slightly textured surfaces
- **Crushed ores**: Powder/dust texture matching ore color
- **Crystals**: Translucent, gem-like, glowing
- **Tachyon Core**: Mechanical/tech look with glowing center
- **Exotic materials**: Purple/void-themed with particle-like glow
- **Antimatter**: Dark red/black with unstable shimmer
- **Quantum items**: Blue-cyan with circuit-like patterns
