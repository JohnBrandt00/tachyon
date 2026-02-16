package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.entity.ThoriumReactorBlockEntity;
import com.setusertso.tachyon.init.ModMenuTypes;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ThoriumReactorMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Client constructor
    public ThoriumReactorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(1), new SimpleContainerData(4));
    }

    // Server constructor (from block entity)
    public ThoriumReactorMenu(int containerId, Inventory playerInventory, ThoriumReactorBlockEntity be) {
        this(containerId, playerInventory, be.getItems(), be.getDataAccess());
    }

    // Main constructor
    public ThoriumReactorMenu(int containerId, Inventory playerInventory,
                              net.neoforged.neoforge.items.IItemHandler handler, ContainerData data) {
        super(ModMenuTypes.THORIUM_REACTOR.get(), containerId);
        this.data = data;

        // Fuel slot (center of GUI)
        this.addSlot(new SlotItemHandler(handler, 0, 80, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.THORIUM_INGOT.get());
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

    public int getBurnTime() {
        return data.get(0);
    }

    public int getMaxBurnTime() {
        return data.get(1);
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    public float getBurnProgress() {
        int max = data.get(1);
        if (max == 0) return 0;
        return (float) data.get(0) / max;
    }

    public int getEnergyStored() {
        return data.get(2);
    }

    public int getEnergyCapacity() {
        return data.get(3);
    }

    public float getEnergyProgress() {
        int max = data.get(3);
        if (max == 0) return 0;
        return (float) data.get(2) / max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From fuel slot (0) to player inventory (1-36)
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to fuel slot
            else if (slotStack.is(ModItems.THORIUM_INGOT.get())) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Between inventory and hotbar
            else if (index < 28) {
                if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
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
