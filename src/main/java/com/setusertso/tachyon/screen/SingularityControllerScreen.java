package com.setusertso.tachyon.screen;

import com.setusertso.tachyon.block.entity.EngineState;
import com.setusertso.tachyon.menu.SingularityControllerMenu;
import com.setusertso.tachyon.network.ReformStructurePacket;
import com.setusertso.tachyon.tachyon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SingularityControllerScreen extends AbstractContainerScreen<SingularityControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "textures/gui/singularity_controller.png");

    private Button reformButton;

    public SingularityControllerScreen(SingularityControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void init() {
        super.init();

        reformButton = Button.builder(
                Component.translatable("gui.tachyon.reform_structure"),
                btn -> {
                    PacketDistributor.sendToServer(new ReformStructurePacket(this.menu.getControllerPos()));
                })
                .bounds(this.leftPos + 60, this.topPos + 4, 56, 12)
                .build();
        reformButton.visible = false;
        this.addRenderableWidget(reformButton);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        EngineState state = this.menu.getEngineState();
        reformButton.visible = (state == EngineState.NEUTRALIZED);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // RF buffer bar (left side, 16x52 pixels, position: 10, 16)
        float rfScaled = this.menu.getRfScaled();
        if (rfScaled > 0) {
            int barHeight = (int) (rfScaled * 52);
            graphics.blit(TEXTURE, this.leftPos + 10, this.topPos + 16 + 52 - barHeight,
                    176, 52 - barHeight, 16, barHeight);
        }

        // Stability bar (center-left, 10x52 pixels, position: 30, 16)
        float stabilityScaled = this.menu.getStabilityScaled();
        if (stabilityScaled > 0) {
            int barHeight = (int) (stabilityScaled * 52);
            graphics.blit(TEXTURE, this.leftPos + 30, this.topPos + 16 + 52 - barHeight,
                    192, 52 - barHeight, 10, barHeight);
        }

        // Progress arrow (between slots, 24x17 pixels, position: 79, 34)
        float progressScaled = Math.min(1.0f, this.menu.getCraftProgressScaled());
        if (progressScaled > 0) {
            int arrowWidth = (int) (progressScaled * 24);
            graphics.blit(TEXTURE, this.leftPos + 79, this.topPos + 34,
                    176, 52, arrowWidth, 17);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Override to draw custom title instead of default
        EngineState engineState = this.menu.getEngineState();
        switch (engineState) {
            case NORMAL -> graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x40C040, false);
            case MELTDOWN -> {
                long time = System.currentTimeMillis();
                boolean flash = (time / 300) % 2 == 0;
                int color = flash ? 0xFF2020 : 0xAA0000;
                graphics.drawString(this.font, "MELTDOWN", this.titleLabelX, this.titleLabelY, color, false);
            }
            case NEUTRALIZED -> graphics.drawString(this.font, "Neutralized", this.titleLabelX, this.titleLabelY, 0x808080, false);
        }
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int color = 0x404040;

        // Row 1: Mass + RF output
        int textX = this.leftPos + 44;
        int y1 = this.topPos + 53;
        double bhEnergy = this.menu.getBlackHoleEnergy() / 10.0;
        double bhMax = this.menu.getMaxBlackHoleEnergy() / 10.0;
        double stability = this.menu.getStability() / 100.0;
        graphics.drawString(this.font, String.format("Mass: %.0f/%.0f", bhEnergy, bhMax), textX, y1, color, false);

        // Row 2: Stability + Crafting indicator + RF output
        int y2 = y1 + 9;
        String stabText = String.format("Stab: %.1f%%", stability);
        graphics.drawString(this.font, stabText, textX, y2, color, false);
        int stabWidth = this.font.width(stabText);
        if (this.menu.isCrafting()) {
            graphics.drawString(this.font, "*", textX + stabWidth + 1, y2, 0xC040FF, false);
        }
        int hawkingRf = this.menu.getHawkingRf();
        graphics.drawString(this.font, formatRf(hawkingRf) + "/t", textX + stabWidth + 8, y2, 0x40C040, false);

        // Row 3: Temperature + Tidal (if active)
        int y3 = y2 + 9;
        int temp = this.menu.getTemperature();
        int tempColor = temp > 7000 ? 0xFF2020 : (temp > 4000 ? 0xFF8800 : (temp > 2000 ? 0xFFCC00 : color));
        graphics.drawString(this.font, String.format("Temp: %d", temp), textX, y3, tempColor, false);
        int tidalStress = this.menu.getTidalStress();
        if (tidalStress > 100) {
            int tidalColor = tidalStress > 150 ? 0xFF4040 : 0xFFAA00;
            graphics.drawString(this.font, "T:" + tidalStress + "%", textX + 58, y3, tidalColor, false);
        } else {
            graphics.drawString(this.font, "I:" + this.menu.getActiveInjectors() + " C:" + this.menu.getCoreCount(),
                    textX + 58, y3, color, false);
        }

        this.renderTooltip(graphics, mouseX, mouseY);

        // RF buffer tooltip
        if (isHovering(10, 16, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(formatRf(this.menu.getRfStored()) + " / " + formatRf(this.menu.getRfCapacity())),
                    mouseX, mouseY);
        }

        // Stability tooltip
        if (isHovering(30, 16, 10, 52, mouseX, mouseY)) {
            double netStab = this.menu.getNetStability() / 10000.0;
            String sign = netStab >= 0 ? "+" : "";
            graphics.renderTooltip(this.font,
                    Component.literal(String.format("Stability: %.1f%% (%s%.4f/t)", stability, sign, netStab)),
                    mouseX, mouseY);
        }
    }

    private static String formatRf(int rf) {
        if (rf >= 1_000_000) {
            return String.format("%.1fM RF", rf / 1_000_000.0);
        } else if (rf >= 1_000) {
            return String.format("%.1fk RF", rf / 1_000.0);
        } else {
            return rf + " RF";
        }
    }
}
