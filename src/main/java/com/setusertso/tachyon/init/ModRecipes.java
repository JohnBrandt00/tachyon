package com.setusertso.tachyon.init;

import com.setusertso.tachyon.recipe.VoidMiningRecipe;
import com.setusertso.tachyon.tachyon;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, tachyon.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, tachyon.MODID);

    @SuppressWarnings("unchecked")
    public static final Supplier<RecipeType<VoidMiningRecipe>> VOID_MINING_TYPE =
            (Supplier<RecipeType<VoidMiningRecipe>>) (Supplier<?>) RECIPE_TYPES.register("void_mining",
                    () -> new RecipeType<VoidMiningRecipe>() {
                        @Override
                        public String toString() {
                            return "tachyon:void_mining";
                        }
                    });

    public static final Supplier<RecipeSerializer<VoidMiningRecipe>> VOID_MINING_SERIALIZER =
            RECIPE_SERIALIZERS.register("void_mining", VoidMiningRecipe.Serializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
