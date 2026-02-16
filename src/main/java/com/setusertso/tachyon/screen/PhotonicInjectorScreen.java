package com.setusertso.tachyon.screen;

import com.setusertso.tachyon.block.entity.PhotonicInjectorMenu;
import com.setusertso.tachyon.network.SetInjectorRatePacket;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class PhotonicInjectorScreen extends AbstractContainerScreen<PhotonicInjectorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/photonic_injector.png");

    public PhotonicInjectorScreen(PhotonicInjectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // Rate buttons: -1, -0.1, -0.01, +0.01, +0.1, +1
        int btnY = this.topPos + 55;
        this.addRenderableWidget(Button.builder(Component.literal("-1"),
                b -> sendRate(-100)).bounds(this.leftPos + 8, btnY, 24, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("-.1"),
                b -> sendRate(-10)).bounds(this.leftPos + 34, btnY, 24, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("-.01"),
                b -> sendRate(-1)).bounds(this.leftPos + 60, btnY, 28, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("+.01"),
                b -> sendRate(1)).bounds(this.leftPos + 90, btnY, 28, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("+.1"),
                b -> sendRate(10)).bounds(this.leftPos + 120, btnY, 24, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"),
                b -> sendRate(100)).bounds(this.leftPos + 146, btnY, 24, 14).build());
    }

    private void sendRate(int delta) {
        int current = this.menu.getInjectionRate();
        int newRate = Math.max(1, Math.min(1000, current + delta));
        PacketDistributor.sendToServer(new SetInjectorRatePacket(this.menu.getInjectorPos(), newRate));
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
        graphics.drawString(this.font, status, this.leftPos + 72, this.topPos + 22, color, false);

        // Rate display (hundredths -> decimal)
        double rate = this.menu.getInjectionRate() / 100.0;
        String rateText = String.format("Rate: %.2f/t", rate);
        int rateWidth = this.font.width(rateText);
        graphics.drawString(this.font, rateText,
                this.leftPos + (this.imageWidth - rateWidth) / 2, this.topPos + 44, 0x404040, false);

        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
