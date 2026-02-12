package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModFluids;
import com.setusertso.tachyon.init.ModTags;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class AcceleratorPortBlockEntity extends BlockEntity implements MenuProvider {
    private BlockPos masterPos = null;
    private PortMode mode = PortMode.ITEM_INPUT;

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

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

    public ItemStackHandler getItems() {
        return items;
    }

    public void cycleMode() {
        this.mode = this.mode.next();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            BlockState newState = getBlockState().setValue(
                    com.setusertso.tachyon.block.AcceleratorPortBlock.MODE, mode);
            level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.getValue(com.setusertso.tachyon.block.AcceleratorPortBlock.MODE) != mode) {
                level.setBlock(worldPosition, state.setValue(
                        com.setusertso.tachyon.block.AcceleratorPortBlock.MODE, mode),
                        Block.UPDATE_CLIENTS);
            }
        }
    }

    // --- Server Tick: Transfer items/fluids between port slot and controller ---

    public static void serverTick(Level level, BlockPos pos, BlockState state, AcceleratorPortBlockEntity be) {
        if (be.masterPos == null) return;
        if (!(level.getBlockEntity(be.masterPos) instanceof AcceleratorControllerBlockEntity controller)) return;

        switch (be.mode) {
            case FLUID_INPUT -> {
                ItemStack stack = be.items.getStackInSlot(0);
                if (!stack.isEmpty() && stack.is(ModItems.HELIUM_BUCKET.get())) {
                    FluidStack toFill = new FluidStack(ModFluids.HELIUM_SOURCE.get(), 1000);
                    int filled = controller.getFluidTank().fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                    if (filled == 1000) {
                        controller.getFluidTank().fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                        be.items.setStackInSlot(0, new ItemStack(Items.BUCKET));
                        controller.setChanged();
                    }
                }
            }
            case FLUID_OUTPUT -> {
                ItemStack stack = be.items.getStackInSlot(0);
                if (!stack.isEmpty() && stack.is(Items.BUCKET) && stack.getCount() == 1) {
                    FluidStack drained = controller.getFluidTank().drain(1000, IFluidHandler.FluidAction.SIMULATE);
                    if (drained.getAmount() == 1000) {
                        controller.getFluidTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                        be.items.setStackInSlot(0, new ItemStack(ModItems.HELIUM_BUCKET.get()));
                        controller.setChanged();
                    }
                }
            }
            case ITEM_INPUT -> {
                ItemStack portStack = be.items.getStackInSlot(0);
                if (!portStack.isEmpty() && portStack.is(ModTags.Items.INGOTS_THORIUM)) {
                    ItemStack remaining = controller.getItemHandler().insertItem(
                            AcceleratorControllerBlockEntity.INPUT_SLOT, portStack, false);
                    be.items.setStackInSlot(0, remaining);
                }
            }
            case ITEM_OUTPUT -> {
                ItemStack portStack = be.items.getStackInSlot(0);
                if (portStack.isEmpty() || (portStack.is(ModItems.TACHYON_SHARD.get())
                        && portStack.getCount() < portStack.getMaxStackSize())) {
                    ItemStack extracted = controller.getItemHandler().extractItem(
                            AcceleratorControllerBlockEntity.OUTPUT_SLOT, 1, false);
                    if (!extracted.isEmpty()) {
                        if (portStack.isEmpty()) {
                            be.items.setStackInSlot(0, extracted);
                        } else {
                            portStack.grow(extracted.getCount());
                        }
                    }
                }
            }
            default -> {}
        }
    }

    // --- Capability delegation (for pipes/automation) ---

    public IItemHandler getItemCapHandler() {
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
        mode = PortMode.fromOrdinal(tag.getInt("Mode"));
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
