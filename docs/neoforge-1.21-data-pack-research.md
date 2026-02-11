# NeoForge 1.21.1 Data Pack Research

## Critical: Directory Name Changes in 1.21 (Pack Format 48)

Minecraft 1.21 (pack format 48) changed many data pack directory names from **plural to singular**:

| Old (pre-1.21) | New (1.21+) |
|---|---|
| `data/<ns>/loot_tables/` | `data/<ns>/loot_table/` |
| `data/<ns>/recipes/` | `data/<ns>/recipe/` |
| `data/<ns>/advancements/` | `data/<ns>/advancement/` |
| `data/<ns>/structures/` | `data/<ns>/structure/` |
| `data/<ns>/tags/blocks/` | `data/<ns>/tags/block/` |
| `data/<ns>/tags/items/` | `data/<ns>/tags/item/` |
| `data/<ns>/tags/entity_types/` | `data/<ns>/tags/entity_type/` |
| `data/<ns>/tags/fluids/` | `data/<ns>/tags/fluid/` |
| `data/<ns>/tags/game_events/` | `data/<ns>/tags/game_event/` |

**Exception**: Inside `loot_table/`, the subfolder `blocks/` remains **plural** (it refers to the loot parameter set, not a registry).

Verified from:
- `server_extracted.jar` at `.gradle/caches/minecraft/versions/1.21.1/`
- `neoforge-21.1.219.jar` at `.gradle/repositories/ng_dummy_ng/`

## Block Tags

### Tool Type Tags (mineable)
Path: `data/minecraft/tags/block/mineable/<tool>.json`

Available tools: `pickaxe`, `axe`, `shovel`, `hoe`

```json
{
  "replace": false,
  "values": [
    "mymod:my_block",
    "mymod:my_other_block"
  ]
}
```

### Tool Tier Tags (dual system in 1.21)

1.21 has BOTH the old positive and new negative tag systems:

**Positive (still works):** `data/minecraft/tags/block/needs_<tier>_tool.json`
- `needs_stone_tool.json`
- `needs_iron_tool.json`
- `needs_diamond_tool.json`

**Negative (new in 1.21):** `data/minecraft/tags/block/incorrect_for_<tier>_tool.json`
- `incorrect_for_wooden_tool.json` - references `#minecraft:needs_stone_tool`, `#minecraft:needs_iron_tool`, `#minecraft:needs_diamond_tool`
- `incorrect_for_stone_tool.json` - references `#minecraft:needs_iron_tool`, `#minecraft:needs_diamond_tool`
- `incorrect_for_gold_tool.json` - references `#minecraft:needs_stone_tool`, `#minecraft:needs_iron_tool`, `#minecraft:needs_diamond_tool`
- `incorrect_for_iron_tool.json` - references `#neoforge:needs_netherite_tool` (NeoForge extension)
- `incorrect_for_diamond_tool.json` - references `#neoforge:needs_netherite_tool` (NeoForge extension)

**The `incorrect_for_*` tags automatically cascade from `needs_*` tags**, so adding blocks to `needs_iron_tool` is sufficient - the `incorrect_for_wooden_tool`, `incorrect_for_stone_tool`, and `incorrect_for_gold_tool` tags will automatically include those blocks.

### Using `needs_iron_tool` for iron-tier ore:
```json
{
  "replace": false,
  "values": [
    "mymod:my_ore",
    "mymod:deepslate_my_ore"
  ]
}
```

## Loot Tables

Path: `data/<namespace>/loot_table/blocks/<block_name>.json`

### Ore drops (with Fortune support):
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "mymod:raw_ore",
          "functions": [
            {
              "function": "minecraft:apply_bonus",
              "enchantment": "minecraft:fortune",
              "formula": "minecraft:ore_drops"
            },
            {
              "function": "minecraft:explosion_decay"
            }
          ]
        }
      ]
    }
  ]
}
```

### Self-drop block (with explosion protection):
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "conditions": [
        { "condition": "minecraft:survives_explosion" }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "mymod:my_block"
        }
      ]
    }
  ]
}
```

## Common Tags (c: namespace, NeoForge convention)

All use **singular** directory names in 1.21.1:

- `data/c/tags/block/ores/<material>.json` - ore blocks
- `data/c/tags/block/storage_blocks/<material>.json` - storage blocks
- `data/c/tags/item/ores/<material>.json` - ore block items
- `data/c/tags/item/ingots/<material>.json` - ingots
- `data/c/tags/item/raw_materials/<material>.json` - raw ores

## Bug Found in Tachyon Mod

**Root cause**: All tag files used **plural** directory names (`tags/blocks/`, `tags/items/`) instead of **singular** (`tags/block/`, `tags/item/`).

This caused:
1. `mineable/pickaxe` tag not found -> block has no preferred tool -> mines at hand speed
2. `needs_iron_tool` tag not found -> with `requiresCorrectToolForDrops()` set -> no drops

**Fix**: Rename all tag directories from plural to singular:
- `data/minecraft/tags/blocks/` -> `data/minecraft/tags/block/`
- `data/c/tags/blocks/` -> `data/c/tags/block/`
- `data/c/tags/items/` -> `data/c/tags/item/`
