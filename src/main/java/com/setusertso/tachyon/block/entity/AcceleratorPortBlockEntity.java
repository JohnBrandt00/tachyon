package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.AcceleratorPortMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class AcceleratorPortBlockEntity extends BlockEntity implements MenuProvider {
    private BlockPos masterPos = null;
    private PortMode mode = PortMode.ITEM_INPUT;

    public AcceleratorPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ACCELERATOR_PORT.get(), pos, state);
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public PortMode getMode() {
        return mode;
    }

    public void cycleMode() {
        this.mode = this.mode.next();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public IItemHandler getItemHandler() {
        if (masterPos == null || level == null) return null;
        if (level.getBlockEntity(masterPos) instanceof AcceleratorControllerBlockEntity controller) {
            return switch (mode) {
                case ITEM_INPUT -> new InputOnlyItemHandler(controller.getItemHandler(), AcceleratorControllerBlockEntity.INPUT_SLOT);
                case ITEM_OUTPUT -> new OutputOnlyItemHandler(controller.getItemHandler(), AcceleratorControllerBlockEntity.OUTPUT_SLOT);
                default -> null;
            };
        }
        return null;
    }

    public IFluidHandler getFluidHandler() {
        if (masterPos == null || level == null) return null;
        if (level.getBlockEntity(masterPos) instanceof AcceleratorControllerBlockEntity controller) {
            return switch (mode) {
                case FLUID_INPUT -> new InputOnlyFluidHandler(controller.getFluidTank());
                case FLUID_OUTPUT -> new OutputOnlyFluidHandler(controller.getFluidTank());
                default -> null;
            };
        }
        return null;
    }

    public IEnergyStorage getEnergyHandler() {
        if (masterPos == null || level == null) return null;
        if (mode != PortMode.ENERGY_INPUT) return null;
        if (level.getBlockEntity(masterPos) instanceof AcceleratorControllerBlockEntity controller) {
            return controller.getEnergyStorage();
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.accelerator_port");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AcceleratorPortMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) {
            tag.putInt("MasterX", masterPos.getX());
            tag.putInt("MasterY", masterPos.getY());
            tag.putInt("MasterZ", masterPos.getZ());
        }
        tag.putInt("Mode", mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterX")) {
            masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        } else {
            masterPos = null;
        }
        mode = PortMode.fromOrdinal(tag.getInt("Mode"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
