package com.setusertso.tachyon.screen;

import com.setusertso.tachyon.menu.SingularityControllerMenu;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SingularityControllerScreen extends AbstractContainerScreen<SingularityControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/singularity_controller.png");

    public SingularityControllerScreen(SingularityControllerMenu menu, Inventory playerInventory, Component title) {
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

        // Stability bar (center-left, 10x52 pixels, position: 30, 16)
        float stabilityScaled = this.menu.getStabilityScaled();
        if (stabilityScaled > 0) {
            int barHeight = (int) (stabilityScaled * 52);
            // Color gradient: green at top, red at bottom (encoded in texture UV)
            graphics.blit(TEXTURE, this.leftPos + 30, this.topPos + 16 + 52 - barHeight,
                    192, 52 - barHeight, 10, barHeight);
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

        // Info text
        int textX = this.leftPos + 44;
        int textY = this.topPos + 58;
        int color = 0x404040;

        double stability = this.menu.getStability() / 100.0;
        graphics.drawString(this.font, String.format("Stability: %.1f%%", stability), textX, textY, color, false);
        graphics.drawString(this.font, "Cores: " + this.menu.getCoreCount(), textX, textY + 10, color, false);
        graphics.drawString(this.font, "Injectors: " + this.menu.getActiveInjectors(), textX + 60, textY + 10, color, false);
        graphics.drawString(this.font, "Light: " + this.menu.getLightBuffer() + "/" + this.menu.getMaxLightBuffer(),
                textX, textY + 20, color, false);

        this.renderTooltip(graphics, mouseX, mouseY);

        // Energy tooltip
        if (isHovering(10, 16, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        }

        // Stability tooltip
        if (isHovering(30, 16, 10, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(String.format("Stability: %.1f%%", stability)),
                    mouseX, mouseY);
        }
    }
}
