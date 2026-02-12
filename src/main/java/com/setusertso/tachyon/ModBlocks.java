package com.setusertso.tachyon;

import com.setusertso.tachyon.block.AcceleratorCasingBlock;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorGlassBlock;
import com.setusertso.tachyon.block.AcceleratorPortBlock;
import com.setusertso.tachyon.block.SuperluminalEmitterBlock;
import com.setusertso.tachyon.block.TachyonLightGeneratorBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
