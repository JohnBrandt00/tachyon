package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.entity.IUpgradeable;
import com.setusertso.tachyon.init.ModMenuTypes;
import com.setusertso.tachyon.init.ModTags;

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

public class AcceleratorControllerMenu extends AbstractContainerMenu {
    private final ContainerData data;

    private static final int MACHINE_SLOTS = 2;
    private static final int UPGRADE_SLOTS = 3;
    private static final int TE_SLOTS = MACHINE_SLOTS + UPGRADE_SLOTS; // 5
    private static final int PLAYER_INV_START = TE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36; // 41

    // Client constructor
    public AcceleratorControllerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(2), new ItemStackHandler(3), new SimpleContainerData(8));
    }

    // Server constructor
    public AcceleratorControllerMenu(int containerId, Inventory playerInventory,
                                      IItemHandler handler, IItemHandler upgradeHandler, ContainerData data) {
        super(ModMenuTypes.ACCELERATOR_CONTROLLER.get(), containerId);
        this.data = data;

        // Input slot (thorium ingot, ender pearl, or raw photon)
        this.addSlot(new SlotItemHandler(handler, 0, 56, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.INGOTS_THORIUM) || stack.is(Items.ENDER_PEARL)
                        || stack.is(ModItems.RAW_PHOTON.get());
            }
        });

        // Output slot
        this.addSlot(new SlotItemHandler(handler, 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Upgrade slots (horizontal row below machine area)
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            this.addSlot(new SlotItemHandler(upgradeHandler, i, 62 + i * 18, 62) {
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

        // Player inventory (3 rows)
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
    public int getFluidAmount() { return data.get(4); }
    public int getFluidCapacity() { return data.get(5); }
    public int getMomentum() { return data.get(6); }
    public int getMaxMomentum() { return data.get(7); }

    public float getProgressScaled() {
        int max = data.get(1);
        return max == 0 ? 0 : (float) data.get(0) / max;
    }

    public float getEnergyScaled() {
        int max = data.get(3);
        return max == 0 ? 0 : (float) data.get(2) / max;
    }

    public float getFluidScaled() {
        int max = data.get(5);
        return max == 0 ? 0 : (float) data.get(4) / max;
    }

    public float getMomentumScaled() {
        int max = data.get(7);
        return max == 0 ? 0 : (float) data.get(6) / max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From machine/upgrade slots to player inventory
            if (index < TE_SLOTS) {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory: try upgrade slots first, then input slot
            else if (IUpgradeable.isUpgradeItem(slotStack)) {
                if (!this.moveItemStackTo(slotStack, MACHINE_SLOTS, TE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (slotStack.is(ModTags.Items.INGOTS_THORIUM) || slotStack.is(Items.ENDER_PEARL)
                    || slotStack.is(ModItems.RAW_PHOTON.get())) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Between inventory and hotbar
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
