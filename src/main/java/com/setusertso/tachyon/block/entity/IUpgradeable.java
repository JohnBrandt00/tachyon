package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public interface IUpgradeable {
    int UPGRADE_SLOT_COUNT = 3;

    ItemStackHandler getUpgradeHandler();

    default int countUpgrade(Item upgradeItem) {
        ItemStackHandler handler = getUpgradeHandler();
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(upgradeItem)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    default float getSpeedMultiplier() {
        return 1.0f + 0.05f * countUpgrade(ModItems.SPEED_UPGRADE.get());
    }

    default float getEnergyMultiplier() {
        int count = countUpgrade(ModItems.ENERGY_UPGRADE.get());
        return 1.0f / (1.0f + 0.05f * count);
    }

    default float getOutputChance() {
        return Math.min(0.9f, 0.02f * countUpgrade(ModItems.OUTPUT_UPGRADE.get()));
    }

    default float getCapacityMultiplier() {
        return 1.0f + 0.1f * countUpgrade(ModItems.CAPACITY_UPGRADE.get());
    }

    static boolean isUpgradeItem(ItemStack stack) {
        return stack.is(ModItems.SPEED_UPGRADE.get())
                || stack.is(ModItems.ENERGY_UPGRADE.get())
                || stack.is(ModItems.OUTPUT_UPGRADE.get())
                || stack.is(ModItems.CAPACITY_UPGRADE.get());
    }

    static ItemStackHandler createUpgradeHandler(Runnable onChange) {
        return new ItemStackHandler(UPGRADE_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                onChange.run();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isUpgradeItem(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 32;
            }
        };
    }
}
