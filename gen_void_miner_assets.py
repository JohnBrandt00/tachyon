import json
import os

base = "d:/MCMODDING/tachyon/src/main/resources"

def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")

# ========== VOID MINING DROP RECIPES ==========
vm_dir = f"{base}/data/tachyon/recipe/void_mining"
drops = [
    ("raw_iron", "minecraft:raw_iron", 3, 20, 2, 4, 1, "common_ore"),
    ("raw_copper", "minecraft:raw_copper", 3, 20, 2, 4, 1, "common_ore"),
    ("raw_gold", "minecraft:raw_gold", 1, 15, 1, 2, 1, "common_ore"),
    ("coal", "minecraft:coal", 4, 15, 3, 6, 1, "common_ore"),
    ("redstone", "minecraft:redstone", 4, 12, 3, 6, 1, "common_ore"),
    ("lapis_lazuli", "minecraft:lapis_lazuli", 3, 10, 2, 4, 1, "common_ore"),
    ("raw_titanium", "tachyon:raw_titanium", 2, 12, 1, 3, 2, "mod_ore"),
    ("raw_tungsten", "tachyon:raw_tungsten", 2, 12, 1, 3, 2, "mod_ore"),
    ("raw_lithium", "tachyon:raw_lithium", 1, 10, 1, 2, 2, "mod_ore"),
    ("raw_thorium", "tachyon:raw_thorium", 1, 10, 1, 2, 2, "mod_ore"),
    ("diamond", "minecraft:diamond", 1, 8, 1, 2, 2, "gem"),
    ("emerald", "minecraft:emerald", 1, 8, 1, 2, 2, "gem"),
    ("amethyst_shard", "minecraft:amethyst_shard", 3, 8, 2, 4, 2, "gem"),
    ("tachyon_shard", "tachyon:tachyon_shard", 1, 6, 1, 2, 3, "rare"),
    ("glowstone_dust", "minecraft:glowstone_dust", 3, 6, 2, 4, 3, "rare"),
    ("quartz", "minecraft:quartz", 3, 6, 2, 4, 3, "rare"),
    ("ender_pearl", "minecraft:ender_pearl", 1, 5, 1, 2, 3, "rare"),
    ("ancient_debris", "minecraft:ancient_debris", 1, 3, 1, 1, 4, "ultra_rare"),
    ("exotic_shard", "tachyon:exotic_shard", 1, 2, 1, 1, 4, "ultra_rare"),
    ("graviton_crystal", "tachyon:graviton_crystal", 1, 2, 1, 1, 4, "ultra_rare"),
    ("blaze_powder", "minecraft:blaze_powder", 3, 8, 2, 4, 2, "nether"),
    ("nether_quartz", "minecraft:quartz", 4, 8, 3, 5, 2, "nether"),
    ("gold_nugget", "minecraft:gold_nugget", 6, 6, 4, 8, 1, "nether"),
    ("end_stone", "minecraft:end_stone", 1, 6, 1, 2, 2, "end"),
    ("chorus_fruit", "minecraft:chorus_fruit", 1, 5, 1, 2, 3, "end"),
]
for name, item, rc, w, minc, maxc, tier, cat in drops:
    write_json(f"{vm_dir}/{name}.json", {
        "type": "tachyon:void_mining",
        "result_item": item,
        "result_count": rc,
        "weight": w,
        "min_count": minc,
        "max_count": maxc,
        "tier": tier,
        "category": cat,
    })

