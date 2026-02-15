package com.setusertso.tachyon;

import com.setusertso.tachyon.init.ModFluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
