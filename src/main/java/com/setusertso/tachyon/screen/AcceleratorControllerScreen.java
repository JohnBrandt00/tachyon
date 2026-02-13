package com.setusertso.tachyon.screen;

import com.setusertso.tachyon.init.ModFluids;
import com.setusertso.tachyon.menu.AcceleratorControllerMenu;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class AcceleratorControllerScreen extends AbstractContainerScreen<AcceleratorControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/accelerator_controller.png");

    public AcceleratorControllerScreen(AcceleratorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Energy bar (left side, 16x52 pixels, position: 10, 16)
        float energyScaled = this.menu.getEnergyScaled();
        if (energyScaled > 0) {
            int barHeight = (int) (energyScaled * 52);
            graphics.blit(TEXTURE, this.leftPos + 10, this.topPos + 16 + 52 - barHeight,
                    176, 52 - barHeight, 16, barHeight);
        }

        // Fluid tank (right side, 16x52 pixels, position: 150, 16)
        float fluidScaled = this.menu.getFluidScaled();
        if (fluidScaled > 0) {
            int barHeight = (int) (fluidScaled * 52);

            // Get helium fluid texture
            var fluidExtensions = IClientFluidTypeExtensions.of(ModFluids.HELIUM_SOURCE.get());
            ResourceLocation stillTexture = fluidExtensions.getStillTexture();
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(stillTexture);

            // Get fluid color
            int color = fluidExtensions.getTintColor();
            float red = ((color >> 16) & 0xFF) / 255f;
            float green = ((color >> 8) & 0xFF) / 255f;
            float blue = (color & 0xFF) / 255f;

            // Render fluid texture
            int tankX = this.leftPos + 150;
            int tankY = this.topPos + 16 + 52 - barHeight;

            // Tile the texture to fill the bar height
            for (int i = 0; i < barHeight; i += 16) {
                int renderHeight = Math.min(16, barHeight - i);
                graphics.setColor(red, green, blue, 1.0f);
                graphics.blit(tankX, tankY + i, 0, 16, renderHeight, sprite);
            }
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        // Progress arrow (between slots, 24x17 pixels, position: 79, 34)
        float progressScaled = this.menu.getProgressScaled();
        if (progressScaled > 0) {
            int arrowWidth = (int) (progressScaled * 24);
            graphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 34,
                    176, 52, arrowWidth, 17);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Energy tooltip
        if (isHovering(10, 16, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        }

        // Fluid tooltip
        if (isHovering(150, 16, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getFluidAmount() + " / " + this.menu.getFluidCapacity() + " mB"),
                    mouseX, mouseY);
        }

        // Momentum tooltip (progress arrow area)
        if (isHovering(79, 34, 24, 17, mouseX, mouseY)) {
            float momentumPercent = this.menu.getMomentumScaled() * 100;
            int itemsProcessed = this.menu.getMomentum() / 1000;
            graphics.renderTooltip(this.font,
                    Component.literal(String.format("Momentum: %.0f%% (%d/5 items)", momentumPercent, itemsProcessed)),
                    mouseX, mouseY);
        }
    }
}
