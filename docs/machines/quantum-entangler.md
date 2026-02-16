# Quantum Entangler

**Tier**: 3 — Wireless Infrastructure
**Type**: Single-block machine (paired)
**Category**: Logistics

---

## Overview

The Quantum Entangler creates wireless links between two points for transferring energy, items, or fluids. Place two Entanglers, link them with a Quantum Link Card, and they form a bridge that works across any distance — even cross-dimensional. The backbone of a late-game logistics network.

---

## Specifications

| Property | Value |
|----------|-------|
| Energy Consumption | 100 RF/t base + transfer costs |
| Internal RF Buffer | 500,000 RF |
| Item Transfer | Up to 1 stack per second |
| Energy Transfer | Up to 10,000 RF/t |
| Fluid Transfer | Up to 1,000 mB/t |
| Link Range | Unlimited (same dimension), 10x cost cross-dimension |
| Max Links | 1 per Entangler (point-to-point only) |
| Hardness/Resistance | 6.0 / 8.0 |
| Tool | Pickaxe |

---

## Linking System

### Quantum Link Card (New Item)
- Crafted item that stores a link between two positions
- Right-click Entangler A → Card stores position A
- Right-click Entangler B → Card writes position B, both entanglers are now linked
- Card is consumed on successful link
- Breaking either Entangler breaks the link

### Transfer Modes
Each Entangler has a mode selector (cycle with right-click while sneaking):

| Mode | Behavior | Cost |
|------|----------|------|
| Energy | Pushes/pulls RF between linked entanglers | 100 RF/t + 10% energy tax |
| Items | Pulls items from linked entangler's buffer | 100 RF/t + 500 RF per stack |
| Fluid | Pushes/pulls fluid between linked entanglers | 100 RF/t + 50 RF per 100mB |
| All | Transfers everything (energy, items, fluids) | 100 RF/t + all applicable costs |

### Cross-Dimensional
- Works between Overworld/Nether/End
- 10x energy cost multiplier for cross-dimensional transfers
- Requires both chunks to be loaded (pair with Spatial Anchor)

---

## GUI Layout

```
┌──────────────────────────────────────────────────┐
│            Quantum Entangler                     │
│                                                  │
│  [Energy]   Mode: [Items ▼]    Status: LINKED    │
│   ██████                                         │
│   ██████    Link: (123, 64, -456)                │
│   ██████    Dim: minecraft:overworld             │
│   ██████                                         │
│   ██████    [Item Buffer: 9 slots]               │
│             ┌──┬──┬──┐                           │
│             │  │  │  │                           │
│             ├──┼──┼──┤                           │
│             │  │  │  │                           │
│             ├──┼──┼──┤                           │
│             │  │  │  │                           │
│             └──┴──┴──┘                           │
│                                                  │
│  ┌──────────────────────────────────────┐        │
│  │     Player Inventory                 │        │
│  └──────────────────────────────────────┘        │
└──────────────────────────────────────────────────┘
```

- 9-slot item buffer for item transfer mode
- Energy bar showing internal buffer
- Link status display (coordinates + dimension of linked partner)
- Mode selector dropdown

---

## ContainerData (6 slots)

| Index | Data |
|-------|------|
| 0 | energy stored |
| 1 | energy capacity |
| 2 | mode (0=energy, 1=items, 2=fluid, 3=all) |
| 3 | linked (0=no, 1=yes) |
| 4 | transfer rate (items/tick or RF/t depending on mode) |
| 5 | cross-dimensional (0=no, 1=yes) |

---

## Visual

- **Blockstate**: `LINKED` boolean + `ACTIVE` boolean
  - `linked=true, active=true`: Pulsing ender-like particle beam upward, light level 10
  - `linked=true, active=false`: Dim glow, connected but idle
  - `linked=false`: No particles, dark
- Particle: Vertical beam of purple-cyan particles when actively transferring
- Sound: Soft warping hum when active

---

## Crafting Recipe

```
[Ender Pearl]      [Tachyon Alloy]    [Ender Pearl]
[Tachyon Alloy]    [Graviton Crystal] [Tachyon Alloy]
[Reactor Plating]  [Redstone Block]   [Reactor Plating]
```

### Quantum Link Card Recipe
```
[              ] [Ender Pearl]    [              ]
[Gold Ingot]     [Tachyon Shard]  [Gold Ingot]
[              ] [Paper]          [              ]
```

*Produces 2 link cards per craft.*

---

## New Items

| Item | Registry | Description |
|------|----------|-------------|
| Quantum Link Card | `quantum_link_card` | Stores paired positions, consumed on linking |

---

## Implementation Notes

### New Files
- `block/QuantumEntanglerBlock.java`
- `block/entity/QuantumEntanglerBlockEntity.java`
- `menu/QuantumEntanglerMenu.java`
- `screen/QuantumEntanglerScreen.java`

### Technical Details
- Linked position stored as `BlockPos` + `ResourceKey<Level>` in NBT
- Each tick: check if partner chunk is loaded → if so, transfer
- If partner chunk not loaded → idle (no orphan energy drain)
- Energy tax prevents free energy duplication
- Item transfer uses pull model: pulls from partner's capability
