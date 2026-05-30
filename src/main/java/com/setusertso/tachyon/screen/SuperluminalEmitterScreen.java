package com.setusertso.tachyon.screen;

import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.menu.SuperluminalEmitterMenu;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SuperluminalEmitterScreen extends AbstractContainerScreen<SuperluminalEmitterMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/superluminal_emitter.png");

    public SuperluminalEmitterScreen(SuperluminalEmitterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Flame animation when burning
        if (this.menu.isLit()) {
            float progress = this.menu.getLitProgress();
            int flameHeight = (int) Math.ceil(progress * 13) + 1;
            // Draw flame from bottom up (source: 176,0 in texture, 14x14 flame sprite)
            graphics.blit(TEXTURE, this.leftPos + 80, this.topPos + 21 + 14 - flameHeight,
                    176, 14 - flameHeight, 14, flameHeight);
        }

        UpgradeSlotRenderer.renderGhostUpgrades(graphics, this.menu, this.leftPos, this.topPos,
                1, 3, List.of(ModItems.SPEED_UPGRADE.get(), ModItems.ENERGY_UPGRADE.get()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
