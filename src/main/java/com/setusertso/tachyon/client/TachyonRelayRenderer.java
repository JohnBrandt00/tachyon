package com.setusertso.tachyon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.setusertso.tachyon.block.entity.TachyonRelayBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class TachyonRelayRenderer implements BlockEntityRenderer<TachyonRelayBlockEntity> {

    public TachyonRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TachyonRelayBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Wireless transfer particles are now spawned server-side in serverTick
    }
}
