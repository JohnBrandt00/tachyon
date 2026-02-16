package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.block.entity.EngineState;
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

public class SingularityControllerMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos controllerPos;

    // Client constructor (receives BlockPos from buffer)
    public SingularityControllerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, new ItemStackHandler(1), new SimpleContainerData(19), pos);
    }

    // Server constructor
    public SingularityControllerMenu(int containerId, Inventory playerInventory,
                                      IItemHandler handler, ContainerData data, BlockPos pos) {
        super(ModMenuTypes.SINGULARITY_CONTROLLER.get(), containerId);
        this.data = data;
        this.controllerPos = pos;

        // Output slot (Exotic Matter) — no input slot anymore
        this.addSlot(new SlotItemHandler(handler, 0, 116, 35) {
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

    // --- Data accessors (slots 0-17) ---

    public int getCraftProgress() { return data.get(0); }
    public int getCraftMaxProgress() { return data.get(1); }
    public int getRfStored() { return data.get(2); }
    public int getRfCapacity() { return data.get(3); }
    public int getBlackHoleEnergy() { return data.get(4); }
    public int getMaxBlackHoleEnergy() { return data.get(5); }
    public int getStability() { return data.get(6); }
    public int getMaxStability() { return data.get(7); }
    public int getActiveInjectors() { return data.get(8); }
    public int getCoreCount() { return data.get(9); }
    public EngineState getEngineState() { return EngineState.fromOrdinal(data.get(10)); }
    public int getBlackHoleScaleRaw() { return data.get(11); }
    public int getHawkingRf() { return data.get(12); }
    public int getTidalStress() { return data.get(13); }
    public int getNetStability() { return data.get(14); }
    public int getPhotonRate() { return data.get(15); }
    public int getShieldPower() { return data.get(16); }
    public boolean isCrafting() { return data.get(17) == 1; }
    public int getTemperature() { return data.get(18); }
    public BlockPos getControllerPos() { return controllerPos; }

    public float getCraftProgressScaled() {
        int max = data.get(1);
        return max == 0 ? 0 : (float) data.get(0) / max;
    }

    public float getRfScaled() {
        int max = data.get(3);
        return max == 0 ? 0 : (float) data.get(2) / max;
    }

    public float getBlackHoleEnergyScaled() {
        int max = data.get(5);
        return max == 0 ? 0 : (float) data.get(4) / max;
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

            // From machine slot (0) to player inventory (1-36)
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
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
