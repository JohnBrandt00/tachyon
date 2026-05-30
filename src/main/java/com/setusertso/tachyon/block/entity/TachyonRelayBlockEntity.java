package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.block.TachyonRelayBlock;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.joml.Vector3f;

import static com.setusertso.tachyon.block.entity.TachyonConduitBlockEntity.*;

public class TachyonRelayBlockEntity extends BlockEntity {

    private static final int RELAY_ENERGY_CAPACITY = 50_000;
    private static final int MAX_LINK_DISTANCE_SQ = 64 * 64;

    // Per-side, per-resource-type configs: [6 sides][3 types: 0=energy, 1=item, 2=fluid]
    private final ConduitSideConfig[][] sideConfigs = new ConduitSideConfig[6][3];

    private final CustomEnergyStorage energy = new CustomEnergyStorage(RELAY_ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER);
    private final ItemStackHandler items = new ItemStackHandler(1);
    private final FluidTank fluid = new FluidTank(FLUID_CAPACITY);
    private int lastOutputIndex = 0;

    private BlockPos linkedPos = null;

    public TachyonRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TACHYON_RELAY.get(), pos, state);
        for (int i = 0; i < 6; i++) {
            for (int t = 0; t < 3; t++) {
                sideConfigs[i][t] = ConduitSideConfig.BOTH;
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TachyonRelayBlockEntity be) {
        boolean changed = false;

        // === Local conduit behavior ===

        // Pull from PULL/BOTH sides
        for (Direction dir : Direction.values()) {
            int sideIdx = dir.ordinal();

            BlockPos neighborPos = pos.relative(dir);
            Direction opposite = dir.getOpposite();

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
                int sideIdx = dir.ordinal();

                BlockPos neighborPos = pos.relative(dir);
                Direction opposite = dir.getOpposite();

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

        // === Wireless relay transfer ===
        // Only the relay with the lower block position runs wireless logic to prevent ping-pong
        if (be.linkedPos != null) {
            if (be.linkedPos.distSqr(pos) > MAX_LINK_DISTANCE_SQ) {
                be.clearLink();
                changed = true;
            } else if (level.getBlockEntity(be.linkedPos) instanceof TachyonRelayBlockEntity partner) {
                // Only master (lower position) handles wireless transfer
                if (pos.compareTo(be.linkedPos) < 0) {
                    boolean wirelessTransferred = false;

                    // Energy: balance between both relays with 5% tax
                    int myEnergy = be.energy.getEnergyStored();
                    int partnerEnergy = partner.energy.getEnergyStored();
                    if (Math.abs(myEnergy - partnerEnergy) > 100) {
                        TachyonRelayBlockEntity high = myEnergy > partnerEnergy ? be : partner;
                        TachyonRelayBlockEntity low = myEnergy > partnerEnergy ? partner : be;
                        int diff = (high.energy.getEnergyStored() - low.energy.getEnergyStored()) / 2;
                        int toTransfer = Math.min(diff, ENERGY_TRANSFER);
                        int extracted = high.energy.consumeEnergy(toTransfer);
                        int taxed = (int) (extracted * 0.95);
                        low.energy.addEnergy(taxed);
                        changed = true;
                        wirelessTransferred = true;
                        high.setChanged();
                        low.setChanged();
                    }

                    // Items: transfer from whichever has items to whichever is empty
                    if (!be.items.getStackInSlot(0).isEmpty() && partner.items.getStackInSlot(0).isEmpty()) {
                        ItemStack toMove = be.items.extractItem(0, 64, false);
                        ItemStack remaining = partner.items.insertItem(0, toMove, false);
                        if (!remaining.isEmpty()) {
                            be.items.insertItem(0, remaining, false);
                        }
                        changed = true;
                        wirelessTransferred = true;
                        be.setChanged();
                        partner.setChanged();
                    } else if (!partner.items.getStackInSlot(0).isEmpty() && be.items.getStackInSlot(0).isEmpty()) {
                        ItemStack toMove = partner.items.extractItem(0, 64, false);
                        ItemStack remaining = be.items.insertItem(0, toMove, false);
                        if (!remaining.isEmpty()) {
                            partner.items.insertItem(0, remaining, false);
                        }
                        changed = true;
                        wirelessTransferred = true;
                        be.setChanged();
                        partner.setChanged();
                    }

                    // Fluid: balance between both relays
                    int myFluid = be.fluid.getFluidAmount();
                    int partnerFluid = partner.fluid.getFluidAmount();
                    if (Math.abs(myFluid - partnerFluid) > 50) {
                        boolean meHigher = myFluid > partnerFluid;
                        FluidTank highTank = meHigher ? be.fluid : partner.fluid;
                        FluidTank lowTank = meHigher ? partner.fluid : be.fluid;
                        int diff = (highTank.getFluidAmount() - lowTank.getFluidAmount()) / 2;
                        int toTransfer = Math.min(diff, FLUID_TRANSFER);
                        FluidStack drained = highTank.drain(toTransfer, IFluidHandler.FluidAction.EXECUTE);
                        if (!drained.isEmpty()) {
                            lowTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            changed = true;
                            wirelessTransferred = true;
                            be.setChanged();
                            partner.setChanged();
                        }
                    }

                    // Spawn particles at both relays when transfer occurs (throttled to every 10 ticks)
                    if (wirelessTransferred && level.getGameTime() % 10 == 0) {
                        spawnTransferParticles(level, pos, be.linkedPos);
                    }
                }
                // Non-master relay: skip wireless, partner handles it
            } else {
                be.clearLink();
                changed = true;
            }
        }

        if (changed) {
            be.setChanged();
        }
    }

    private static final DustParticleOptions TRANSFER_PARTICLE =
            new DustParticleOptions(new Vector3f(0.6f, 0.2f, 0.9f), 1.0f);

    /** Spawn a small purple particle burst at both relays when wireless transfer occurs */
    private static void spawnTransferParticles(Level level, BlockPos from, BlockPos to) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(TRANSFER_PARTICLE,
                    from.getX() + 0.5, from.getY() + 0.8, from.getZ() + 0.5,
                    2, 0.15, 0.15, 0.15, 0.01);
            serverLevel.sendParticles(TRANSFER_PARTICLE,
                    to.getX() + 0.5, to.getY() + 0.8, to.getZ() + 0.5,
                    2, 0.15, 0.15, 0.15, 0.01);
        }
    }

    // === Link management ===

    public BlockPos getLinkedPos() {
        return linkedPos;
    }

    public void setLinkedPos(BlockPos pos) {
        this.linkedPos = pos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState().setValue(TachyonRelayBlock.LINKED, pos != null);
            level.setBlock(worldPosition, state, Block.UPDATE_ALL);
        }
    }

    public void clearLink() {
        this.linkedPos = null;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState().setValue(TachyonRelayBlock.LINKED, false);
            level.setBlock(worldPosition, state, Block.UPDATE_ALL);
        }
    }

    public void notifyPartnerOfRemoval() {
        if (linkedPos != null && level != null) {
            if (level.getBlockEntity(linkedPos) instanceof TachyonRelayBlockEntity partner) {
                partner.clearLink();
            }
        }
    }

    // === Side config ===

    public ConduitSideConfig getSideConfig(Direction dir, int type) {
        return sideConfigs[dir.ordinal()][type];
    }

    public void cycleSideConfig(Direction dir, int type) {
        int sideIdx = dir.ordinal();
        sideConfigs[sideIdx][type] = sideConfigs[sideIdx][type].next();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
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

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
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

    // === NBT ===

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
        if (linkedPos != null) {
            tag.putInt("LinkX", linkedPos.getX());
            tag.putInt("LinkY", linkedPos.getY());
            tag.putInt("LinkZ", linkedPos.getZ());
            tag.putBoolean("HasLink", true);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("SideConfigs")) {
            int[] configs = tag.getIntArray("SideConfigs");
            if (configs.length == 18) {
                for (int i = 0; i < 6; i++) {
                    for (int t = 0; t < 3; t++) {
                        sideConfigs[i][t] = ConduitSideConfig.fromOrdinal(configs[i * 3 + t]);
                    }
                }
            } else if (configs.length == 6) {
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
        if (tag.getBoolean("HasLink")) {
            linkedPos = new BlockPos(tag.getInt("LinkX"), tag.getInt("LinkY"), tag.getInt("LinkZ"));
        } else {
            linkedPos = null;
        }
    }
}
