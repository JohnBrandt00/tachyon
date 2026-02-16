package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.block.entity.SingularityPortBlockEntity;
import com.setusertso.tachyon.block.entity.PortMode;
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

public class SingularityPortMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos portPos;

    // Client constructor (from IMenuTypeExtension)
    public SingularityPortMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, pos, new ItemStackHandler(1), new SimpleContainerData(2));
    }

    // Server constructor
    public SingularityPortMenu(int containerId, Inventory playerInventory, SingularityPortBlockEntity be) {
        this(containerId, playerInventory, be.getBlockPos(), be.getItems(), new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getMode().ordinal();
                    case 1 -> be.getShieldPowerRate();
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

    private SingularityPortMenu(int containerId, Inventory playerInventory, BlockPos pos,
                                 IItemHandler handler, ContainerData data) {
        super(ModMenuTypes.SINGULARITY_PORT.get(), containerId);
        this.portPos = pos;
        this.data = data;

        // Port I/O slot (slot 0)
        this.addSlot(new SlotItemHandler(handler, 0, 80, 20));

        // Player inventory (3 rows) - slots 1-27
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar - slots 28-36
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    public BlockPos getPortPos() {
        return portPos;
    }

    public PortMode getMode() {
        return PortMode.fromOrdinal(data.get(0));
    }

    public int getShieldPowerRate() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From port slot (0) to player inventory (1-36)
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to port slot
            else {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // Between inventory and hotbar
                    if (index < 28) {
                        if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
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
