package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.block.VoidMinerPortBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class VoidMinerPortBlockEntity extends BlockEntity {
    private static final int CATALYST_SLOT = 0;
    private static final int OUTPUT_SLOT_START = 7;
    private static final int OUTPUT_SLOT_END = 12;
    private static final int ENERGY_TRANSFER_RATE = 10000; // RF per tick

    private BlockPos masterPos;
    private final ItemStackHandler internalInventory;

    public VoidMinerPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_MINER_PORT.get(), pos, state);
        this.internalInventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VoidMinerPortBlockEntity portBE) {
        if (level.isClientSide || portBE.masterPos == null) {
            return;
        }

        BlockEntity be = level.getBlockEntity(portBE.masterPos);
        if (!(be instanceof VoidMinerControllerBlockEntity controller)) {
            return;
        }

        VoidMinerPortBlock.VoidMinerPortMode mode = state.getValue(VoidMinerPortBlock.MODE);

        switch (mode) {
            case CATALYST_INPUT -> portBE.handleCatalystInput(controller);
            case ITEM_OUTPUT -> portBE.handleItemOutput(controller);
            case ENERGY_INPUT -> portBE.handleEnergyInput(level, pos, controller);
        }
    }

    private void handleCatalystInput(VoidMinerControllerBlockEntity controller) {
        ItemStack internalStack = internalInventory.getStackInSlot(0);
        if (internalStack.isEmpty()) {
            return;
        }

        ItemStackHandler controllerInventory = controller.getItems();
        ItemStack catalystSlot = controllerInventory.getStackInSlot(CATALYST_SLOT);

        // Try to insert into catalyst slot
        if (catalystSlot.isEmpty()) {
            // Slot is empty, move entire stack
            controllerInventory.setStackInSlot(CATALYST_SLOT, internalStack.copy());
            internalInventory.setStackInSlot(0, ItemStack.EMPTY);
            controller.setChanged();
        } else if (ItemStack.isSameItemSameComponents(catalystSlot, internalStack)) {
            // Same item, try to merge
            int spaceAvailable = catalystSlot.getMaxStackSize() - catalystSlot.getCount();
            if (spaceAvailable > 0) {
                int toTransfer = Math.min(spaceAvailable, internalStack.getCount());
                catalystSlot.grow(toTransfer);
                internalStack.shrink(toTransfer);
                controllerInventory.setStackInSlot(CATALYST_SLOT, catalystSlot);
                internalInventory.setStackInSlot(0, internalStack);
                controller.setChanged();
            }
        }
    }

    private void handleItemOutput(VoidMinerControllerBlockEntity controller) {
        ItemStack internalStack = internalInventory.getStackInSlot(0);
        if (!internalStack.isEmpty()) {
            return; // Internal slot is full, can't pull more
        }

        ItemStackHandler controllerInventory = controller.getItems();

        // Try to pull from output slots (7-12)
        for (int i = OUTPUT_SLOT_START; i <= OUTPUT_SLOT_END; i++) {
            ItemStack outputStack = controllerInventory.getStackInSlot(i);
            if (!outputStack.isEmpty()) {
                // Pull the entire stack
                internalInventory.setStackInSlot(0, outputStack.copy());
                controllerInventory.setStackInSlot(i, ItemStack.EMPTY);
                controller.setChanged();
                break; // Only pull one stack per tick
            }
        }
    }

    private void handleEnergyInput(Level level, BlockPos pos, VoidMinerControllerBlockEntity controller) {
        IEnergyStorage controllerEnergy = controller.getEnergy();
        if (controllerEnergy == null) {
            return;
        }

        // Check all adjacent blocks for energy sources
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockEntity adjacentBE = level.getBlockEntity(adjacentPos);

            if (adjacentBE != null) {
                IEnergyStorage adjacentEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                        adjacentPos, level.getBlockState(adjacentPos), adjacentBE, direction.getOpposite());

                if (adjacentEnergy != null && adjacentEnergy.canExtract()) {
                    // Try to extract energy from adjacent block
                    int energyToTransfer = Math.min(ENERGY_TRANSFER_RATE, controllerEnergy.getMaxEnergyStored() - controllerEnergy.getEnergyStored());
                    if (energyToTransfer > 0) {
                        int extracted = adjacentEnergy.extractEnergy(energyToTransfer, false);
                        if (extracted > 0) {
                            controllerEnergy.receiveEnergy(extracted, false);
                            controller.setChanged();
                        }
                    }
                }
            }
        }
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
    }

    public ItemStackHandler getItemHandler() {
        return internalInventory;
    }

    @Nullable
    public IEnergyStorage getEnergyHandler() {
        if (getBlockState().getValue(VoidMinerPortBlock.MODE) == VoidMinerPortBlock.VoidMinerPortMode.ENERGY_INPUT) {
            if (masterPos != null && level != null) {
                BlockEntity be = level.getBlockEntity(masterPos);
                if (be instanceof VoidMinerControllerBlockEntity controller) {
                    return controller.getEnergy();
                }
            }
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) {
            tag.putInt("MasterX", masterPos.getX());
            tag.putInt("MasterY", masterPos.getY());
            tag.putInt("MasterZ", masterPos.getZ());
        }
        tag.put("Inventory", internalInventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterX")) {
            masterPos = new BlockPos(
                    tag.getInt("MasterX"),
                    tag.getInt("MasterY"),
                    tag.getInt("MasterZ")
            );
        } else {
            masterPos = null;
        }
        if (tag.contains("Inventory")) {
            internalInventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }
}
