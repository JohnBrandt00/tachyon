package com.setusertso.tachyon.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> GLASS_BLOCKS = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("c", "glass_blocks"));
    }

    public static class Items {
        public static final TagKey<Item> INGOTS_THORIUM = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "ingots/thorium"));
    }
}
