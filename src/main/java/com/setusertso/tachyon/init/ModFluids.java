package com.setusertso.tachyon.init;

import java.util.function.Supplier;

import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.tachyon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, tachyon.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, tachyon.MODID);

    // Helium gas - used as coolant in the Particle Accelerator
    public static final Supplier<FluidType> HELIUM_TYPE = FLUID_TYPES.register("helium",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.tachyon.helium")
                    .density(-1)
                    .viscosity(50)
                    .temperature(4)
            ));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> HELIUM_SOURCE =
            FLUIDS.register("helium",
                    () -> new BaseFlowingFluid.Source(heliumProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> HELIUM_FLOWING =
            FLUIDS.register("helium_flowing",
                    () -> new BaseFlowingFluid.Flowing(heliumProperties()));

    private static BaseFlowingFluid.Properties heliumProperties() {
        return new BaseFlowingFluid.Properties(HELIUM_TYPE, HELIUM_SOURCE, HELIUM_FLOWING)
                .block(ModBlocks.HELIUM_BLOCK)
                .bucket(ModItems.HELIUM_BUCKET);
    }

    // Tachyon Flux - dense purple liquid, signature tachyon resource
    public static final Supplier<FluidType> TACHYON_FLUX_TYPE = FLUID_TYPES.register("tachyon_flux",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.tachyon.tachyon_flux")
                    .density(1200)
                    .viscosity(800)
                    .temperature(5000)
            ));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> TACHYON_FLUX_SOURCE =
            FLUIDS.register("tachyon_flux",
                    () -> new BaseFlowingFluid.Source(tachyonFluxProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> TACHYON_FLUX_FLOWING =
            FLUIDS.register("tachyon_flux_flowing",
                    () -> new BaseFlowingFluid.Flowing(tachyonFluxProperties()));

    private static BaseFlowingFluid.Properties tachyonFluxProperties() {
        return new BaseFlowingFluid.Properties(TACHYON_FLUX_TYPE, TACHYON_FLUX_SOURCE, TACHYON_FLUX_FLOWING)
                .block(ModBlocks.TACHYON_FLUX_BLOCK)
                .bucket(ModItems.TACHYON_FLUX_BUCKET);
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
