package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.block.entity.VoidMinerControllerBlockEntity;
import com.setusertso.tachyon.init.ModMenuTypes;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class VoidMinerMenu extends AbstractContainerMenu {
    private final ContainerData data;

    private static final int TE_SLOTS = 13; // 1 catalyst + 6 modules + 6 outputs
    private static final int PLAYER_INV_START = TE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    // Client-side constructor
    public VoidMinerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(13), new SimpleContainerData(8));
    }

    // Server-side constructor
    public VoidMinerMenu(int containerId, Inventory playerInventory,
                          IItemHandler handler, ContainerData data) {
        super(ModMenuTypes.VOID_MINER.get(), containerId);
        this.data = data;

        // Catalyst slot (34, 35)
        this.addSlot(new SlotItemHandler(handler, 0, 34, 35));

        // Module slots (8, 66) to (98, 66) — horizontal row of 6
        for (int i = 0; i < 6; i++) {
            final int slotIndex = 1 + i;
            this.addSlot(new SlotItemHandler(handler, slotIndex, 8 + i * 18, 66) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return VoidMinerControllerBlockEntity.isVoidMinerModule(stack);
                }
            });
        }

        // Output slots — 3x2 grid
        // Row 1: (92, 26), (110, 26), (128, 26)
        // Row 2: (92, 46), (110, 46), (128, 46)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = 7 + row * 3 + col;
                this.addSlot(new SlotItemHandler(handler, slotIndex, 92 + col * 18, 26 + row * 20) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false; // Output only
                    }
                });
            }
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    public int getProgress() { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }

    public int getEnergy() {
        return (data.get(3) << 16) | (data.get(2) & 0xFFFF);
    }

    public int getMaxEnergy() {
        return (data.get(5) << 16) | (data.get(4) & 0xFFFF);
    }

    public int getCatalystCycles() { return data.get(6); }
    public int getStructureTier() { return data.get(7); }

    public float getProgressScaled() {
        int max = getMaxProgress();
        return max == 0 ? 0 : (float) getProgress() / max;
    }

    public float getEnergyScaled() {
        int max = getMaxEnergy();
        return max == 0 ? 0 : (float) getEnergy() / max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index < TE_SLOTS) {
                // Move from machine to player
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (VoidMinerControllerBlockEntity.isVoidMinerModule(slotStack)) {
                // Module → module slots (1-6)
                if (!this.moveItemStackTo(slotStack, 1, 7, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Other items → catalyst slot (0)
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
