package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.init.ModBlockEntities;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PhotonicInjectorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CONSUME_INTERVAL = 2000; // ticks per condensed light consumed

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.CONDENSED_LIGHT.get());
        }
    };

    private int burnTime = 0;
    private boolean active = false;

    public PhotonicInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PHOTONIC_INJECTOR.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isActive() {
        return active;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PhotonicInjectorBlockEntity be) {
        boolean wasActive = be.active;

        if (be.burnTime > 0) {
            be.burnTime--;
            be.active = true;
        } else {
            // Try to consume a condensed light
            ItemStack fuel = be.items.getStackInSlot(0);
            if (!fuel.isEmpty() && fuel.is(ModItems.CONDENSED_LIGHT.get())) {
                be.items.extractItem(0, 1, false);
                be.burnTime = CONSUME_INTERVAL;
                be.active = true;
            } else {
                be.active = false;
            }
        }

        if (wasActive != be.active) {
            // Update block state
            level.setBlock(pos, state.setValue(PhotonicInjectorBlock.ACTIVE, be.active), Block.UPDATE_ALL);
            be.setChanged();
            be.syncToClient();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.photonic_injector");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        // Simple single-slot container — reuse a minimal approach
        return new PhotonicInjectorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("BurnTime", burnTime);
        tag.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        burnTime = tag.getInt("BurnTime");
        active = tag.getBoolean("Active");
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

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
