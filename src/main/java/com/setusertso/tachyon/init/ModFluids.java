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

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
