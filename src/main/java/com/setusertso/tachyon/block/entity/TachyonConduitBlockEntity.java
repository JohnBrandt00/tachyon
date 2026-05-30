package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.block.TachyonConduitBlock;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class TachyonConduitBlockEntity extends BlockEntity {

    public static final int ENERGY_CAPACITY = 10_000;
    public static final int ENERGY_TRANSFER = 10_000;
    public static final int FLUID_CAPACITY = 500;
    public static final int FLUID_TRANSFER = 500;

    public static final int TYPE_ENERGY = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FLUID = 2;

    // Per-side, per-resource-type configs: [6 sides][3 types: 0=energy, 1=item, 2=fluid]
    private final ConduitSideConfig[][] sideConfigs = new ConduitSideConfig[6][3];

    private final CustomEnergyStorage energy = new CustomEnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER);
    private final ItemStackHandler items = new ItemStackHandler(1);
    private final FluidTank fluid = new FluidTank(FLUID_CAPACITY);
    private int lastOutputIndex = 0;
    private long lastWrenchTick = -100;

    public TachyonConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TACHYON_CONDUIT.get(), pos, state);
        for (int i = 0; i < 6; i++) {
            for (int t = 0; t < 3; t++) {
                sideConfigs[i][t] = ConduitSideConfig.BOTH;
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TachyonConduitBlockEntity be) {
        boolean changed = false;

        // Pull from PULL/BOTH sides
        for (Direction dir : Direction.values()) {
            if (!state.getValue(getPropertyForDirection(dir))) continue;

            BlockPos neighborPos = pos.relative(dir);
            Direction opposite = dir.getOpposite();
            int sideIdx = dir.ordinal();

            // Pull energy
            if (be.sideConfigs[sideIdx][TYPE_ENERGY].canPull()) {
                if (be.energy.getEnergyStored() < be.energy.getMaxEnergyStored()) {
                    IEnergyStorage neighborEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, opposite);
                    if (neighborEnergy != null && neighborEnergy.canExtract()) {
                        int space = be.energy.getMaxEnergyStored() - be.energy.getEnergyStored();
                        int extracted = neighborEnergy.extractEnergy(Math.min(ENERGY_TRANSFER, space), false);
                        if (extracted > 0) {
                            be.energy.addEnergy(extracted);
                            changed = true;
                        }
                    }
                }
            }

            // Pull items
            if (be.sideConfigs[sideIdx][TYPE_ITEM].canPull()) {
                if (be.items.getStackInSlot(0).isEmpty()) {
                    IItemHandler neighborItems = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, opposite);
                    if (neighborItems != null) {
                        for (int slot = 0; slot < neighborItems.getSlots(); slot++) {
                            ItemStack extracted = neighborItems.extractItem(slot, 64, true);
                            if (!extracted.isEmpty()) {
                                ItemStack remaining = be.items.insertItem(0, extracted, true);
                                if (remaining.getCount() < extracted.getCount()) {
                                    int toExtract = extracted.getCount() - remaining.getCount();
                                    ItemStack actual = neighborItems.extractItem(slot, toExtract, false);
                                    be.items.insertItem(0, actual, false);
                                    changed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Pull fluid
            if (be.sideConfigs[sideIdx][TYPE_FLUID].canPull()) {
                if (be.fluid.getFluidAmount() < be.fluid.getCapacity()) {
                    IFluidHandler neighborFluid = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, opposite);
                    if (neighborFluid != null) {
                        int space = be.fluid.getCapacity() - be.fluid.getFluidAmount();
                        FluidStack drained = neighborFluid.drain(Math.min(FLUID_TRANSFER, space), IFluidHandler.FluidAction.SIMULATE);
                        if (!drained.isEmpty()) {
                            int filled = be.fluid.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                            if (filled > 0) {
                                FluidStack actual = neighborFluid.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                be.fluid.fill(actual, IFluidHandler.FluidAction.EXECUTE);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }

        // Push to PUSH/BOTH sides (round-robin)
        boolean hasResources = be.energy.getEnergyStored() > 0
                || !be.items.getStackInSlot(0).isEmpty()
                || be.fluid.getFluidAmount() > 0;

        if (hasResources) {
            Direction[] directions = Direction.values();
            for (int i = 0; i < 6; i++) {
                int idx = (be.lastOutputIndex + 1 + i) % 6;
                Direction dir = directions[idx];
                if (!state.getValue(getPropertyForDirection(dir))) continue;

                BlockPos neighborPos = pos.relative(dir);
                Direction opposite = dir.getOpposite();
                int sideIdx = dir.ordinal();

                // Push energy
                if (be.sideConfigs[sideIdx][TYPE_ENERGY].canPush() && be.energy.getEnergyStored() > 0) {
                    IEnergyStorage neighborEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, opposite);
                    if (neighborEnergy != null && neighborEnergy.canReceive()) {
                        int pushed = neighborEnergy.receiveEnergy(
                                Math.min(ENERGY_TRANSFER, be.energy.getEnergyStored()), false);
                        if (pushed > 0) {
                            be.energy.consumeEnergy(pushed);
                            changed = true;
                        }
                    }
                }

                // Push items
                if (be.sideConfigs[sideIdx][TYPE_ITEM].canPush() && !be.items.getStackInSlot(0).isEmpty()) {
                    IItemHandler neighborItems = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, opposite);
                    if (neighborItems != null) {
                        ItemStack toMove = be.items.extractItem(0, 64, true);
                        if (!toMove.isEmpty()) {
                            ItemStack leftover = toMove.copy();
                            for (int slot = 0; slot < neighborItems.getSlots(); slot++) {
                                leftover = neighborItems.insertItem(slot, leftover, false);
                                if (leftover.isEmpty()) break;
                            }
                            int moved = toMove.getCount() - leftover.getCount();
                            if (moved > 0) {
                                be.items.extractItem(0, moved, false);
                                changed = true;
                            }
                        }
                    }
                }

                // Push fluid
                if (be.sideConfigs[sideIdx][TYPE_FLUID].canPush() && be.fluid.getFluidAmount() > 0) {
                    IFluidHandler neighborFluid = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, opposite);
                    if (neighborFluid != null) {
                        FluidStack toDrain = be.fluid.drain(FLUID_TRANSFER, IFluidHandler.FluidAction.SIMULATE);
                        if (!toDrain.isEmpty()) {
                            int filled = neighborFluid.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);
                            if (filled > 0) {
                                be.fluid.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                changed = true;
                            }
                        }
                    }
                }
            }
            be.lastOutputIndex = (be.lastOutputIndex + 1) % 6;
        }

        if (changed) {
            be.setChanged();
        }
    }

    // === Side config accessors ===

    public ConduitSideConfig getSideConfig(Direction dir, int type) {
        return sideConfigs[dir.ordinal()][type];
    }

    public void cycleSideConfig(Direction dir, int type) {
        int sideIdx = dir.ordinal();
        sideConfigs[sideIdx][type] = sideConfigs[sideIdx][type].next();
        if (level != null) {
            lastWrenchTick = level.getGameTime();
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** Called when the wrench inspects this conduit (non-shift click) */
    public void markWrenchInteraction() {
        if (level != null) {
            lastWrenchTick = level.getGameTime();
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    /** Returns true if the wrench was used on this conduit recently (within 3 seconds / 60 ticks) */
    public boolean isShowingModeIndicators() {
        if (level == null) return false;
        return level.getGameTime() - lastWrenchTick < 60;
    }

    public IEnergyStorage getEnergyForSide(Direction side) {
        if (side == null) return null;
        if (sideConfigs[side.ordinal()][TYPE_ENERGY] == ConduitSideConfig.DISABLED) return null;
        return energy;
    }

    public IItemHandler getItemsForSide(Direction side) {
        if (side == null) return null;
        if (sideConfigs[side.ordinal()][TYPE_ITEM] == ConduitSideConfig.DISABLED) return null;
        return items;
    }

    public IFluidHandler getFluidForSide(Direction side) {
        if (side == null) return null;
        if (sideConfigs[side.ordinal()][TYPE_FLUID] == ConduitSideConfig.DISABLED) return null;
        return fluid;
    }

    public boolean hasResources() {
        return energy.getEnergyStored() > 0
                || !items.getStackInSlot(0).isEmpty()
                || fluid.getFluidAmount() > 0;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int[] configs = new int[18];
        for (int i = 0; i < 6; i++) {
            for (int t = 0; t < 3; t++) {
                configs[i * 3 + t] = sideConfigs[i][t].ordinal();
            }
        }
        tag.putIntArray("SideConfigs", configs);
        tag.put("Energy", energy.serializeNBT(registries));
        tag.put("Items", items.serializeNBT(registries));
        tag.put("Fluid", fluid.writeToNBT(registries, new CompoundTag()));
        tag.putInt("LastOutput", lastOutputIndex);
        tag.putLong("WrenchTick", lastWrenchTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastWrenchTick = tag.getLong("WrenchTick");
        if (tag.contains("SideConfigs")) {
            int[] configs = tag.getIntArray("SideConfigs");
            if (configs.length == 18) {
                for (int i = 0; i < 6; i++) {
                    for (int t = 0; t < 3; t++) {
                        sideConfigs[i][t] = ConduitSideConfig.fromOrdinal(configs[i * 3 + t]);
                    }
                }
            } else if (configs.length == 6) {
                // Legacy: single config per side, apply to all types
                for (int i = 0; i < 6; i++) {
                    ConduitSideConfig legacy = ConduitSideConfig.fromOrdinal(configs[i]);
                    for (int t = 0; t < 3; t++) {
                        sideConfigs[i][t] = legacy;
                    }
                }
            }
        }
        if (tag.contains("Energy")) {
            energy.deserializeNBT(registries, tag.get("Energy"));
        }
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        if (tag.contains("Fluid")) {
            fluid.readFromNBT(registries, tag.getCompound("Fluid"));
        }
        lastOutputIndex = tag.getInt("LastOutput");
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

    private static net.minecraft.world.level.block.state.properties.BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> TachyonConduitBlock.NORTH;
            case SOUTH -> TachyonConduitBlock.SOUTH;
            case EAST -> TachyonConduitBlock.EAST;
            case WEST -> TachyonConduitBlock.WEST;
            case UP -> TachyonConduitBlock.UP;
            case DOWN -> TachyonConduitBlock.DOWN;
        };
    }

    public static String typeDisplayName(int type) {
        return switch (type) {
            case TYPE_ENERGY -> "Energy";
            case TYPE_ITEM -> "Item";
            case TYPE_FLUID -> "Fluid";
            default -> "Unknown";
        };
    }
}
