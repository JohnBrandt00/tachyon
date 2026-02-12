package com.setusertso.tachyon.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.setusertso.tachyon.block.entity.AcceleratorControllerBlockEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;


public class AcceleratorControllerRenderer implements BlockEntityRenderer<AcceleratorControllerBlockEntity> {

    private static final float ORBIT_TICKS = 80.0f; // 4 seconds per full orbit
    private static final float PARTICLE_SIZE = 0.3f;
    // Cyan glow color
    private static final float R = 0.0f, G = 0.9f, B = 1.0f, A = 0.8f;

    public AcceleratorControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AcceleratorControllerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isFormed() || be.getProgress() <= 0) return;

        List<BlockPos> ringPath = be.getRingPath();
        if (ringPath.isEmpty()) return;

        // Calculate position along the ring path
        float gameTime = be.getLevel().getGameTime() + partialTick;
        float normalizedTime = (gameTime % ORBIT_TICKS) / ORBIT_TICKS;
        float pathIndex = normalizedTime * ringPath.size();

        int idx0 = (int) pathIndex % ringPath.size();
        int idx1 = (idx0 + 1) % ringPath.size();
        float fraction = pathIndex - (int) pathIndex;

        BlockPos p0 = ringPath.get(idx0);
        BlockPos p1 = ringPath.get(idx1);

        // Interpolate position (centered on blocks)
        double px = p0.getX() + 0.5 + (p1.getX() - p0.getX()) * fraction;
        double py = p0.getY() + 0.5 + (p1.getY() - p0.getY()) * fraction;
        double pz = p0.getZ() + 0.5 + (p1.getZ() - p0.getZ()) * fraction;

        // Convert to relative coordinates (renderer is at block entity position)
        double relX = px - be.getBlockPos().getX();
        double relY = py - be.getBlockPos().getY();
        double relZ = pz - be.getBlockPos().getZ();

        poseStack.pushPose();
        poseStack.translate(relX, relY, relZ);

        // Render a small glowing cube
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        renderGlowingCube(poseStack, consumer, PARTICLE_SIZE, packedOverlay);

        poseStack.popPose();
    }

    private void renderGlowingCube(PoseStack poseStack, VertexConsumer consumer, float size, int packedOverlay) {
        float half = size / 2.0f;
        PoseStack.Pose pose = poseStack.last();
        int light = LightTexture.FULL_BRIGHT;

        // Each face: 2 triangles = 4 vertices (quads)
        // Bottom face (y = -half)
        vertex(pose, consumer, -half, -half, -half, 0, -1, 0, light, packedOverlay);
        vertex(pose, consumer,  half, -half, -half, 0, -1, 0, light, packedOverlay);
        vertex(pose, consumer,  half, -half,  half, 0, -1, 0, light, packedOverlay);
        vertex(pose, consumer, -half, -half,  half, 0, -1, 0, light, packedOverlay);

        // Top face (y = +half)
        vertex(pose, consumer, -half, half,  half, 0, 1, 0, light, packedOverlay);
        vertex(pose, consumer,  half, half,  half, 0, 1, 0, light, packedOverlay);
        vertex(pose, consumer,  half, half, -half, 0, 1, 0, light, packedOverlay);
        vertex(pose, consumer, -half, half, -half, 0, 1, 0, light, packedOverlay);

        // North face (z = -half)
        vertex(pose, consumer,  half, -half, -half, 0, 0, -1, light, packedOverlay);
        vertex(pose, consumer, -half, -half, -half, 0, 0, -1, light, packedOverlay);
        vertex(pose, consumer, -half,  half, -half, 0, 0, -1, light, packedOverlay);
        vertex(pose, consumer,  half,  half, -half, 0, 0, -1, light, packedOverlay);

        // South face (z = +half)
        vertex(pose, consumer, -half, -half, half, 0, 0, 1, light, packedOverlay);
        vertex(pose, consumer,  half, -half, half, 0, 0, 1, light, packedOverlay);
        vertex(pose, consumer,  half,  half, half, 0, 0, 1, light, packedOverlay);
        vertex(pose, consumer, -half,  half, half, 0, 0, 1, light, packedOverlay);

        // West face (x = -half)
        vertex(pose, consumer, -half, -half, -half, -1, 0, 0, light, packedOverlay);
        vertex(pose, consumer, -half, -half,  half, -1, 0, 0, light, packedOverlay);
        vertex(pose, consumer, -half,  half,  half, -1, 0, 0, light, packedOverlay);
        vertex(pose, consumer, -half,  half, -half, -1, 0, 0, light, packedOverlay);

        // East face (x = +half)
        vertex(pose, consumer, half, -half,  half, 1, 0, 0, light, packedOverlay);
        vertex(pose, consumer, half, -half, -half, 1, 0, 0, light, packedOverlay);
        vertex(pose, consumer, half,  half, -half, 1, 0, 0, light, packedOverlay);
        vertex(pose, consumer, half,  half,  half, 1, 0, 0, light, packedOverlay);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer consumer,
                        float x, float y, float z,
                        float nx, float ny, float nz,
                        int light, int overlay) {
        consumer.addVertex(pose, x, y, z)
                .setColor(R, G, B, A)
                .setUv(0, 0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public boolean shouldRenderOffScreen(AcceleratorControllerBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(AcceleratorControllerBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        return new AABB(
                pos.getX() - 6, pos.getY() - 1, pos.getZ() - 6,
                pos.getX() + 7, pos.getY() + 4, pos.getZ() + 7);
    }
}
