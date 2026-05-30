package com.setusertso.tachyon.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class UpgradeSlotRenderer {
    private UpgradeSlotRenderer() {}

    /**
     * Renders ghost item hints in empty upgrade slots.
     * Each slot shows a different allowed upgrade icon (cycling through the list).
     */
    public static void renderGhostUpgrades(GuiGraphics graphics, AbstractContainerMenu menu,
                                            int leftPos, int topPos,
                                            int firstUpgradeSlot, int upgradeSlotCount,
                                            List<Item> allowedUpgrades) {
        if (allowedUpgrades.isEmpty()) return;

        for (int i = 0; i < upgradeSlotCount; i++) {
            int slotIndex = firstUpgradeSlot + i;
            if (slotIndex >= menu.slots.size()) break;

            Slot slot = menu.slots.get(slotIndex);
            if (slot.hasItem()) continue;

            Item ghostItem = allowedUpgrades.get(i % allowedUpgrades.size());
            ItemStack ghostStack = new ItemStack(ghostItem);

            int x = leftPos + slot.x;
            int y = topPos + slot.y;

            graphics.renderFakeItem(ghostStack, x, y);
            // Semi-transparent overlay to create the faded "ghost" look
            graphics.fill(x, y, x + 16, y + 16, 0xC0C6C6C6);
        }
    }
}
