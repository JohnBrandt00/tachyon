package com.setusertso.tachyon.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.setusertso.tachyon.init.ModRecipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Data-driven void mining recipe. Each JSON file defines one possible drop
 * with a weight, tier requirement, category, and count range.
 * Optional silk_touch_item: if present and Silk Touch module is installed,
 * produces this item instead of the normal result.
 */
public record VoidMiningRecipe(
        Item resultItem,
        int resultCount,
        int weight,
        int minCount,
        int maxCount,
        int requiredTier,
        String category,
        Item silkTouchItem
) implements Recipe<RecipeInput> {

    public boolean hasSilkTouchAlternative() {
        return silkTouchItem != null && silkTouchItem != Items.AIR;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false; // Not used for standard crafting
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return new ItemStack(resultItem, resultCount);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(resultItem, resultCount);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.VOID_MINING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.VOID_MINING_TYPE.get();
    }

    // --- Serializer ---

    public static class Serializer implements RecipeSerializer<VoidMiningRecipe> {

        public static final MapCodec<VoidMiningRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("result_item").forGetter(r ->
                                BuiltInRegistries.ITEM.getKey(r.resultItem())),
                        Codec.INT.optionalFieldOf("result_count", 1).forGetter(VoidMiningRecipe::resultCount),
                        Codec.INT.fieldOf("weight").forGetter(VoidMiningRecipe::weight),
                        Codec.INT.optionalFieldOf("min_count", 1).forGetter(VoidMiningRecipe::minCount),
                        Codec.INT.optionalFieldOf("max_count", 1).forGetter(VoidMiningRecipe::maxCount),
                        Codec.INT.optionalFieldOf("tier", 1).forGetter(VoidMiningRecipe::requiredTier),
                        Codec.STRING.optionalFieldOf("category", "common_ore").forGetter(VoidMiningRecipe::category),
                        ResourceLocation.CODEC.optionalFieldOf("silk_touch_item").forGetter(r ->
                                r.hasSilkTouchAlternative()
                                        ? Optional.of(BuiltInRegistries.ITEM.getKey(r.silkTouchItem()))
                                        : Optional.empty())
                ).apply(instance, (resultId, resultCount, weight, minCount, maxCount, tier, category, silkTouchId) -> {
                    Item item = BuiltInRegistries.ITEM.get(resultId);
                    Item silkItem = silkTouchId.map(id -> BuiltInRegistries.ITEM.get(id)).orElse(Items.AIR);
                    return new VoidMiningRecipe(item, resultCount, weight, minCount, maxCount, tier, category, silkItem);
                })
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, VoidMiningRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        private static VoidMiningRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation itemId = buf.readResourceLocation();
            int resultCount = buf.readVarInt();
            int weight = buf.readVarInt();
            int minCount = buf.readVarInt();
            int maxCount = buf.readVarInt();
            int tier = buf.readVarInt();
            String category = buf.readUtf();
            boolean hasSilk = buf.readBoolean();
            Item silkItem = Items.AIR;
            if (hasSilk) {
                silkItem = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
            }
            Item item = BuiltInRegistries.ITEM.get(itemId);
            return new VoidMiningRecipe(item, resultCount, weight, minCount, maxCount, tier, category, silkItem);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, VoidMiningRecipe recipe) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.resultItem()));
            buf.writeVarInt(recipe.resultCount());
            buf.writeVarInt(recipe.weight());
            buf.writeVarInt(recipe.minCount());
            buf.writeVarInt(recipe.maxCount());
            buf.writeVarInt(recipe.requiredTier());
            buf.writeUtf(recipe.category());
            buf.writeBoolean(recipe.hasSilkTouchAlternative());
            if (recipe.hasSilkTouchAlternative()) {
                buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.silkTouchItem()));
            }
        }

        @Override
        public MapCodec<VoidMiningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VoidMiningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
