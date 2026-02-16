package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.init.ModMenuTypes;

import net.minecraft.core.BlockPos;
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

public class PhotonicInjectorMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos injectorPos;

    // Client constructor (from IMenuTypeExtension)
    public PhotonicInjectorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, pos, new ItemStackHandler(1), new SimpleContainerData(2));
    }

    // Server constructor
    public PhotonicInjectorMenu(int containerId, Inventory playerInventory, PhotonicInjectorBlockEntity be) {
        this(containerId, playerInventory, be.getBlockPos(), be.getItems(), new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.isActive() ? 1 : 0;
                    case 1 -> be.getInjectionRate();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    private PhotonicInjectorMenu(int containerId, Inventory playerInventory, BlockPos pos,
                                  IItemHandler handler, ContainerData data) {
        super(ModMenuTypes.PHOTONIC_INJECTOR.get(), containerId);
        this.data = data;
        this.injectorPos = pos;

        // Fuel slot (Condensed Light)
        this.addSlot(new SlotItemHandler(handler, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CONDENSED_LIGHT.get()) || stack.is(ModItems.ANTIMATTER_NEUTRALIZER.get());
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

    public boolean isActive() {
        return data.get(0) == 1;
    }

    public int getInjectionRate() {
        return data.get(1);
    }

    public BlockPos getInjectorPos() {
        return injectorPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.is(ModItems.CONDENSED_LIGHT.get()) || slotStack.is(ModItems.ANTIMATTER_NEUTRALIZER.get())) {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 28) {
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
