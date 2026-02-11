package com.setusertso.tachyon;

import net.minecraft.world.item.BlockItem;
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

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
