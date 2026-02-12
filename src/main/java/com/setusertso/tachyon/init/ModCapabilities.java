package com.setusertso.tachyon.init;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ACCELERATOR_PORT.get(),
                (be, side) -> be.getItemHandler()
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.ACCELERATOR_PORT.get(),
                (be, side) -> be.getFluidHandler()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ACCELERATOR_PORT.get(),
                (be, side) -> be.getEnergyHandler()
        );
    }
}
