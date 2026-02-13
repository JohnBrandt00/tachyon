package com.setusertso.tachyon.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.setusertso.tachyon.block.entity.AcceleratorControllerBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;


public class AcceleratorControllerRenderer implements BlockEntityRenderer<AcceleratorControllerBlockEntity> {

    private static final float ORBIT_TICKS = 80.0f; // 4 seconds per full orbit
    private static final float PARTICLE_SIZE = 0.2f; // Size for wispy effect
    private static final int PARTICLE_COUNT = 6; // Particles orbiting
    private static final int PARTICLES_PER_POINT = 2; // Spawn multiple particles per point for wispy effect
    // Bright cyan/electric blue glow color
    private static final float R = 0.3f, G = 0.7f, B = 1.0f, A = 0.9f;

    public AcceleratorControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AcceleratorControllerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isFormed() || be.getProgress() <= 0) return;

        List<BlockPos> ringPath = be.getRingPath();
        if (ringPath.isEmpty() || ringPath.size() < 4) return;

        // Calculate base position along the ring path with speed multiplier
        float speedMultiplier = be.getSpeedMultiplier();
        float gameTime = be.getLevel().getGameTime() + partialTick;
        float effectiveOrbitTime = ORBIT_TICKS / speedMultiplier; // Faster orbit at higher momentum
        float normalizedTime = (gameTime % effectiveOrbitTime) / effectiveOrbitTime;

        ClientLevel level = (ClientLevel) be.getLevel();

        // Render multiple particles evenly spaced around the ring
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float offset = (float) i / PARTICLE_COUNT;
            float particleTime = (normalizedTime + offset) % 1.0f;

            // Get smooth position using Catmull-Rom spline interpolation
            double[] pos = getSmoothPosition(ringPath, particleTime);

            // Spawn glowing particles for lighting effect (more at higher speed)
            // Soul fire flames emit light level 10
            float particleChance = 0.3f + (speedMultiplier - 0.5f) * 0.3f; // More particles at higher momentum
            if (level.random.nextFloat() < particleChance) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.15;
                double offsetY = (level.random.nextDouble() - 0.5) * 0.15;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.15;

                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos[0] + offsetX, pos[1] + offsetY, pos[2] + offsetZ,
                    0, 0.01, 0);
            }

            // Render a pulsing glowing core with full brightness
            float pulse = (float) (0.8 + 0.2 * Math.sin(gameTime * 0.1 + i));
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

            double relX = pos[0] - be.getBlockPos().getX();
            double relY = pos[1] - be.getBlockPos().getY();
            double relZ = pos[2] - be.getBlockPos().getZ();

            poseStack.pushPose();
            poseStack.translate(relX, relY, relZ);
            poseStack.scale(pulse, pulse, pulse);
            renderGlowingCube(poseStack, consumer, PARTICLE_SIZE, packedOverlay);
            poseStack.popPose();
        }
    }

    // Catmull-Rom spline interpolation for smooth curves
    private double[] getSmoothPosition(List<BlockPos> ringPath, float t) {
        int size = ringPath.size();
        float scaledT = t * size;
        int p1Index = (int) Math.floor(scaledT) % size;
        float fraction = scaledT - (int) scaledT;

        // Get 4 control points for Catmull-Rom spline
        int p0Index = (p1Index - 1 + size) % size;
        int p2Index = (p1Index + 1) % size;
        int p3Index = (p1Index + 2) % size;

        BlockPos p0 = ringPath.get(p0Index);
        BlockPos p1 = ringPath.get(p1Index);
        BlockPos p2 = ringPath.get(p2Index);
        BlockPos p3 = ringPath.get(p3Index);

        // Catmull-Rom spline formula
        double x = catmullRom(p0.getX() + 0.5, p1.getX() + 0.5, p2.getX() + 0.5, p3.getX() + 0.5, fraction);
        double y = catmullRom(p0.getY() + 0.5, p1.getY() + 0.5, p2.getY() + 0.5, p3.getY() + 0.5, fraction);
        double z = catmullRom(p0.getZ() + 0.5, p1.getZ() + 0.5, p2.getZ() + 0.5, p3.getZ() + 0.5, fraction);

        return new double[]{x, y, z};
    }

    private double catmullRom(double p0, double p1, double p2, double p3, float t) {
        double t2 = t * t;
        double t3 = t2 * t;

        return 0.5 * ((2 * p1) +
                     (-p0 + p2) * t +
                     (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 +
                     (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
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
                pos.getX() - 8, pos.getY() - 1, pos.getZ() - 8,
                pos.getX() + 9, pos.getY() + 6, pos.getZ() + 9);
    }
}
