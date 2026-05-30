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

        // Solar Collector item handler (output only)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_COLLECTOR.get(),
                (be, side) -> be.getItemHandler()
        );

        // Photon Compressor
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.PHOTON_COMPRESSOR.get(),
                (be, side) -> be.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PHOTON_COMPRESSOR.get(),
                (be, side) -> be.getSidedItemHandler(side)
        );

        // Ore Crusher
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ORE_CRUSHER.get(),
                (be, side) -> be.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ORE_CRUSHER.get(),
                (be, side) -> be.getSidedItemHandler(side)
        );

        // Alloy Forge
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ALLOY_FORGE.get(),
                (be, side) -> be.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ALLOY_FORGE.get(),
                (be, side) -> be.getSidedItemHandler(side)
        );

        // Void Miner Controller
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.VOID_MINER_CONTROLLER.get(),
                (be, side) -> be.getEnergy()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.VOID_MINER_CONTROLLER.get(),
                (be, side) -> be.getItems()
        );

        // Void Miner Port
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.VOID_MINER_PORT.get(),
                (be, side) -> be.getItemHandler()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.VOID_MINER_PORT.get(),
                (be, side) -> be.getEnergyHandler()
        );

        // Tachyon Condenser Port
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CONDENSER_PORT.get(),
                (be, side) -> be.getItemCapHandler()
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.CONDENSER_PORT.get(),
                (be, side) -> be.getFluidHandler()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.CONDENSER_PORT.get(),
                (be, side) -> be.getEnergyHandler()
        );

        // Tachyon Conduit
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.TACHYON_CONDUIT.get(),
                (be, side) -> be.getEnergyForSide(side)
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.TACHYON_CONDUIT.get(),
                (be, side) -> be.getItemsForSide(side)
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.TACHYON_CONDUIT.get(),
                (be, side) -> be.getFluidForSide(side)
        );

        // Tachyon Relay
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.TACHYON_RELAY.get(),
                (be, side) -> be.getEnergyForSide(side)
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.TACHYON_RELAY.get(),
                (be, side) -> be.getItemsForSide(side)
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.TACHYON_RELAY.get(),
                (be, side) -> be.getFluidForSide(side)
        );
    }
}
