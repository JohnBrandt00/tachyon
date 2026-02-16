package com.setusertso.tachyon.init;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ACCELERATOR_PORT.get(),
                (be, side) -> be.getItemCapHandler()
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

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.CREATIVE_POWER_SOURCE.get(),
                (be, side) -> be.getEnergyStorage()
        );

        // Singularity Engine ports
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SINGULARITY_PORT.get(),
                (be, side) -> be.getItemCapHandler()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.SINGULARITY_PORT.get(),
                (be, side) -> be.getEnergyHandler()
        );

        // Creative Power Sink energy handler
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.CREATIVE_POWER_SINK.get(),
                (be, side) -> be.getEnergyStorage()
        );

        // Thorium Reactor
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.THORIUM_REACTOR.get(),
                (be, side) -> be.getEnergy()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.THORIUM_REACTOR.get(),
                (be, side) -> be.getItems()
        );

        // Photonic Injector item handler
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PHOTONIC_INJECTOR.get(),
                (be, side) -> be.getItems()
        );
    }
}
