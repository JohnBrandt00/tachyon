package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.entity.IUpgradeable;
import com.setusertso.tachyon.init.ModMenuTypes;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PhotonCompressorMenu extends AbstractContainerMenu {
    private final ContainerData data;

    private static final int MACHINE_SLOTS = 3;
    private static final int UPGRADE_SLOTS = 3;
    private static final int TE_SLOTS = MACHINE_SLOTS + UPGRADE_SLOTS;
    private static final int PLAYER_INV_START = TE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    // Client constructor
    public PhotonCompressorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(3), new ItemStackHandler(3), new SimpleContainerData(4));
    }

    // Server constructor
    public PhotonCompressorMenu(int containerId, Inventory playerInventory,
                                 IItemHandler handler, IItemHandler upgradeHandler, ContainerData data) {
        super(ModMenuTypes.PHOTON_COMPRESSOR.get(), containerId);
        this.data = data;

        // Excited Photon input slot
        this.addSlot(new SlotItemHandler(handler, 0, 38, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.EXCITED_PHOTON.get());
            }
        });

        // Glowstone Dust input slot
        this.addSlot(new SlotItemHandler(handler, 1, 56, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GLOWSTONE_DUST);
            }
        });

        // Output slot
        this.addSlot(new SlotItemHandler(handler, 2, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Upgrade slots (right side column)
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            this.addSlot(new SlotItemHandler(upgradeHandler, i, 152, 8 + i * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return IUpgradeable.isUpgradeItem(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 32;
                }
            });
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
    public int getEnergy() { return data.get(2); }
    public int getMaxEnergy() { return data.get(3); }

    public float getProgressScaled() {
        int max = data.get(1);
        return max == 0 ? 0 : (float) data.get(0) / max;
    }

    public float getEnergyScaled() {
        int max = data.get(3);
        return max == 0 ? 0 : (float) data.get(2) / max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index < TE_SLOTS) {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (IUpgradeable.isUpgradeItem(slotStack)) {
                if (!this.moveItemStackTo(slotStack, MACHINE_SLOTS, TE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (slotStack.is(ModItems.EXCITED_PHOTON.get())) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (slotStack.is(Items.GLOWSTONE_DUST)) {
                if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < PLAYER_INV_START + 27) {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START + 27, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_START + 27, false)) {
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
