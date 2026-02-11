# Plan: Add Titanium, Tungsten, Lithium, and Thorium Ores

## Context
Adding 4 real-world ores to the Tachyon mod with full NeoForge cross-mod compatibility via `c:` convention tags. Each ore gets stone + deepslate variants, raw ore drops, ingots, storage blocks, worldgen, recipes, and loot tables.

## Ore Properties

| Ore | Hardness | Tool Tier | Vein Size | Veins/Chunk | Y Range | XP |
|-----|----------|-----------|-----------|-------------|---------|-----|
| Titanium | 3.0/3.0 | Iron | 7 | 6 | -64 to 0 | 0.7 |
| Tungsten | 4.5/6.0 | Iron | 6 | 5 | -64 to 16 | 0.8 |
| Lithium | 2.5/2.5 | Iron | 9 | 8 | -32 to 64 | 0.5 |
| Thorium | 4.0/3.0 | Diamond | 5 | 4 | -64 to -16 | 1.0 |

## Per-Ore Items (x4 ores = 12 blocks + 8 items)
- `<ore>_ore` block (stone variant)
- `deepslate_<ore>_ore` block
- `<ore>_block` storage block (9 ingots)
- `raw_<ore>` item (ore drop)
- `<ore>_ingot` item (smelted from raw)

## Implementation Steps

### 1. `ModBlocks.java`
**Path:** `src/main/java/com/setusertso/tachyon/ModBlocks.java`
- Own `DeferredRegister.Blocks` and `DeferredRegister.Items` (for BlockItems)
- Register all 12 blocks (4 ores + 4 deepslate + 4 storage)
- Storage blocks: metal properties, ore blocks: stone properties
- Call `register(bus)` from main class

### 2. `ModItems.java`
**Path:** `src/main/java/com/setusertso/tachyon/ModItems.java`
- Own `DeferredRegister.Items`
- Register 8 items (4 raw ores + 4 ingots)
- Call `register(bus)` from main class

### 3. Update `tachyon.java`
**Path:** `src/main/java/com/setusertso/tachyon/tachyon.java`
- Call `ModBlocks.register(modEventBus)` and `ModItems.register(modEventBus)`
- Add all new items/blocks to creative tab

### 4. Resource Files (per ore, x4)

**Blockstates** (12 files): `assets/tachyon/blockstates/<block>.json`
**Block models** (12 files): `assets/tachyon/models/block/<block>.json` (parent: `cube_all`)
**Item models - blocks** (12 files): `assets/tachyon/models/item/<block>.json` (parent: block model)
**Item models - items** (8 files): `assets/tachyon/models/item/<item>.json` (parent: `item/generated`, layer0 texture)
**Textures - blocks** (12 files): `assets/tachyon/textures/block/<block>.png`
**Textures - items** (8 files): `assets/tachyon/textures/item/<item>.png` (recolored from vanilla iron)

### 5. Loot Tables (12 files)
`data/tachyon/loot_table/blocks/<block>.json` (NOTE: singular `loot_table`, plural `blocks`)
- Ore blocks: drop raw ore with Fortune support (`apply_bonus` + `ore_drops`)
- Storage blocks: drop themselves (`survives_explosion`)

### 6. Recipes (per ore x4 = 16 files)
`data/tachyon/recipe/` (NOTE: singular `recipe`)
- `<ore>_ingot_from_smelting.json` (raw ore -> ingot, 200 ticks)
- `<ore>_ingot_from_blasting.json` (raw ore -> ingot, 100 ticks)
- `<ore>_block.json` (shaped 3x3 ingots -> block)
- `<ore>_ingot_from_block.json` (shapeless block -> 9 ingots)

Recipe format must use object ingredients: `{"item": "tachyon:raw_titanium"}` not plain strings.

### 7. Common Tags (`c:` namespace for cross-mod compat)

**Block tags** under `data/c/tags/block/` (NOTE: singular `block`):
- `ores/<ore>.json` - lists ore + deepslate ore
- `storage_blocks/<ore>.json` - lists storage block

**Item tags** under `data/c/tags/item/` (NOTE: singular `item`):
- `ores/<ore>.json` - ore block items
- `ingots/<ore>.json` - ingot
- `raw_materials/<ore>.json` - raw ore

### 8. Minecraft Tags (tool requirements)
`data/minecraft/tags/block/` (NOTE: singular `block`)
- `mineable/pickaxe.json` - all 12 blocks (ores + storage), `replace: false`
- `needs_iron_tool.json` - titanium, tungsten, lithium ores + deepslate + storage, `replace: false`
- `needs_diamond_tool.json` - thorium ores + deepslate + storage, `replace: false`

### 9. Worldgen (per ore = 12 files)
- `data/tachyon/worldgen/configured_feature/<ore>_ore.json` - ore config with stone + deepslate targets
- `data/tachyon/worldgen/placed_feature/<ore>_ore.json` - placement (count, height range, biome)
- `data/tachyon/neoforge/biome_modifier/add_<ore>_ore.json` - add to overworld

### 10. Lang File Update
Add translation entries to `en_us.json` for each ore's blocks and items.

### 11. Textures
Recolor vanilla iron textures (ingot and raw) via Python/Pillow for each ore:
- Titanium: silver/blue tone (hue=0.55, low saturation)
- Tungsten: dark gray/steel (hue=0.6, very low saturation, darker)
- Lithium: light pink/white (hue=0.85, low saturation, bright)
- Thorium: green-tinted (hue=0.35, moderate saturation)

## Cross-Mod Compatibility Notes
- Use `c:` namespace for convention tags (NOT `neoforge:` or `forge:`)
- Always use `"replace": false` when adding to existing tags
- Other mods using the same `c:ores/titanium` tag will be interchangeable in recipes
- Mods like JAOPCA or Unified Resources can deduplicate ores at runtime

## Status
- [x] Titanium - fully implemented and tested
- [ ] Tungsten
- [ ] Lithium
- [ ] Thorium
