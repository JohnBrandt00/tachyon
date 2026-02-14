package com.setusertso.tachyon;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.setusertso.tachyon.command.ModCommands;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModCapabilities;
import com.setusertso.tachyon.init.ModFluids;
import com.setusertso.tachyon.init.ModMenuTypes;
import com.setusertso.tachyon.network.ModNetworking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(tachyon.MODID)
public class tachyon {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tachyon";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "tachyon" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "tachyon" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "tachyon" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "tachyon:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "tachyon:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new Block with the id "tachyon:tachyon_metal_block", with iron-like properties
    public static final DeferredBlock<Block> TACHYON_METAL_BLOCK = BLOCKS.registerSimpleBlock("tachyon_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> TACHYON_METAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("tachyon_metal_block", TACHYON_METAL_BLOCK);

    // Creates a new food item with the id "tachyon:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "tachyon:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("tachyon", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tachyon")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
                output.accept(TACHYON_METAL_BLOCK_ITEM.get());
                // Titanium
                output.accept(ModBlocks.TITANIUM_ORE_ITEM.get());
                output.accept(ModBlocks.DEEPSLATE_TITANIUM_ORE_ITEM.get());
                output.accept(ModBlocks.TITANIUM_BLOCK_ITEM.get());
                output.accept(ModItems.RAW_TITANIUM.get());
                output.accept(ModItems.TITANIUM_INGOT.get());
                // Tungsten
                output.accept(ModBlocks.TUNGSTEN_ORE_ITEM.get());
                output.accept(ModBlocks.DEEPSLATE_TUNGSTEN_ORE_ITEM.get());
                output.accept(ModBlocks.TUNGSTEN_BLOCK_ITEM.get());
                output.accept(ModItems.RAW_TUNGSTEN.get());
                output.accept(ModItems.TUNGSTEN_INGOT.get());
                // Lithium
                output.accept(ModBlocks.LITHIUM_ORE_ITEM.get());
                output.accept(ModBlocks.DEEPSLATE_LITHIUM_ORE_ITEM.get());
                output.accept(ModBlocks.LITHIUM_BLOCK_ITEM.get());
                output.accept(ModItems.RAW_LITHIUM.get());
                output.accept(ModItems.LITHIUM_INGOT.get());
                // Thorium
                output.accept(ModBlocks.THORIUM_ORE_ITEM.get());
                output.accept(ModBlocks.DEEPSLATE_THORIUM_ORE_ITEM.get());
                output.accept(ModBlocks.THORIUM_BLOCK_ITEM.get());
                output.accept(ModItems.RAW_THORIUM.get());
                output.accept(ModItems.THORIUM_INGOT.get());
                // Superluminal Emitter system
                output.accept(ModBlocks.SUPERLUMINAL_EMITTER_ITEM.get());
                output.accept(ModBlocks.TACHYON_LIGHT_GENERATOR_ITEM.get());
                output.accept(ModItems.TACHYON_SHARD.get());
                // Particle Accelerator
                output.accept(ModBlocks.ACCELERATOR_CONTROLLER_ITEM.get());
                output.accept(ModBlocks.ACCELERATOR_CASING_ITEM.get());
                output.accept(ModBlocks.ACCELERATOR_PORT_ITEM.get());
                output.accept(ModBlocks.ACCELERATOR_GLASS_ITEM.get());
                // Fluids
                output.accept(ModItems.HELIUM_BUCKET.get());
                // Creative / Testing
                output.accept(ModBlocks.CREATIVE_POWER_SOURCE_ITEM.get());
                output.accept(ModBlocks.SINGULARITY_DEBUG_ITEM.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public tachyon(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ore blocks and items
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);

        // Register fluids
        ModFluids.register(modEventBus);

        // Register block entities and menu types
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (tachyon) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register capabilities and networking
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(ModNetworking::register);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
            event.accept(TACHYON_METAL_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
