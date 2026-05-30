package com.setusertso.tachyon.screen;

import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.menu.ThoriumReactorMenu;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ThoriumReactorScreen extends AbstractContainerScreen<ThoriumReactorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/thorium_reactor.png");

    public ThoriumReactorScreen(ThoriumReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Energy bar (left side, 16x52 area at x=10, y=16)
        float energyPct = this.menu.getEnergyProgress();
        int energyHeight = (int) (energyPct * 52);
        if (energyHeight > 0) {
            graphics.blit(TEXTURE, this.leftPos + 10, this.topPos + 16 + 52 - energyHeight,
                    176, 52 - energyHeight, 16, energyHeight);
        }

        // Flame animation when burning (above fuel slot)
        if (this.menu.isBurning()) {
            float progress = this.menu.getBurnProgress();
            int flameHeight = (int) Math.ceil(progress * 13) + 1;
            graphics.blit(TEXTURE, this.leftPos + 80, this.topPos + 28 + 14 - flameHeight,
                    176, 52 + 14 - flameHeight, 14, flameHeight);
        }

        UpgradeSlotRenderer.renderGhostUpgrades(graphics, this.menu, this.leftPos, this.topPos,
                1, 3, List.of(ModItems.SPEED_UPGRADE.get(), ModItems.ENERGY_UPGRADE.get()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw RF/t and burn time text
        int textX = this.leftPos + 30;
        int textY = this.topPos + 20;

        if (this.menu.isBurning()) {
            // RF/t output
            graphics.drawString(this.font, "500 RF/t", textX, textY, 0x40C040, false);

            // Burn time remaining
            int burnTicks = this.menu.getBurnTime();
            int seconds = burnTicks / 20;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            String timeStr = String.format("%d:%02d", minutes, seconds);
            graphics.drawString(this.font, timeStr, textX, textY + 10, 0xFFAA00, false);
        } else {
            graphics.drawString(this.font, "Idle", textX, textY, 0x888888, false);
        }

        // Energy stored
        String energyStr = formatEnergy(this.menu.getEnergyStored()) + " RF";
        graphics.drawString(this.font, energyStr, textX, textY + 40, 0x55FFFF, false);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private String formatEnergy(int rf) {
        if (rf >= 1_000_000) return String.format("%.1fM", rf / 1_000_000.0);
        if (rf >= 1_000) return String.format("%.1fk", rf / 1_000.0);
        return String.valueOf(rf);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
