package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.ModItems;
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

public class SingularityControllerMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Client constructor
    public SingularityControllerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(2), new SimpleContainerData(10));
    }

    // Server constructor
    public SingularityControllerMenu(int containerId, Inventory playerInventory,
                                      IItemHandler handler, ContainerData data) {
        super(ModMenuTypes.SINGULARITY_CONTROLLER.get(), containerId);
        this.data = data;

        // Input slot (Condensed Light)
        this.addSlot(new SlotItemHandler(handler, 0, 56, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CONDENSED_LIGHT.get());
            }
        });

        // Output slot (Exotic Matter)
        this.addSlot(new SlotItemHandler(handler, 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

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
    public int getLightBuffer() { return data.get(4); }
    public int getMaxLightBuffer() { return data.get(5); }
    public int getStability() { return data.get(6); }
    public int getMaxStability() { return data.get(7); }
    public int getActiveInjectors() { return data.get(8); }
    public int getCoreCount() { return data.get(9); }

    public float getProgressScaled() {
        int max = data.get(1);
        return max == 0 ? 0 : (float) data.get(0) / max;
    }

    public float getEnergyScaled() {
        int max = data.get(3);
        return max == 0 ? 0 : (float) data.get(2) / max;
    }

    public float getStabilityScaled() {
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

            // From machine slots (0-1) to player inventory (2-37)
            if (index < 2) {
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to input slot (condensed light only)
            else if (slotStack.is(ModItems.CONDENSED_LIGHT.get())) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Between inventory and hotbar
            else if (index < 29) {
                if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
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
