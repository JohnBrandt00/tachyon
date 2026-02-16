# Tachyon Relay

**Tier**: 2 — Wireless Energy
**Type**: Single-block (paired transmitter + receiver)
**Category**: Energy Logistics

---

## Overview

The Tachyon Relay is a simple wireless energy transmission system. Place a Transmitter, place a Receiver, link them, and energy flows wirelessly. Cheaper and simpler than the Quantum Entangler but energy-only and limited range.

---

## Two Block Types

### Tachyon Relay Transmitter
- Receives energy from adjacent blocks
- Beams energy wirelessly to linked Receiver
- Energy tax: 5% loss during transmission

### Tachyon Relay Receiver
- Receives energy wirelessly from linked Transmitter
- Outputs energy to adjacent blocks (auto-push)
- Passive — no energy cost to operate

---

## Specifications

| Property | Transmitter | Receiver |
|----------|------------|----------|
| Max Transfer | 25,000 RF/t | 25,000 RF/t output |
| Internal Buffer | 100,000 RF | 100,000 RF |
| Range | 64 blocks (same dimension only) | — |
| Energy Tax | 5% | None |
| Links | 1 Receiver per Transmitter | 1 Transmitter per Receiver |
| Hardness | 3.5 | 3.5 |

---

## Linking

### Tachyon Relay Linker (New Item)
- Right-click Transmitter → stores position
- Right-click Receiver → creates link
- Shows particle beam between linked blocks when successful
- Linker is NOT consumed — reusable tool

### Link Validation
- Both blocks must be in same dimension
- Distance must be <= 64 blocks
- Line of sight NOT required (goes through walls)
- Breaking either block breaks the link

---

## Visual

### Transmitter
- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Blue energy core glowing, particles shooting upward
  - `active=false`: Dim core
- Particle beam: Visible energy stream toward Receiver when transmitting

### Receiver
- **Blockstate**: `ACTIVE` boolean
  - `active=true`: Blue energy core glowing, particles drifting down
  - `active=false`: Dim core

### Energy Beam
- Rendered between Transmitter and Receiver when active
- Thin blue line, visible through blocks (like beacon beam but horizontal)
- Pulse animation along the beam direction

---

## Crafting Recipes

### Transmitter
```
[Iron Ingot]   [Tachyon Shard]  [Iron Ingot]
[Redstone]     [Tachyon Alloy]  [Redstone]
[Iron Ingot]   [Redstone Block] [Iron Ingot]
```

### Receiver
```
[Iron Ingot]   [Redstone]       [Iron Ingot]
[Redstone]     [Tachyon Alloy]  [Redstone]
[Iron Ingot]   [Tachyon Shard]  [Iron Ingot]
```

### Tachyon Relay Linker
```
[              ] [Tachyon Shard] [              ]
[              ] [Gold Ingot]    [              ]
[              ] [Iron Ingot]    [              ]
```

---

## Interaction (No GUI)

### Right-Click
- Shows status: `"Transmitter: Linked to (x, y, z) — Transferring 15,230 RF/t"`
- Or: `"Transmitter: Not linked"`
- Receiver: `"Receiver: Linked to (x, y, z) — Receiving 14,468 RF/t"`

### Sneak + Right-Click
- Breaks the link (if linked)
- Shows confirmation: `"Link broken"`

---

## Implementation Notes

### New Files
- `block/TachyonRelayTransmitterBlock.java`
- `block/TachyonRelayReceiverBlock.java`
- `block/entity/TachyonRelayTransmitterBlockEntity.java`
- `block/entity/TachyonRelayReceiverBlockEntity.java`
- `client/TachyonRelayBeamRenderer.java` — beam rendering between pairs

### New Items
- `TACHYON_RELAY_LINKER` — tool item with right-click behavior + stored BlockPos NBT

### Technical Details
- Transmitter stores Receiver BlockPos in NBT
- Each tick: check if Receiver exists at stored pos → push energy
- 5% tax: transmit `amount * 0.95` to receiver
- Beam renderer: custom BlockEntityRenderer that draws a line between positions
- No menu/screen needed
