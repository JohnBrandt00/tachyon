package com.setusertso.tachyon.screen;

import com.setusertso.tachyon.block.entity.PortMode;
import com.setusertso.tachyon.menu.SingularityPortMenu;
import com.setusertso.tachyon.network.CyclePortModePacket;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SingularityPortScreen extends AbstractContainerScreen<SingularityPortMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/accelerator_port.png");

    public SingularityPortScreen(SingularityPortMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelY = 6;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.tachyon.cycle_mode"),
                button -> PacketDistributor.sendToServer(new CyclePortModePacket(this.menu.getPortPos()))
        ).bounds(this.leftPos + 58, this.topPos + 56, 60, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        PortMode mode = this.menu.getMode();
        Component modeText = Component.translatable("gui.tachyon.port_mode." + mode.getSerializedName());
        int textWidth = this.font.width(modeText);
        graphics.drawString(this.font, modeText,
                this.leftPos + (this.imageWidth - textWidth) / 2, this.topPos + 42, 0xFFFFFF);

        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
