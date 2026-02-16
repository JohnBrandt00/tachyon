package com.setusertso.tachyon;

import com.setusertso.tachyon.block.AlloyForgeBlock;
import com.setusertso.tachyon.block.AcceleratorCasingBlock;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorGlassBlock;
import com.setusertso.tachyon.block.AcceleratorPortBlock;
import com.setusertso.tachyon.block.PhotonCompressorBlock;
import com.setusertso.tachyon.block.SolarCollectorBlock;
import com.setusertso.tachyon.block.OreCrusherBlock;
import com.setusertso.tachyon.block.CreativePowerBlock;
import com.setusertso.tachyon.block.CreativePowerSinkBlock;
import com.setusertso.tachyon.block.ExoticMatterCoreBlock;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.block.SingularityCasingBlock;
import com.setusertso.tachyon.block.SingularityControllerBlock;
import com.setusertso.tachyon.block.SingularityDebugBlock;
import com.setusertso.tachyon.block.SingularityPortBlock;
import com.setusertso.tachyon.block.SuperluminalEmitterBlock;
import com.setusertso.tachyon.block.TachyonLightGeneratorBlock;
import com.setusertso.tachyon.block.ThoriumReactorBlock;
import com.setusertso.tachyon.block.VoidFrameBlock;
import com.setusertso.tachyon.block.VoidMinerControllerBlock;
import com.setusertso.tachyon.block.VoidMinerPortBlock;
import com.setusertso.tachyon.init.ModFluids;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(tachyon.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(tachyon.MODID);

    // Titanium
    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.registerSimpleBlock("titanium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> TITANIUM_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("titanium_ore", TITANIUM_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.registerSimpleBlock("deepslate_titanium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DEEPSLATE_TITANIUM_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("deepslate_titanium_ore", DEEPSLATE_TITANIUM_ORE);

    public static final DeferredBlock<Block> TITANIUM_BLOCK = BLOCKS.registerSimpleBlock("titanium_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> TITANIUM_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("titanium_block", TITANIUM_BLOCK);

    // Tungsten
    public static final DeferredBlock<Block> TUNGSTEN_ORE = BLOCKS.registerSimpleBlock("tungsten_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> TUNGSTEN_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("tungsten_ore", TUNGSTEN_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_TUNGSTEN_ORE = BLOCKS.registerSimpleBlock("deepslate_tungsten_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(5.5F, 6.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DEEPSLATE_TUNGSTEN_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("deepslate_tungsten_ore", DEEPSLATE_TUNGSTEN_ORE);

    public static final DeferredBlock<Block> TUNGSTEN_BLOCK = BLOCKS.registerSimpleBlock("tungsten_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> TUNGSTEN_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("tungsten_block", TUNGSTEN_BLOCK);

    // Lithium
    public static final DeferredBlock<Block> LITHIUM_ORE = BLOCKS.registerSimpleBlock("lithium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5F, 2.5F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> LITHIUM_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("lithium_ore", LITHIUM_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_LITHIUM_ORE = BLOCKS.registerSimpleBlock("deepslate_lithium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(3.5F, 2.5F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DEEPSLATE_LITHIUM_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("deepslate_lithium_ore", DEEPSLATE_LITHIUM_ORE);

    public static final DeferredBlock<Block> LITHIUM_BLOCK = BLOCKS.registerSimpleBlock("lithium_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> LITHIUM_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("lithium_block", LITHIUM_BLOCK);

    // Thorium
    public static final DeferredBlock<Block> THORIUM_ORE = BLOCKS.registerSimpleBlock("thorium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0F, 3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> THORIUM_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("thorium_ore", THORIUM_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_THORIUM_ORE = BLOCKS.registerSimpleBlock("deepslate_thorium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(5.0F, 3.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DEEPSLATE_THORIUM_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("deepslate_thorium_ore", DEEPSLATE_THORIUM_ORE);

    public static final DeferredBlock<Block> THORIUM_BLOCK = BLOCKS.registerSimpleBlock("thorium_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> THORIUM_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("thorium_block", THORIUM_BLOCK);

    // Superluminal Light-Wave Emitter
    public static final DeferredBlock<SuperluminalEmitterBlock> SUPERLUMINAL_EMITTER = BLOCKS.register("superluminal_emitter",
            () -> new SuperluminalEmitterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> SUPERLUMINAL_EMITTER_ITEM = BLOCK_ITEMS.register("superluminal_emitter",
            () -> new BlockItem(SUPERLUMINAL_EMITTER.get(), new Item.Properties()));

    // Tachyon Light Generator
    public static final DeferredBlock<TachyonLightGeneratorBlock> TACHYON_LIGHT_GENERATOR = BLOCKS.register("tachyon_light_generator",
            () -> new TachyonLightGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5F, 3.5F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(TachyonLightGeneratorBlock.ACTIVE) ? 15 : 0)));
    public static final DeferredItem<BlockItem> TACHYON_LIGHT_GENERATOR_ITEM = BLOCK_ITEMS.register("tachyon_light_generator",
            () -> new BlockItem(TACHYON_LIGHT_GENERATOR.get(), new Item.Properties()));

    // Particle Accelerator
    public static final DeferredBlock<AcceleratorControllerBlock> ACCELERATOR_CONTROLLER = BLOCKS.register("accelerator_controller",
            () -> new AcceleratorControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ACCELERATOR_CONTROLLER_ITEM = BLOCK_ITEMS.register("accelerator_controller",
            () -> new BlockItem(ACCELERATOR_CONTROLLER.get(), new Item.Properties()));

    public static final DeferredBlock<AcceleratorCasingBlock> ACCELERATOR_CASING = BLOCKS.register("accelerator_casing",
            () -> new AcceleratorCasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ACCELERATOR_CASING_ITEM = BLOCK_ITEMS.register("accelerator_casing",
            () -> new BlockItem(ACCELERATOR_CASING.get(), new Item.Properties()));

    public static final DeferredBlock<AcceleratorPortBlock> ACCELERATOR_PORT = BLOCKS.register("accelerator_port",
            () -> new AcceleratorPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ACCELERATOR_PORT_ITEM = BLOCK_ITEMS.register("accelerator_port",
            () -> new BlockItem(ACCELERATOR_PORT.get(), new Item.Properties()));

    public static final DeferredBlock<AcceleratorGlassBlock> ACCELERATOR_GLASS = BLOCKS.register("accelerator_glass",
            () -> new AcceleratorGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).strength(3.0F, 6.0F).sound(SoundType.GLASS)
                    .noOcclusion().isValidSpawn((s, l, p, e) -> false)
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ACCELERATOR_GLASS_ITEM = BLOCK_ITEMS.register("accelerator_glass",
            () -> new BlockItem(ACCELERATOR_GLASS.get(), new Item.Properties()));

    // Helium fluid block
    public static final DeferredBlock<LiquidBlock> HELIUM_BLOCK = BLOCKS.register("helium",
            () -> new LiquidBlock(ModFluids.HELIUM_SOURCE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).replaceable().noCollission()
                    .randomTicks().strength(-1.0F).liquid().noLootTable()));

    // Singularity Debug (visual test block)
    public static final DeferredBlock<SingularityDebugBlock> SINGULARITY_DEBUG = BLOCKS.register("singularity_debug",
            () -> new SingularityDebugBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(3.0F, 6.0F).sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> SINGULARITY_DEBUG_ITEM = BLOCK_ITEMS.register("singularity_debug",
            () -> new BlockItem(SINGULARITY_DEBUG.get(), new Item.Properties()));

    // Singularity Engine
    public static final DeferredBlock<SingularityControllerBlock> SINGULARITY_CONTROLLER = BLOCKS.register("singularity_controller",
            () -> new SingularityControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> SINGULARITY_CONTROLLER_ITEM = BLOCK_ITEMS.register("singularity_controller",
            () -> new BlockItem(SINGULARITY_CONTROLLER.get(), new Item.Properties()));

    public static final DeferredBlock<SingularityCasingBlock> SINGULARITY_CASING = BLOCKS.register("singularity_casing",
            () -> new SingularityCasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> SINGULARITY_CASING_ITEM = BLOCK_ITEMS.register("singularity_casing",
            () -> new BlockItem(SINGULARITY_CASING.get(), new Item.Properties()));

    public static final DeferredBlock<SingularityPortBlock> SINGULARITY_PORT = BLOCKS.register("singularity_port",
            () -> new SingularityPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> SINGULARITY_PORT_ITEM = BLOCK_ITEMS.register("singularity_port",
            () -> new BlockItem(SINGULARITY_PORT.get(), new Item.Properties()));

    public static final DeferredBlock<ExoticMatterCoreBlock> EXOTIC_MATTER_CORE = BLOCKS.register("exotic_matter_core",
            () -> new ExoticMatterCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)));
    public static final DeferredItem<BlockItem> EXOTIC_MATTER_CORE_ITEM = BLOCK_ITEMS.register("exotic_matter_core",
            () -> new BlockItem(EXOTIC_MATTER_CORE.get(), new Item.Properties()));

    public static final DeferredBlock<PhotonicInjectorBlock> PHOTONIC_INJECTOR = BLOCKS.register("photonic_injector",
            () -> new PhotonicInjectorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(PhotonicInjectorBlock.ACTIVE) ? 12 : 0)));
    public static final DeferredItem<BlockItem> PHOTONIC_INJECTOR_ITEM = BLOCK_ITEMS.register("photonic_injector",
            () -> new BlockItem(PHOTONIC_INJECTOR.get(), new Item.Properties()));

    // Thorium Reactor
    public static final DeferredBlock<ThoriumReactorBlock> THORIUM_REACTOR = BLOCKS.register("thorium_reactor",
            () -> new ThoriumReactorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(ThoriumReactorBlock.BURNING) ? 13 : 0)));
    public static final DeferredItem<BlockItem> THORIUM_REACTOR_ITEM = BLOCK_ITEMS.register("thorium_reactor",
            () -> new BlockItem(THORIUM_REACTOR.get(), new Item.Properties()));

    // Solar Collector
    public static final DeferredBlock<SolarCollectorBlock> SOLAR_COLLECTOR = BLOCKS.register("solar_collector",
            () -> new SolarCollectorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(SolarCollectorBlock.ACTIVE) ? 8 : 0)));
    public static final DeferredItem<BlockItem> SOLAR_COLLECTOR_ITEM = BLOCK_ITEMS.register("solar_collector",
            () -> new BlockItem(SOLAR_COLLECTOR.get(), new Item.Properties()));

    // Photon Compressor
    public static final DeferredBlock<PhotonCompressorBlock> PHOTON_COMPRESSOR = BLOCKS.register("photon_compressor",
            () -> new PhotonCompressorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(PhotonCompressorBlock.ACTIVE) ? 12 : 0)));
    public static final DeferredItem<BlockItem> PHOTON_COMPRESSOR_ITEM = BLOCK_ITEMS.register("photon_compressor",
            () -> new BlockItem(PHOTON_COMPRESSOR.get(), new Item.Properties()));

    // Ore Crusher
    public static final DeferredBlock<OreCrusherBlock> ORE_CRUSHER = BLOCKS.register("ore_crusher",
            () -> new OreCrusherBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(4.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(OreCrusherBlock.ACTIVE) ? 4 : 0)));
    public static final DeferredItem<BlockItem> ORE_CRUSHER_ITEM = BLOCK_ITEMS.register("ore_crusher",
            () -> new BlockItem(ORE_CRUSHER.get(), new Item.Properties()));

    // Alloy Forge
    public static final DeferredBlock<AlloyForgeBlock> ALLOY_FORGE = BLOCKS.register("alloy_forge",
            () -> new AlloyForgeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(AlloyForgeBlock.ACTIVE) ? 10 : 0)));
    public static final DeferredItem<BlockItem> ALLOY_FORGE_ITEM = BLOCK_ITEMS.register("alloy_forge",
            () -> new BlockItem(ALLOY_FORGE.get(), new Item.Properties()));

    // Void Miner
    public static final DeferredBlock<VoidMinerControllerBlock> VOID_MINER_CONTROLLER = BLOCKS.register("void_miner_controller",
            () -> new VoidMinerControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> VOID_MINER_CONTROLLER_ITEM = BLOCK_ITEMS.register("void_miner_controller",
            () -> new BlockItem(VOID_MINER_CONTROLLER.get(), new Item.Properties()));

    public static final DeferredBlock<VoidFrameBlock> VOID_FRAME = BLOCKS.register("void_frame",
            () -> new VoidFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 1));
    public static final DeferredItem<BlockItem> VOID_FRAME_ITEM = BLOCK_ITEMS.register("void_frame",
            () -> new BlockItem(VOID_FRAME.get(), new Item.Properties()));

    public static final DeferredBlock<VoidFrameBlock> STABILIZED_VOID_FRAME = BLOCKS.register("stabilized_void_frame",
            () -> new VoidFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 2));
    public static final DeferredItem<BlockItem> STABILIZED_VOID_FRAME_ITEM = BLOCK_ITEMS.register("stabilized_void_frame",
            () -> new BlockItem(STABILIZED_VOID_FRAME.get(), new Item.Properties()));

    public static final DeferredBlock<VoidFrameBlock> REINFORCED_VOID_FRAME = BLOCKS.register("reinforced_void_frame",
            () -> new VoidFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(6.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 3));
    public static final DeferredItem<BlockItem> REINFORCED_VOID_FRAME_ITEM = BLOCK_ITEMS.register("reinforced_void_frame",
            () -> new BlockItem(REINFORCED_VOID_FRAME.get(), new Item.Properties()));

    public static final DeferredBlock<VoidFrameBlock> QUANTUM_VOID_FRAME = BLOCKS.register("quantum_void_frame",
            () -> new VoidFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(6.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), 4));
    public static final DeferredItem<BlockItem> QUANTUM_VOID_FRAME_ITEM = BLOCK_ITEMS.register("quantum_void_frame",
            () -> new BlockItem(QUANTUM_VOID_FRAME.get(), new Item.Properties()));

    public static final DeferredBlock<VoidMinerPortBlock> VOID_MINER_PORT = BLOCKS.register("void_miner_port",
            () -> new VoidMinerPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> VOID_MINER_PORT_ITEM = BLOCK_ITEMS.register("void_miner_port",
            () -> new BlockItem(VOID_MINER_PORT.get(), new Item.Properties()));

    // Creative Power Source (test block)
    public static final DeferredBlock<CreativePowerBlock> CREATIVE_POWER_SOURCE = BLOCKS.register("creative_power_source",
            () -> new CreativePowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 6.0F).sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> CREATIVE_POWER_SOURCE_ITEM = BLOCK_ITEMS.register("creative_power_source",
            () -> new BlockItem(CREATIVE_POWER_SOURCE.get(), new Item.Properties()));

    // Creative Power Sink (test block)
    public static final DeferredBlock<CreativePowerSinkBlock> CREATIVE_POWER_SINK = BLOCKS.register("creative_power_sink",
            () -> new CreativePowerSinkBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 6.0F).sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> CREATIVE_POWER_SINK_ITEM = BLOCK_ITEMS.register("creative_power_sink",
            () -> new BlockItem(CREATIVE_POWER_SINK.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
