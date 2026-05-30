package com.setusertso.tachyon;

import com.setusertso.tachyon.init.ModArmorMaterials;
import com.setusertso.tachyon.init.ModFluids;
import com.setusertso.tachyon.item.ModToolTier;
import com.setusertso.tachyon.item.TachyonArmorItem;
import com.setusertso.tachyon.item.TachyonAxeItem;
import com.setusertso.tachyon.item.TachyonPickaxeItem;
import com.setusertso.tachyon.item.TachyonShovelItem;
import com.setusertso.tachyon.item.TachyonLinkerItem;
import com.setusertso.tachyon.item.TachyonWrenchItem;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(tachyon.MODID);

    // Titanium
    public static final DeferredItem<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium");
    public static final DeferredItem<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot");

    // Tungsten
    public static final DeferredItem<Item> RAW_TUNGSTEN = ITEMS.registerSimpleItem("raw_tungsten");
    public static final DeferredItem<Item> TUNGSTEN_INGOT = ITEMS.registerSimpleItem("tungsten_ingot");

    // Lithium
    public static final DeferredItem<Item> RAW_LITHIUM = ITEMS.registerSimpleItem("raw_lithium");
    public static final DeferredItem<Item> LITHIUM_INGOT = ITEMS.registerSimpleItem("lithium_ingot");

    // Thorium
    public static final DeferredItem<Item> RAW_THORIUM = ITEMS.registerSimpleItem("raw_thorium");
    public static final DeferredItem<Item> THORIUM_INGOT = ITEMS.registerSimpleItem("thorium_ingot");

    // Tachyon Shard (fuel for Superluminal Emitter)
    public static final DeferredItem<Item> TACHYON_SHARD = ITEMS.registerSimpleItem("tachyon_shard");

    // Helium Bucket
    public static final DeferredItem<BucketItem> HELIUM_BUCKET = ITEMS.register("helium_bucket",
            () -> new BucketItem(ModFluids.HELIUM_SOURCE.get(), new Item.Properties().stacksTo(1)));

    // Tachyon Flux Bucket
    public static final DeferredItem<BucketItem> TACHYON_FLUX_BUCKET = ITEMS.register("tachyon_flux_bucket",
            () -> new BucketItem(ModFluids.TACHYON_FLUX_SOURCE.get(), new Item.Properties().stacksTo(1)));

    // Singularity Engine
    public static final DeferredItem<Item> PHOTONIC_MATRIX = ITEMS.registerSimpleItem("photonic_matrix");
    public static final DeferredItem<Item> CONDENSED_LIGHT = ITEMS.registerSimpleItem("condensed_light");
    public static final DeferredItem<Item> EXOTIC_MATTER = ITEMS.registerSimpleItem("exotic_matter");
    public static final DeferredItem<Item> ANTIMATTER_NEUTRALIZER = ITEMS.registerSimpleItem("antimatter_neutralizer");

    // Alloy Forge Materials
    public static final DeferredItem<Item> TACHYON_ALLOY_INGOT = ITEMS.registerSimpleItem("tachyon_alloy_ingot");
    public static final DeferredItem<Item> REACTOR_PLATING = ITEMS.registerSimpleItem("reactor_plating");
    public static final DeferredItem<Item> ENERGY_CRYSTAL = ITEMS.registerSimpleItem("energy_crystal");
    public static final DeferredItem<Item> EXOTIC_SHARD = ITEMS.registerSimpleItem("exotic_shard");

    // Crushed Ores (from Ore Crusher)
    public static final DeferredItem<Item> CRUSHED_TITANIUM = ITEMS.registerSimpleItem("crushed_titanium");
    public static final DeferredItem<Item> CRUSHED_TUNGSTEN = ITEMS.registerSimpleItem("crushed_tungsten");
    public static final DeferredItem<Item> CRUSHED_LITHIUM = ITEMS.registerSimpleItem("crushed_lithium");
    public static final DeferredItem<Item> CRUSHED_THORIUM = ITEMS.registerSimpleItem("crushed_thorium");
    public static final DeferredItem<Item> CRUSHED_IRON = ITEMS.registerSimpleItem("crushed_iron");
    public static final DeferredItem<Item> CRUSHED_GOLD = ITEMS.registerSimpleItem("crushed_gold");
    public static final DeferredItem<Item> CRUSHED_COPPER = ITEMS.registerSimpleItem("crushed_copper");

    // Advanced Components
    public static final DeferredItem<Item> TACHYON_CORE = ITEMS.register("tachyon_core",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> QUANTUM_PROCESSOR = ITEMS.register("quantum_processor",
            () -> new Item(new Item.Properties().stacksTo(16)));

    // Condensed Light Pipeline
    public static final DeferredItem<Item> RAW_PHOTON = ITEMS.registerSimpleItem("raw_photon");
    public static final DeferredItem<Item> EXCITED_PHOTON = ITEMS.registerSimpleItem("excited_photon");

    // Graviton Crystal
    public static final DeferredItem<Item> GRAVITON_CRYSTAL = ITEMS.register("graviton_crystal",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    // Machine Upgrades
    public static final DeferredItem<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final DeferredItem<Item> ENERGY_UPGRADE = ITEMS.register("energy_upgrade",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final DeferredItem<Item> OUTPUT_UPGRADE = ITEMS.register("output_upgrade",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final DeferredItem<Item> CAPACITY_UPGRADE = ITEMS.register("capacity_upgrade",
            () -> new Item(new Item.Properties().stacksTo(32)));

    // Void Miner Modules
    public static final DeferredItem<Item> ORE_EXTRACTION_MODULE = ITEMS.register("ore_extraction_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> SILK_TOUCH_MODULE = ITEMS.register("silk_touch_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> RARE_EARTH_MODULE = ITEMS.register("rare_earth_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> NETHER_SIPHON_MODULE = ITEMS.register("nether_siphon_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> END_SIPHON_MODULE = ITEMS.register("end_siphon_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> EXOTIC_ATTUNEMENT_MODULE = ITEMS.register("exotic_attunement_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> SPEED_MODULE = ITEMS.register("speed_module",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> FORTUNE_MODULE = ITEMS.register("fortune_module",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // Tachyon Wrench
    public static final DeferredItem<Item> TACHYON_WRENCH = ITEMS.register("tachyon_wrench",
            () -> new TachyonWrenchItem(new Item.Properties().stacksTo(1)));

    // Tachyon Linker (relay pairing tool)
    public static final DeferredItem<Item> TACHYON_LINKER = ITEMS.register("tachyon_linker",
            () -> new TachyonLinkerItem(new Item.Properties().stacksTo(1)));

    // Tachyon Tools
    public static final DeferredItem<Item> TACHYON_PICKAXE = ITEMS.register("tachyon_pickaxe",
            () -> new TachyonPickaxeItem(new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModToolTier.TACHYON_ALLOY, 1.0f, -2.8f))));
    public static final DeferredItem<Item> TACHYON_SWORD = ITEMS.register("tachyon_sword",
            () -> new SwordItem(ModToolTier.TACHYON_ALLOY, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTier.TACHYON_ALLOY, 3, -1.7f))));
    public static final DeferredItem<Item> TACHYON_AXE = ITEMS.register("tachyon_axe",
            () -> new TachyonAxeItem(new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModToolTier.TACHYON_ALLOY, 5.0f, -3.0f))));
    public static final DeferredItem<Item> TACHYON_SHOVEL = ITEMS.register("tachyon_shovel",
            () -> new TachyonShovelItem(new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModToolTier.TACHYON_ALLOY, 1.5f, -3.0f))));

    // Tachyon Armor
    public static final DeferredItem<ArmorItem> TACHYON_HELMET = ITEMS.register("tachyon_helmet",
            () -> new TachyonArmorItem(ModArmorMaterials.TACHYON_ALLOY, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(41))));
    public static final DeferredItem<ArmorItem> TACHYON_CHESTPLATE = ITEMS.register("tachyon_chestplate",
            () -> new TachyonArmorItem(ModArmorMaterials.TACHYON_ALLOY, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(41))));
    public static final DeferredItem<ArmorItem> TACHYON_LEGGINGS = ITEMS.register("tachyon_leggings",
            () -> new TachyonArmorItem(ModArmorMaterials.TACHYON_ALLOY, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(40))));
    public static final DeferredItem<ArmorItem> TACHYON_BOOTS = ITEMS.register("tachyon_boots",
            () -> new TachyonArmorItem(ModArmorMaterials.TACHYON_ALLOY, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(38))));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
