package com.setusertso.tachyon.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import org.joml.Matrix4f;

/**
 * Renders a fluid tank bar in a GUI, filling from bottom to top.
 * Uses manual vertex buffer rendering following Mekanism's proven approach.
 */
public final class FluidTankRenderer {
    private FluidTankRenderer() {}

    public static void renderFluidTank(GuiGraphics graphics, Fluid fluid, int x, int y, int width, int height, float fillAmount) {
        if (fillAmount <= 0) return;

        var fluidExtensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation stillTexture = fluidExtensions.getStillTexture();
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        int color = fluidExtensions.getTintColor();
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a == 0) a = 1.0f; // Default to opaque if alpha not set
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int barHeight = (int) (fillAmount * height);
        if (barHeight <= 0) return;

        int tankBottom = y + height;

        // Apply fluid tint via shader color
        RenderSystem.setShaderColor(r, g, b, a);

        // Explicit render setup (Mekanism's approach)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        float uDif = uMax - uMin;
        float vDif = vMax - vMin;

        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = graphics.pose().last().pose();

        int drawn = 0;
        while (drawn < barHeight) {
            int tileH = Math.min(16, barHeight - drawn);
            int drawY = tankBottom - drawn - tileH;
            int maskTop = 16 - tileH;
            float vLocalMin = vMin + (vDif * maskTop / 16f);

            int drawnX = 0;
            while (drawnX < width) {
                int tileW = Math.min(16, width - drawnX);
                int maskRight = 16 - tileW;
                float uLocalMax = uMax - (uDif * maskRight / 16f);

                buffer.addVertex(matrix, x + drawnX, drawY + tileH, 0).setUv(uMin, vMax);
                buffer.addVertex(matrix, x + drawnX + tileW, drawY + tileH, 0).setUv(uLocalMax, vMax);
                buffer.addVertex(matrix, x + drawnX + tileW, drawY, 0).setUv(uLocalMax, vLocalMin);
                buffer.addVertex(matrix, x + drawnX, drawY, 0).setUv(uMin, vLocalMin);

                drawnX += tileW;
            }
            drawn += tileH;
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
