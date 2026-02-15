package com.setusertso.tachyon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.block.entity.PhotonicInjectorBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

public class PhotonicInjectorRenderer implements BlockEntityRenderer<PhotonicInjectorBlockEntity> {

    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    private static final float BEAM_WIDTH = 0.15f;

    public PhotonicInjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PhotonicInjectorBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isActive()) return;

        Direction facing = be.getBlockState().getValue(PhotonicInjectorBlock.FACING);

        // Render a simple glowing beam in the facing direction
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        // Direction vector
        float dx = facing.getStepX();
        float dy = facing.getStepY();
        float dz = facing.getStepZ();

        // Beam length
        float beamLength = 16.0f;

        // Draw 4-sided beam along the facing direction
        float hw = BEAM_WIDTH; // half-width

        // Two perpendicular axes to the beam direction
        float ax, ay, az, bx, by, bz;
        if (Math.abs(dy) > 0.9f) {
            ax = hw; ay = 0; az = 0;
            bx = 0; by = 0; bz = hw;
        } else if (Math.abs(dx) > 0.9f) {
            ax = 0; ay = hw; az = 0;
            bx = 0; by = 0; bz = hw;
        } else {
            ax = hw; ay = 0; az = 0;
            bx = 0; by = hw; bz = 0;
        }

        float ex = dx * beamLength;
        float ey = dy * beamLength;
        float ez = dz * beamLength;

        // Color: bright white/cyan
        int r = 230, g = 230, b = 255, a = 180;

        // Side 1 (using axis a)
        consumer.addVertex(matrix, -ax, -ay, -az).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, ax, ay, az).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, ax + ex, ay + ey, az + ez).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        consumer.addVertex(matrix, -ax, -ay, -az).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, ax + ex, ay + ey, az + ez).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, -ax + ex, -ay + ey, -az + ez).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        // Side 2 (using axis b)
        consumer.addVertex(matrix, -bx, -by, -bz).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, bx, by, bz).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, bx + ex, by + ey, bz + ez).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        consumer.addVertex(matrix, -bx, -by, -bz).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, bx + ex, by + ey, bz + ez).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, -bx + ex, -by + ey, -bz + ez).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(PhotonicInjectorBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        return new AABB(pos.getX() - 16, pos.getY() - 16, pos.getZ() - 16,
                         pos.getX() + 17, pos.getY() + 17, pos.getZ() + 17);
    }

    @Override
    public boolean shouldRenderOffScreen(PhotonicInjectorBlockEntity be) {
        return true;
    }
}
