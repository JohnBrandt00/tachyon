package com.setusertso.tachyon.init;

import java.util.function.Supplier;

import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.tachyon;
import com.setusertso.tachyon.block.entity.AcceleratorCasingBlockEntity;
import com.setusertso.tachyon.block.entity.AcceleratorControllerBlockEntity;
import com.setusertso.tachyon.block.entity.AcceleratorPortBlockEntity;
import com.setusertso.tachyon.block.entity.CreativePowerBlockEntity;
import com.setusertso.tachyon.block.entity.ExoticMatterCoreBlockEntity;
import com.setusertso.tachyon.block.entity.PhotonicInjectorBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityCasingBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityDebugBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityPortBlockEntity;
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

    public static final Supplier<BlockEntityType<AcceleratorControllerBlockEntity>> ACCELERATOR_CONTROLLER =
            BLOCK_ENTITIES.register("accelerator_controller",
                    () -> BlockEntityType.Builder.of(AcceleratorControllerBlockEntity::new,
                            ModBlocks.ACCELERATOR_CONTROLLER.get()).build(null));

    public static final Supplier<BlockEntityType<AcceleratorCasingBlockEntity>> ACCELERATOR_CASING =
            BLOCK_ENTITIES.register("accelerator_casing",
                    () -> BlockEntityType.Builder.of(AcceleratorCasingBlockEntity::new,
                            ModBlocks.ACCELERATOR_CASING.get(), ModBlocks.ACCELERATOR_GLASS.get()).build(null));

    public static final Supplier<BlockEntityType<AcceleratorPortBlockEntity>> ACCELERATOR_PORT =
            BLOCK_ENTITIES.register("accelerator_port",
                    () -> BlockEntityType.Builder.of(AcceleratorPortBlockEntity::new,
                            ModBlocks.ACCELERATOR_PORT.get()).build(null));

    public static final Supplier<BlockEntityType<CreativePowerBlockEntity>> CREATIVE_POWER_SOURCE =
            BLOCK_ENTITIES.register("creative_power_source",
                    () -> BlockEntityType.Builder.of(CreativePowerBlockEntity::new,
                            ModBlocks.CREATIVE_POWER_SOURCE.get()).build(null));

    public static final Supplier<BlockEntityType<SingularityControllerBlockEntity>> SINGULARITY_CONTROLLER =
            BLOCK_ENTITIES.register("singularity_controller",
                    () -> BlockEntityType.Builder.of(SingularityControllerBlockEntity::new,
                            ModBlocks.SINGULARITY_CONTROLLER.get()).build(null));

    public static final Supplier<BlockEntityType<SingularityCasingBlockEntity>> SINGULARITY_CASING =
            BLOCK_ENTITIES.register("singularity_casing",
                    () -> BlockEntityType.Builder.of(SingularityCasingBlockEntity::new,
                            ModBlocks.SINGULARITY_CASING.get()).build(null));

    public static final Supplier<BlockEntityType<SingularityPortBlockEntity>> SINGULARITY_PORT =
            BLOCK_ENTITIES.register("singularity_port",
                    () -> BlockEntityType.Builder.of(SingularityPortBlockEntity::new,
                            ModBlocks.SINGULARITY_PORT.get()).build(null));

    public static final Supplier<BlockEntityType<ExoticMatterCoreBlockEntity>> EXOTIC_MATTER_CORE =
            BLOCK_ENTITIES.register("exotic_matter_core",
                    () -> BlockEntityType.Builder.of(ExoticMatterCoreBlockEntity::new,
                            ModBlocks.EXOTIC_MATTER_CORE.get()).build(null));

    public static final Supplier<BlockEntityType<PhotonicInjectorBlockEntity>> PHOTONIC_INJECTOR =
            BLOCK_ENTITIES.register("photonic_injector",
                    () -> BlockEntityType.Builder.of(PhotonicInjectorBlockEntity::new,
                            ModBlocks.PHOTONIC_INJECTOR.get()).build(null));

    public static final Supplier<BlockEntityType<SingularityDebugBlockEntity>> SINGULARITY_DEBUG =
            BLOCK_ENTITIES.register("singularity_debug",
                    () -> BlockEntityType.Builder.of(SingularityDebugBlockEntity::new,
                            ModBlocks.SINGULARITY_DEBUG.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
