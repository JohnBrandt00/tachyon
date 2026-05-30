package com.setusertso.tachyon.screen;

import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.menu.OreCrusherMenu;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OreCrusherScreen extends AbstractContainerScreen<OreCrusherMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/ore_crusher.png");

    public OreCrusherScreen(OreCrusherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        float energyScaled = this.menu.getEnergyScaled();
        if (energyScaled > 0) {
            int barHeight = (int) (energyScaled * 52);
            graphics.blit(TEXTURE, this.leftPos + 10, this.topPos + 16 + 52 - barHeight,
                    176, 52 - barHeight, 16, barHeight);
        }

        float progressScaled = this.menu.getProgressScaled();
        if (progressScaled > 0) {
            int arrowWidth = (int) (progressScaled * 24);
            graphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 34,
                    176, 52, arrowWidth, 17);
        }

        UpgradeSlotRenderer.renderGhostUpgrades(graphics, this.menu, this.leftPos, this.topPos,
                2, 3, List.of(ModItems.SPEED_UPGRADE.get(), ModItems.ENERGY_UPGRADE.get(), ModItems.OUTPUT_UPGRADE.get()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(10, 16, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        }
    }
}
