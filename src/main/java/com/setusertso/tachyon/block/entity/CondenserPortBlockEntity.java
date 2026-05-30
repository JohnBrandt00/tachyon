package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.CondenserPortBlock;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CondenserPortBlockEntity extends BlockEntity {
    private BlockPos masterPos = null;
    private CondenserPortMode mode = CondenserPortMode.ITEM_INPUT;

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CondenserPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDENSER_PORT.get(), pos, state);
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

    public CondenserPortMode getMode() {
        return mode;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public void cycleMode() {
        this.mode = this.mode.next();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            BlockState newState = getBlockState().setValue(CondenserPortBlock.MODE, mode);
            level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.getValue(CondenserPortBlock.MODE) != mode) {
                level.setBlock(worldPosition, state.setValue(CondenserPortBlock.MODE, mode),
                        Block.UPDATE_CLIENTS);
            }
        }
    }

    // --- Server Tick: Transfer items/fluids/energy between port and controller ---

    public static void serverTick(Level level, BlockPos pos, BlockState state, CondenserPortBlockEntity be) {
        if (be.masterPos == null) return;
        if (!(level.getBlockEntity(be.masterPos) instanceof CondenserControllerBlockEntity controller)) return;

        switch (be.mode) {
            case ITEM_INPUT -> {
                // Push tachyon shards from port buffer to controller input
                ItemStack portStack = be.items.getStackInSlot(0);
                if (!portStack.isEmpty() && portStack.is(ModItems.TACHYON_SHARD.get())) {
                    ItemStack remaining = controller.getItemHandler().insertItem(
                            CondenserControllerBlockEntity.INPUT_SLOT, portStack, false);
                    be.items.setStackInSlot(0, remaining);
                }
            }
            case FLUID_OUTPUT -> {
                // Push fluid from controller tank to adjacent fluid handlers
                if (controller.getFluidTank().getFluidAmount() > 0) {
                    for (Direction dir : Direction.values()) {
                        BlockPos adjPos = pos.relative(dir);
                        IFluidHandler adjHandler = level.getCapability(
                                Capabilities.FluidHandler.BLOCK, adjPos, dir.getOpposite());
                        if (adjHandler != null) {
                            FluidStack drained = controller.getFluidTank().drain(250, IFluidHandler.FluidAction.SIMULATE);
                            if (!drained.isEmpty()) {
                                int filled = adjHandler.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                                if (filled > 0) {
                                    FluidStack actual = controller.getFluidTank().drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                    adjHandler.fill(actual, IFluidHandler.FluidAction.EXECUTE);
                                    controller.setChanged();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            case ENERGY_INPUT -> {
                // Pull energy from adjacent blocks into controller
                for (Direction dir : Direction.values()) {
                    BlockPos adjPos = pos.relative(dir);
                    IEnergyStorage adjEnergy = level.getCapability(
                            Capabilities.EnergyStorage.BLOCK, adjPos, dir.getOpposite());
                    if (adjEnergy != null && adjEnergy.canExtract()) {
                        int toExtract = Math.min(10_000, controller.getEnergyStorage().receiveEnergy(10_000, true));
                        if (toExtract > 0) {
                            int extracted = adjEnergy.extractEnergy(toExtract, false);
                            if (extracted > 0) {
                                controller.getEnergyStorage().receiveEnergy(extracted, false);
                                controller.setChanged();
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Capability delegation (for pipes/automation) ---

    public IItemHandler getItemCapHandler() {
        if (masterPos == null || level == null) return null;
        if (level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
            if (mode == CondenserPortMode.ITEM_INPUT) {
                return new InputOnlyItemHandler(controller.getItemHandler(), CondenserControllerBlockEntity.INPUT_SLOT);
            }
        }
        return null;
    }

    public IFluidHandler getFluidHandler() {
        if (masterPos == null || level == null) return null;
        if (level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
            if (mode == CondenserPortMode.FLUID_OUTPUT) {
                return new OutputOnlyFluidHandler(controller.getFluidTank());
            }
        }
        return null;
    }

    public IEnergyStorage getEnergyHandler() {
        if (masterPos == null || level == null) return null;
        if (mode != CondenserPortMode.ENERGY_INPUT) return null;
        if (level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
            return controller.getEnergyStorage();
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
        tag.putInt("Mode", mode.ordinal());
        tag.put("PortItems", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterX")) {
            masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        } else {
            masterPos = null;
        }
        mode = CondenserPortMode.fromOrdinal(tag.getInt("Mode"));
        if (tag.contains("PortItems")) {
            items.deserializeNBT(registries, tag.getCompound("PortItems"));
        }
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
