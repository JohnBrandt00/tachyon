# Tachyon Mod Changelog

## v1.0.0 - Titanium Ore (Working)

### Added
- **Titanium Ore** - spawns in stone between Y -64 and 0, 7 vein size, 6 veins/chunk
- **Deepslate Titanium Ore** - deepslate variant, same drops
- **Block of Titanium** - storage block (9 ingots <-> 1 block)
- **Raw Titanium** - dropped from ore, Fortune-compatible
- **Titanium Ingot** - smelted from raw titanium (furnace: 200t, blast: 100t)
- **Tachyon creative tab** with all items
- **World generation** via JSON datapacks (configured_feature + placed_feature + biome_modifier)
- **Cross-mod compatibility** via `c:` convention tags (ores, ingots, raw_materials, storage_blocks)
- **Item textures** recolored from vanilla iron (silver-blue titanium tone)

### Bug Fixes
- **Tags directory renamed** from `tags/blocks/` to `tags/block/` and `tags/items/` to `tags/item/` (Minecraft 1.21 pack format 48 change). This fixed:
  - Ore was unmineably slow (no `mineable/pickaxe` tag found)
  - Ore dropped nothing (no `needs_iron_tool` tag found + `requiresCorrectToolForDrops()`)
- **Loot tables directory renamed** from `loot_tables/` to `loot_table/` (1.21 change)
- **Recipes directory renamed** from `recipes/` to `recipe/` (1.21 change)
- **Recipe ingredient format fixed** from plain strings to `{"item": "..."}` objects to match vanilla format
- **Creative tab ID fixed** from uppercase "Tachyon" to lowercase "tachyon" (resource locations must be `[a-z0-9/._-]`)

### Files Structure (per ore)
```
src/main/resources/
  assets/tachyon/
    blockstates/{ore}_ore.json, deepslate_{ore}_ore.json, {ore}_block.json
    models/block/{ore}_ore.json, deepslate_{ore}_ore.json, {ore}_block.json
    models/item/{ore}_ore.json, deepslate_{ore}_ore.json, {ore}_block.json, raw_{ore}.json, {ore}_ingot.json
    textures/block/{ore}_ore.png, deepslate_{ore}_ore.png, {ore}_block.png
    textures/item/raw_{ore}.png, {ore}_ingot.png
  data/
    tachyon/
      loot_table/blocks/{ore}_ore.json, deepslate_{ore}_ore.json, {ore}_block.json
      recipe/{ore}_ingot_from_smelting.json, _from_blasting.json, {ore}_block.json, {ore}_ingot_from_block.json
      worldgen/configured_feature/{ore}_ore.json
      worldgen/placed_feature/{ore}_ore.json
      neoforge/biome_modifier/add_{ore}_ore.json
    minecraft/tags/block/
      mineable/pickaxe.json
      needs_iron_tool.json (or needs_diamond_tool.json)
    c/tags/block/ores/{ore}.json, storage_blocks/{ore}.json
    c/tags/item/ores/{ore}.json, ingots/{ore}.json, raw_materials/{ore}.json
```
