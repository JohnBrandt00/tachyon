package com.setusertso.tachyon.block.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class OutputOnlyItemHandler implements IItemHandler {
    private final ItemStackHandler inner;
    private final int slot;

    public OutputOnlyItemHandler(ItemStackHandler inner, int slot) {
        this.inner = inner;
        this.slot = slot;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inner.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        return inner.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int index) {
        return inner.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int index, ItemStack stack) {
        return false;
    }
}
