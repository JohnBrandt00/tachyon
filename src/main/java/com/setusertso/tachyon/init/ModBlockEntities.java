package com.setusertso.tachyon.init;

import java.util.function.Supplier;

import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.tachyon;
import com.setusertso.tachyon.block.entity.SuperluminalEmitterBlockEntity;
import com.setusertso.tachyon.block.entity.TachyonLightGeneratorBlockEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, tachyon.MODID);

    public static final Supplier<BlockEntityType<SuperluminalEmitterBlockEntity>> SUPERLUMINAL_EMITTER =
            BLOCK_ENTITIES.register("superluminal_emitter",
                    () -> BlockEntityType.Builder.of(SuperluminalEmitterBlockEntity::new,
                            ModBlocks.SUPERLUMINAL_EMITTER.get()).build(null));

    public static final Supplier<BlockEntityType<TachyonLightGeneratorBlockEntity>> TACHYON_LIGHT_GENERATOR =
            BLOCK_ENTITIES.register("tachyon_light_generator",
                    () -> BlockEntityType.Builder.of(TachyonLightGeneratorBlockEntity::new,
                            ModBlocks.TACHYON_LIGHT_GENERATOR.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
