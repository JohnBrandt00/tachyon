package com.setusertso.tachyon;

import com.setusertso.tachyon.init.ModFluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
