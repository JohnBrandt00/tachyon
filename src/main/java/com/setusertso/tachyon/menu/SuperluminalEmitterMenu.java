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

public class SuperluminalEmitterMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Client constructor
    public SuperluminalEmitterMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(4), new SimpleContainerData(2));
    }

    // Server constructor
    public SuperluminalEmitterMenu(int containerId, Inventory playerInventory,
                                    IItemHandler handler, ContainerData data) {
        super(ModMenuTypes.SUPERLUMINAL_EMITTER.get(), containerId);
        this.data = data;

        // Fuel slot (center of GUI)
        this.addSlot(new SlotItemHandler(handler, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.TACHYON_SHARD.get());
            }
        });

        // 3 locked upgrade slots (greyed out)
        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotItemHandler(handler, 1 + i, 26 + i * 27, 62) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean isActive() {
                    return false;
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

    public int getLitTime() {
        return data.get(0);
    }

    public int getLitDuration() {
        return data.get(1);
    }

    public boolean isLit() {
        return data.get(0) > 0;
    }

    public float getLitProgress() {
        int duration = data.get(1);
        if (duration == 0) return 0;
        return (float) data.get(0) / duration;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From fuel/upgrade slots (0-3) to player inventory (4-39)
            if (index < 4) {
                if (!this.moveItemStackTo(slotStack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to fuel slot
            else if (slotStack.is(ModItems.TACHYON_SHARD.get())) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Between inventory and hotbar
            else if (index < 31) {
                if (!this.moveItemStackTo(slotStack, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 4, 31, false)) {
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