# ========== CRAFTING RECIPES ==========
recipe_dir = f"{base}/data/tachyon/recipe"
crafting = {
    "void_miner_controller": {
        "type": "minecraft:crafting_shaped",
        "pattern": ["TQT", "QEQ", "TQT"],
        "key": {"T": {"item": "tachyon:tachyon_alloy_ingot"}, "Q": {"item": "tachyon:quantum_processor"}, "E": {"item": "tachyon:exotic_matter"}},
        "result": {"id": "tachyon:void_miner_controller", "count": 1},
    },
    "void_frame": {
        "type": "minecraft:crafting_shaped",
        "pattern": ["IRI", "R R", "IRI"],
        "key": {"I": {"item": "minecraft:iron_block"}, "R": {"item": "tachyon:reactor_plating"}},
        "result": {"id": "tachyon:void_frame", "count": 4},
    },
    "stabilized_void_frame": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" E ", "EFE", " E "],
        "key": {"E": {"item": "tachyon:energy_crystal"}, "F": {"item": "tachyon:void_frame"}},
        "result": {"id": "tachyon:stabilized_void_frame", "count": 1},
    },
    "reinforced_void_frame": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" T ", "TFT", " T "],
        "key": {"T": {"item": "tachyon:tachyon_alloy_ingot"}, "F": {"item": "tachyon:stabilized_void_frame"}},
        "result": {"id": "tachyon:reinforced_void_frame", "count": 1},
    },
    "quantum_void_frame": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" Q ", "QFQ", " Q "],
        "key": {"Q": {"item": "tachyon:quantum_processor"}, "F": {"item": "tachyon:reinforced_void_frame"}},
        "result": {"id": "tachyon:quantum_void_frame", "count": 1},
    },
    "void_miner_port": {
        "type": "minecraft:crafting_shaped",
        "pattern": ["ITI", "THT", "ITI"],
        "key": {"I": {"item": "minecraft:iron_ingot"}, "T": {"item": "tachyon:tachyon_alloy_ingot"}, "H": {"item": "minecraft:hopper"}},
        "result": {"id": "tachyon:void_miner_port", "count": 2},
    },
    "ore_extraction_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" I ", "IEI", " I "],
        "key": {"I": {"item": "minecraft:iron_block"}, "E": {"item": "tachyon:energy_crystal"}},
        "result": {"id": "tachyon:ore_extraction_module", "count": 1},
    },
    "gem_resonance_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" D ", "DED", " D "],
        "key": {"D": {"item": "minecraft:diamond"}, "E": {"item": "tachyon:energy_crystal"}},
        "result": {"id": "tachyon:gem_resonance_module", "count": 1},
    },
    "rare_earth_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" T ", "TET", " T "],
        "key": {"T": {"item": "tachyon:tachyon_alloy_ingot"}, "E": {"item": "tachyon:energy_crystal"}},
        "result": {"id": "tachyon:rare_earth_module", "count": 1},
    },
    "nether_siphon_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" B ", "BTB", " B "],
        "key": {"B": {"item": "minecraft:blaze_rod"}, "T": {"item": "tachyon:tachyon_alloy_ingot"}},
        "result": {"id": "tachyon:nether_siphon_module", "count": 1},
    },
    "end_siphon_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" E ", "ETE", " E "],
        "key": {"E": {"item": "minecraft:ender_pearl"}, "T": {"item": "tachyon:tachyon_alloy_ingot"}},
        "result": {"id": "tachyon:end_siphon_module", "count": 1},
    },
    "exotic_attunement_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" S ", "SQS", " S "],
        "key": {"S": {"item": "tachyon:exotic_shard"}, "Q": {"item": "tachyon:quantum_processor"}},
        "result": {"id": "tachyon:exotic_attunement_module", "count": 1},
    },
    "speed_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" S ", "STS", " S "],
        "key": {"S": {"item": "tachyon:speed_upgrade"}, "T": {"item": "tachyon:tachyon_alloy_ingot"}},
        "result": {"id": "tachyon:speed_module", "count": 1},
    },
    "yield_module": {
        "type": "minecraft:crafting_shaped",
        "pattern": [" O ", "OTO", " O "],
        "key": {"O": {"item": "tachyon:output_upgrade"}, "T": {"item": "tachyon:tachyon_alloy_ingot"}},
        "result": {"id": "tachyon:yield_module", "count": 1},
    },
}
for name, data in crafting.items():
    write_json(f"{recipe_dir}/{name}.json", data)

# ========== BLOCKSTATES ==========
bs_dir = f"{base}/assets/tachyon/blockstates"
blockstates = {
    "void_miner_controller": {"variants": {
        "active=false,formed=false": {"model": "tachyon:block/void_miner_controller"},
        "active=false,formed=true": {"model": "tachyon:block/void_miner_controller_formed"},
        "active=true,formed=false": {"model": "tachyon:block/void_miner_controller"},
        "active=true,formed=true": {"model": "tachyon:block/void_miner_controller_active"},
    }},
    "void_frame": {"variants": {"": {"model": "tachyon:block/void_frame"}}},
    "stabilized_void_frame": {"variants": {"": {"model": "tachyon:block/stabilized_void_frame"}}},
    "reinforced_void_frame": {"variants": {"": {"model": "tachyon:block/reinforced_void_frame"}}},
    "quantum_void_frame": {"variants": {"": {"model": "tachyon:block/quantum_void_frame"}}},
    "void_miner_port": {"variants": {
        "mode=catalyst_input": {"model": "tachyon:block/void_miner_port"},
        "mode=item_output": {"model": "tachyon:block/void_miner_port"},
        "mode=energy_input": {"model": "tachyon:block/void_miner_port"},
    }},
}
for name, data in blockstates.items():
    write_json(f"{bs_dir}/{name}.json", data)

# ========== BLOCK MODELS ==========
bm_dir = f"{base}/assets/tachyon/models/block"
block_models = {
    "void_miner_controller": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/void_miner_controller"}},
    "void_miner_controller_formed": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/void_miner_controller_formed"}},
    "void_miner_controller_active": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/void_miner_controller_active"}},
    "void_frame": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/void_frame"}},
    "stabilized_void_frame": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/stabilized_void_frame"}},
    "reinforced_void_frame": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/reinforced_void_frame"}},
    "quantum_void_frame": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/quantum_void_frame"}},
    "void_miner_port": {"parent": "minecraft:block/cube_all", "textures": {"all": "tachyon:block/void_miner_port"}},
}
for name, data in block_models.items():
    write_json(f"{bm_dir}/{name}.json", data)

# ========== ITEM MODELS ==========
im_dir = f"{base}/assets/tachyon/models/item"
# Block items
for name in ["void_miner_controller", "void_frame", "stabilized_void_frame", "reinforced_void_frame", "quantum_void_frame", "void_miner_port"]:
    write_json(f"{im_dir}/{name}.json", {"parent": f"tachyon:block/{name}"})
# Module items
for name in ["ore_extraction_module", "gem_resonance_module", "rare_earth_module", "nether_siphon_module", "end_siphon_module", "exotic_attunement_module", "speed_module", "yield_module"]:
    write_json(f"{im_dir}/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"tachyon:item/{name}"}})

# ========== LOOT TABLES ==========
lt_dir = f"{base}/data/tachyon/loot_table/blocks"
for name in ["void_miner_controller", "void_frame", "stabilized_void_frame", "reinforced_void_frame", "quantum_void_frame", "void_miner_port"]:
    write_json(f"{lt_dir}/{name}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"tachyon:{name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })

print(f"Created {len(drops)} void mining drop recipes")
print(f"Created {len(crafting)} crafting recipes")
print(f"Created {len(blockstates)} blockstates")
print(f"Created {len(block_models)} block models")
print(f"Created 14 item models")
print(f"Created 6 loot tables")
print("Done!")
