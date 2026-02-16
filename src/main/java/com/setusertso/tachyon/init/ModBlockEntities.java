package com.setusertso.tachyon.init;

import java.util.function.Supplier;

import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.tachyon;
import com.setusertso.tachyon.block.entity.AcceleratorCasingBlockEntity;
import com.setusertso.tachyon.block.entity.AcceleratorControllerBlockEntity;
import com.setusertso.tachyon.block.entity.AcceleratorPortBlockEntity;
import com.setusertso.tachyon.block.entity.CreativePowerBlockEntity;
import com.setusertso.tachyon.block.entity.CreativePowerSinkBlockEntity;
import com.setusertso.tachyon.block.entity.ExoticMatterCoreBlockEntity;
import com.setusertso.tachyon.block.entity.PhotonicInjectorBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityCasingBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityDebugBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityPortBlockEntity;
import com.setusertso.tachyon.block.entity.SuperluminalEmitterBlockEntity;
import com.setusertso.tachyon.block.entity.TachyonLightGeneratorBlockEntity;
import com.setusertso.tachyon.block.entity.ThoriumReactorBlockEntity;
import com.setusertso.tachyon.block.entity.SolarCollectorBlockEntity;
import com.setusertso.tachyon.block.entity.PhotonCompressorBlockEntity;
import com.setusertso.tachyon.block.entity.OreCrusherBlockEntity;
import com.setusertso.tachyon.block.entity.AlloyForgeBlockEntity;
import com.setusertso.tachyon.block.entity.VoidMinerControllerBlockEntity;
import com.setusertso.tachyon.block.entity.VoidFrameBlockEntity;
import com.setusertso.tachyon.block.entity.VoidMinerPortBlockEntity;

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

    public static final Supplier<BlockEntityType<CreativePowerSinkBlockEntity>> CREATIVE_POWER_SINK =
            BLOCK_ENTITIES.register("creative_power_sink",
                    () -> BlockEntityType.Builder.of(CreativePowerSinkBlockEntity::new,
                            ModBlocks.CREATIVE_POWER_SINK.get()).build(null));

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

    public static final Supplier<BlockEntityType<ThoriumReactorBlockEntity>> THORIUM_REACTOR =
            BLOCK_ENTITIES.register("thorium_reactor",
                    () -> BlockEntityType.Builder.of(ThoriumReactorBlockEntity::new,
                            ModBlocks.THORIUM_REACTOR.get()).build(null));

    public static final Supplier<BlockEntityType<SolarCollectorBlockEntity>> SOLAR_COLLECTOR =
            BLOCK_ENTITIES.register("solar_collector",
                    () -> BlockEntityType.Builder.of(SolarCollectorBlockEntity::new,
                            ModBlocks.SOLAR_COLLECTOR.get()).build(null));

    public static final Supplier<BlockEntityType<PhotonCompressorBlockEntity>> PHOTON_COMPRESSOR =
            BLOCK_ENTITIES.register("photon_compressor",
                    () -> BlockEntityType.Builder.of(PhotonCompressorBlockEntity::new,
                            ModBlocks.PHOTON_COMPRESSOR.get()).build(null));

    public static final Supplier<BlockEntityType<OreCrusherBlockEntity>> ORE_CRUSHER =
            BLOCK_ENTITIES.register("ore_crusher",
                    () -> BlockEntityType.Builder.of(OreCrusherBlockEntity::new,
                            ModBlocks.ORE_CRUSHER.get()).build(null));

    public static final Supplier<BlockEntityType<AlloyForgeBlockEntity>> ALLOY_FORGE =
            BLOCK_ENTITIES.register("alloy_forge",
                    () -> BlockEntityType.Builder.of(AlloyForgeBlockEntity::new,
                            ModBlocks.ALLOY_FORGE.get()).build(null));

    // Void Miner
    public static final Supplier<BlockEntityType<VoidMinerControllerBlockEntity>> VOID_MINER_CONTROLLER =
            BLOCK_ENTITIES.register("void_miner_controller",
                    () -> BlockEntityType.Builder.of(VoidMinerControllerBlockEntity::new,
                            ModBlocks.VOID_MINER_CONTROLLER.get()).build(null));

    public static final Supplier<BlockEntityType<VoidFrameBlockEntity>> VOID_FRAME =
            BLOCK_ENTITIES.register("void_frame",
                    () -> BlockEntityType.Builder.of(VoidFrameBlockEntity::new,
                            ModBlocks.VOID_FRAME.get(), ModBlocks.STABILIZED_VOID_FRAME.get(),
                            ModBlocks.REINFORCED_VOID_FRAME.get(), ModBlocks.QUANTUM_VOID_FRAME.get()).build(null));

    public static final Supplier<BlockEntityType<VoidMinerPortBlockEntity>> VOID_MINER_PORT =
            BLOCK_ENTITIES.register("void_miner_port",
                    () -> BlockEntityType.Builder.of(VoidMinerPortBlockEntity::new,
                            ModBlocks.VOID_MINER_PORT.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
