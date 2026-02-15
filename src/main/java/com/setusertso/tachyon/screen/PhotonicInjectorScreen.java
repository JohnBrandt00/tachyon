package com.setusertso.tachyon.screen;

import com.setusertso.tachyon.block.entity.PhotonicInjectorMenu;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PhotonicInjectorScreen extends AbstractContainerScreen<PhotonicInjectorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/photonic_injector.png");

    public PhotonicInjectorScreen(PhotonicInjectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Status text
        String status = this.menu.isActive() ? "Active" : "Inactive";
        int color = this.menu.isActive() ? 0x00FF00 : 0xFF0000;
        graphics.drawString(this.font, status, this.leftPos + 72, this.topPos + 58, color, false);

        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
