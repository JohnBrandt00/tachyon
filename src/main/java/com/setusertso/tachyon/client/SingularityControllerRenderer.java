package com.setusertso.tachyon.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;
import com.setusertso.tachyon.init.ModParticles;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SingularityControllerRenderer implements BlockEntityRenderer<SingularityControllerBlockEntity> {

    // Scale factor — the structure is 11 blocks across (radius 5), scale up to fill it
    private static final float SCALE = 3.0f;

    private static final float SPHERE_RADIUS = 1.5f * SCALE;
    private static final float PHOTON_RING_RADIUS = 1.65f * SCALE;
    private static final float PHOTON_RING_WIDTH = 0.08f * SCALE;
    private static final float DISK_INNER = 1.8f * SCALE;
    private static final float DISK_FADE = 4.5f * SCALE;
    private static final float LENS_ARC_INNER = 1.6f * SCALE;
    private static final float LENS_ARC_OUTER = 2.1f * SCALE;

    private static final int DISK_SEGMENTS = 64;
    private static final int DISK_RADIAL_STEPS = 24;
    private static final float DISK_TILT = 8.0f;
    private static final float DISK_ROTATION_SPEED = 1.5f;
    private static final int LENS_ARC_SEGMENTS = 32;
    private static final int SPHERE_SUBDIVISIONS = 3;
    private static final float SHIELD_RADIUS = 16.0f;
    private static final int SHIELD_SUBDIVISIONS = 3;

    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static List<float[]> sphereTriangles;
    private static List<float[]> shieldTriangles;

    private float cachedBillboardYaw = 0;
    private float cachedDiskRotation = 0;

    public SingularityControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SingularityControllerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isFormed()) return;

        BlockPos centerPos = be.getCenterPos();
        if (centerPos == null) return;
        if (be.getLevel() == null) return;

        float gameTime = be.getLevel().getGameTime() + partialTick;

        // Calculate offset from controller to center of the center block
        double dx = centerPos.getX() + 0.5 - be.getBlockPos().getX();
        double dy = centerPos.getY() + 0.5 - be.getBlockPos().getY();
        double dz = centerPos.getZ() + 0.5 - be.getBlockPos().getZ();

        double centerX = be.getBlockPos().getX() + dx;
        double centerY = be.getBlockPos().getY() + dy;
        double centerZ = be.getBlockPos().getZ() + dz;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        float billboardYaw = (float) Math.toDegrees(Math.atan2(
                cameraPos.x - centerX, cameraPos.z - centerZ));

        cachedBillboardYaw = billboardYaw;
        cachedDiskRotation = (gameTime * DISK_ROTATION_SPEED) % 360.0f;

        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);

        // Build sphere geometry if needed
        if (sphereTriangles == null) {
            sphereTriangles = generateIcosphere(SPHERE_RADIUS, SPHERE_SUBDIVISIONS);
        }
        if (shieldTriangles == null) {
            shieldTriangles = generateIcosphere(SHIELD_RADIUS, SHIELD_SUBDIVISIONS);
        }

        // Render all effects when formed
        renderBlackHoleSphere(poseStack, bufferSource, packedOverlay, gameTime);
        renderPhotonRing(poseStack, bufferSource, packedOverlay, gameTime, billboardYaw);
        renderAccretionDisk(poseStack, bufferSource, packedOverlay, gameTime, billboardYaw);
        renderLensingArc(poseStack, bufferSource, packedOverlay, gameTime,
                cameraPos, centerX, centerY, centerZ);

        // Render containment shield based on stability
        float stability = (float) be.getStability();
        renderContainmentShield(poseStack, bufferSource, packedOverlay, gameTime, stability);

        poseStack.popPose();

        // Spawn particles
        spawnParticles(be, gameTime, centerX, centerY, centerZ);
    }

    // =========================================================================
    // 1. BLACK HOLE SPHERE — uses entitySolid with black_concrete texture
    // =========================================================================
    private void renderBlackHoleSphere(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedOverlay, float gameTime) {
        float pulse = 1.0f
                + 0.03f * (float) Math.sin(gameTime * 0.03)
                + 0.01f * (float) Math.sin(gameTime * 0.13);

        poseStack.pushPose();
        poseStack.scale(pulse, pulse, pulse);

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entitySolid(ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png")));
        PoseStack.Pose pose = poseStack.last();

        for (float[] tri : sphereTriangles) {
            float nx = -tri[9], ny = -tri[10], nz = -tri[11];
            iVertex(pose, consumer, tri[0], tri[1], tri[2], nx, ny, nz,
                    3, 3, 5, 255, 0, packedOverlay);
            iVertex(pose, consumer, tri[3], tri[4], tri[5], nx, ny, nz,
                    3, 3, 5, 255, 0, packedOverlay);
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz,
                    3, 3, 5, 255, 0, packedOverlay);
            // Degenerate 4th vertex for QUADS format
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz,
                    3, 3, 5, 255, 0, packedOverlay);
        }

        poseStack.popPose();
    }

    // =========================================================================
    // 2. PHOTON RING
    // =========================================================================
    private void renderPhotonRing(PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedOverlay, float gameTime, float billboardYaw) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(billboardYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(DISK_TILT));

        PoseStack.Pose pose = poseStack.last();

        float innerR = PHOTON_RING_RADIUS - PHOTON_RING_WIDTH;
        float outerR = PHOTON_RING_RADIUS + PHOTON_RING_WIDTH;

        float bp = 0.85f + 0.15f * (float) Math.sin(gameTime * 0.1);
        int br = (int)(255 * bp);
        int bg = (int)(245 * bp);
        int bb = (int)(220 * bp);

        for (int i = 0; i < DISK_SEGMENTS; i++) {
            float a1 = (float) (i * 2 * Math.PI / DISK_SEGMENTS);
            float a2 = (float) ((i + 1) * 2 * Math.PI / DISK_SEGMENTS);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float c2 = (float) Math.cos(a2), s2 = (float) Math.sin(a2);

            // Front face
            iVertex(pose, consumer, c1*innerR, 0, s1*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);
            iVertex(pose, consumer, c1*outerR, 0, s1*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c2*outerR, 0, s2*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c2*innerR, 0, s2*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);

            // Back face
            iVertex(pose, consumer, c2*innerR, 0, s2*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);
            iVertex(pose, consumer, c2*outerR, 0, s2*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c1*outerR, 0, s1*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c1*innerR, 0, s1*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);
        }

        poseStack.popPose();
    }

    // =========================================================================
    // 3. ACCRETION DISK
    // =========================================================================
    private void renderAccretionDisk(PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedOverlay, float gameTime, float billboardYaw) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        float rotation = (gameTime * DISK_ROTATION_SPEED) % 360.0f;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(billboardYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(DISK_TILT));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        PoseStack.Pose pose = poseStack.last();

        float radialRange = DISK_FADE - DISK_INNER;

        for (int ring = 0; ring < DISK_RADIAL_STEPS; ring++) {
            float t0 = (float) ring / DISK_RADIAL_STEPS;
            float t1 = (float) (ring + 1) / DISK_RADIAL_STEPS;
            float r0 = DISK_INNER + t0 * radialRange;
            float r1 = DISK_INNER + t1 * radialRange;

            int[] c0 = diskColorAt(t0);
            int[] c1 = diskColorAt(t1);

            for (int seg = 0; seg < DISK_SEGMENTS; seg++) {
                float a1 = (float) (seg * 2 * Math.PI / DISK_SEGMENTS);
                float a2 = (float) ((seg + 1) * 2 * Math.PI / DISK_SEGMENTS);
                float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
                float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

                // Front face
                iVertex(pose, consumer, cos1*r0, 0, sin1*r0, 0,0,0,
                        c0[0], c0[1], c0[2], c0[3], light, packedOverlay);
                iVertex(pose, consumer, cos1*r1, 0, sin1*r1, 0,0,0,
                        c1[0], c1[1], c1[2], c1[3], light, packedOverlay);
                iVertex(pose, consumer, cos2*r1, 0, sin2*r1, 0,0,0,
                        c1[0], c1[1], c1[2], c1[3], light, packedOverlay);
                iVertex(pose, consumer, cos2*r0, 0, sin2*r0, 0,0,0,
                        c0[0], c0[1], c0[2], c0[3], light, packedOverlay);

                // Back face
                iVertex(pose, consumer, cos2*r0, 0, sin2*r0, 0,0,0,
                        c0[0], c0[1], c0[2], c0[3], light, packedOverlay);
                iVertex(pose, consumer, cos2*r1, 0, sin2*r1, 0,0,0,
                        c1[0], c1[1], c1[2], c1[3], light, packedOverlay);
                iVertex(pose, consumer, cos1*r1, 0, sin1*r1, 0,0,0,
                        c1[0], c1[1], c1[2], c1[3], light, packedOverlay);
                iVertex(pose, consumer, cos1*r0, 0, sin1*r0, 0,0,0,
                        c0[0], c0[1], c0[2], c0[3], light, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private static int[] diskColorAt(float t) {
        float[][] stops = {
            {0.00f, 255, 255, 240, 255},
            {0.15f, 255, 240, 160, 250},
            {0.30f, 255, 210, 80,  245},
            {0.50f, 255, 150, 30,  230},
            {0.70f, 240, 80,  10,  200},
            {0.85f, 200, 40,  5,   130},
            {1.00f, 120, 20,  5,   0},
        };

        int idx = 0;
        for (int i = 0; i < stops.length - 1; i++) {
            if (t >= stops[i][0] && t <= stops[i + 1][0]) {
                idx = i;
                break;
            }
        }

        float localT = (t - stops[idx][0]) / (stops[idx + 1][0] - stops[idx][0]);
        localT = localT * localT * (3 - 2 * localT);

        return new int[]{
            clamp255((int)(stops[idx][1] + (stops[idx + 1][1] - stops[idx][1]) * localT)),
            clamp255((int)(stops[idx][2] + (stops[idx + 1][2] - stops[idx][2]) * localT)),
            clamp255((int)(stops[idx][3] + (stops[idx + 1][3] - stops[idx][3]) * localT)),
            clamp255((int)(stops[idx][4] + (stops[idx + 1][4] - stops[idx][4]) * localT)),
        };
    }

    // =========================================================================
    // 4. GRAVITATIONAL LENSING ARC
    // =========================================================================
    private void renderLensingArc(PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedOverlay, float gameTime,
                                   Vec3 cameraPos, double cx, double cy, double cz) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();

        double ddx = cameraPos.x - cx;
        double ddz = cameraPos.z - cz;
        float yaw = (float) Math.toDegrees(Math.atan2(ddx, ddz));

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(DISK_TILT));

        PoseStack.Pose pose = poseStack.last();

        float pulse = 0.9f + 0.1f * (float) Math.sin(gameTime * 0.08);

        int lensRadialSteps = 8;
        float lensRadialRange = LENS_ARC_OUTER - LENS_ARC_INNER;

        for (int ring = 0; ring < lensRadialSteps; ring++) {
            float rt0 = (float) ring / lensRadialSteps;
            float rt1 = (float) (ring + 1) / lensRadialSteps;
            float rad0 = LENS_ARC_INNER + rt0 * lensRadialRange;
            float rad1 = LENS_ARC_INNER + rt1 * lensRadialRange;

            for (int i = 0; i < LENS_ARC_SEGMENTS; i++) {
                float a1 = (float) (i * 2 * Math.PI / LENS_ARC_SEGMENTS);
                float a2 = (float) ((i + 1) * 2 * Math.PI / LENS_ARC_SEGMENTS);
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                float c2 = (float) Math.cos(a2), s2 = (float) Math.sin(a2);

                float vertFactor1 = Math.abs(s1);
                float vertFactor2 = Math.abs(s2);
                float sharpFactor1 = vertFactor1 * vertFactor1;
                float sharpFactor2 = vertFactor2 * vertFactor2;

                float innerBright = (0.15f + 0.85f * sharpFactor1) * pulse * (1.0f - rt0 * 0.6f);
                int ir0 = clamp255((int)(255 * innerBright));
                int ig0 = clamp255((int)(240 * innerBright));
                int ib0 = clamp255((int)(200 * innerBright));
                int ia0 = clamp255((int)(230 * (0.1f + 0.9f * sharpFactor1) * (1.0f - rt0 * 0.8f)));

                float outerBright = (0.15f + 0.85f * sharpFactor1) * pulse * (1.0f - rt1 * 0.6f);
                int ir1 = clamp255((int)(255 * outerBright));
                int ig1 = clamp255((int)(240 * outerBright));
                int ib1 = clamp255((int)(200 * outerBright));
                int ia1 = clamp255((int)(230 * (0.1f + 0.9f * sharpFactor1) * (1.0f - rt1 * 0.8f)));

                float innerBright2 = (0.15f + 0.85f * sharpFactor2) * pulse * (1.0f - rt0 * 0.6f);
                int ir0b = clamp255((int)(255 * innerBright2));
                int ig0b = clamp255((int)(240 * innerBright2));
                int ib0b = clamp255((int)(200 * innerBright2));
                int ia0b = clamp255((int)(230 * (0.1f + 0.9f * sharpFactor2) * (1.0f - rt0 * 0.8f)));

                float outerBright2 = (0.15f + 0.85f * sharpFactor2) * pulse * (1.0f - rt1 * 0.6f);
                int ir1b = clamp255((int)(255 * outerBright2));
                int ig1b = clamp255((int)(240 * outerBright2));
                int ib1b = clamp255((int)(200 * outerBright2));
                int ia1b = clamp255((int)(230 * (0.1f + 0.9f * sharpFactor2) * (1.0f - rt1 * 0.8f)));

                float ix1 = c1 * rad0, iy1 = s1 * rad0;
                float ix2 = c2 * rad0, iy2 = s2 * rad0;
                float ox1 = c1 * rad1, oy1 = s1 * rad1;
                float ox2 = c2 * rad1, oy2 = s2 * rad1;

                iVertex(pose, consumer, ix1, iy1, 0, 0,0,0, ir0,ig0,ib0,ia0, light, packedOverlay);
                iVertex(pose, consumer, ox1, oy1, 0, 0,0,0, ir1,ig1,ib1,ia1, light, packedOverlay);
                iVertex(pose, consumer, ox2, oy2, 0, 0,0,0, ir1b,ig1b,ib1b,ia1b, light, packedOverlay);
                iVertex(pose, consumer, ix2, iy2, 0, 0,0,0, ir0b,ig0b,ib0b,ia0b, light, packedOverlay);

                iVertex(pose, consumer, ix2, iy2, 0, 0,0,0, ir0b,ig0b,ib0b,ia0b, light, packedOverlay);
                iVertex(pose, consumer, ox2, oy2, 0, 0,0,0, ir1b,ig1b,ib1b,ia1b, light, packedOverlay);
                iVertex(pose, consumer, ox1, oy1, 0, 0,0,0, ir1,ig1,ib1,ia1, light, packedOverlay);
                iVertex(pose, consumer, ix1, iy1, 0, 0,0,0, ir0,ig0,ib0,ia0, light, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    // =========================================================================
    // 5. CONTAINMENT SHIELD
    // =========================================================================
    private void renderContainmentShield(PoseStack poseStack, MultiBufferSource bufferSource,
                                          int packedOverlay, float gameTime, float stability) {
        if (stability <= 0) return;

        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        // Stability 0-100 mapped to shield properties
        float t = stability / 100.0f; // 0.0 to 1.0

        // Color: high stability = cyan/blue, low stability = red/orange
        int r = clamp255((int)(255 * (1.0f - t) + 40 * t));
        int g = clamp255((int)(40 * (1.0f - t) + 200 * t));
        int b = clamp255((int)(20 * (1.0f - t) + 255 * t));

        // Alpha: scales with stability, with a subtle pulse
        float pulse = 0.85f + 0.15f * (float) Math.sin(gameTime * 0.05);
        // Flicker when low stability
        float flicker = 1.0f;
        if (t < 0.3f) {
            flicker = 0.5f + 0.5f * (float) Math.sin(gameTime * 0.7 + Math.sin(gameTime * 1.3) * 3.0);
            flicker = Math.max(0.1f, flicker);
        }
        int alpha = clamp255((int)(60 * t * pulse * flicker));
        if (alpha < 3) return; // too faint to bother

        PoseStack.Pose pose = poseStack.last();

        for (float[] tri : shieldTriangles) {
            float nx = -tri[9], ny = -tri[10], nz = -tri[11];
            // Front face
            iVertex(pose, consumer, tri[0], tri[1], tri[2], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            iVertex(pose, consumer, tri[3], tri[4], tri[5], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            // Back face
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            iVertex(pose, consumer, tri[3], tri[4], tri[5], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
            iVertex(pose, consumer, tri[0], tri[1], tri[2], nx, ny, nz, r, g, b, alpha, light, packedOverlay);
        }
    }

    // =========================================================================
    // 6. PARTICLES
    // =========================================================================
    private Vec3 diskToWorld(double lx, double lz, double cx, double cy, double cz,
                              float yawRad, float tiltRad, float rotRad) {
        double cosR = Math.cos(rotRad), sinR = Math.sin(rotRad);
        double rx = lx * cosR + lz * sinR;
        double rz = -lx * sinR + lz * cosR;
        double ry = 0;

        double cosT = Math.cos(tiltRad), sinT = Math.sin(tiltRad);
        double ty = ry * cosT - rz * sinT;
        double tz = ry * sinT + rz * cosT;
        double tx = rx;

        double cosY = Math.cos(yawRad), sinY = Math.sin(yawRad);
        double fy = ty;
        double fx = tx * cosY + tz * sinY;
        double fz = -tx * sinY + tz * cosY;

        return new Vec3(cx + fx, cy + fy, cz + fz);
    }

    private void spawnParticles(SingularityControllerBlockEntity be, float gameTime,
                                 double cx, double cy, double cz) {
        if (!(be.getLevel() instanceof ClientLevel level)) return;

        float yawRad = (float) Math.toRadians(cachedBillboardYaw);
        float tiltRad = (float) Math.toRadians(DISK_TILT);
        float rotRad = (float) Math.toRadians(cachedDiskRotation);

        // Disk plane particles
        for (int p = 0; p < 10; p++) {
            if (level.random.nextFloat() < 0.95f) {
                float angle = level.random.nextFloat() * (float)(2 * Math.PI);
                double lx = Math.cos(angle) * DISK_FADE;
                double lz = Math.sin(angle) * DISK_FADE;

                Vec3 worldPos = diskToWorld(lx, lz, cx, cy, cz, yawRad, tiltRad, rotRad);

                double toX = cx - worldPos.x;
                double toY = cy - worldPos.y;
                double toZ = cz - worldPos.z;
                double dist = Math.sqrt(toX * toX + toY * toY + toZ * toZ);

                double speed = dist / 50.0;
                double vx = (toX / dist) * speed;
                double vy = (toY / dist) * speed;
                double vz = (toZ / dist) * speed;

                level.addParticle(ModParticles.ACCRETION_DISK.get(), worldPos.x, worldPos.y, worldPos.z, vx, vy, vz);
            }
        }

        // Infall particles
        for (int p = 0; p < 2; p++) {
            if (level.random.nextFloat() < 0.4f) {
                float angle = level.random.nextFloat() * (float)(2 * Math.PI);
                float radius = DISK_FADE + 0.5f * SCALE + level.random.nextFloat() * 3.0f * SCALE;
                float heightOff = (level.random.nextFloat() - 0.5f) * 2.0f * SCALE;

                double x = cx + Math.cos(angle) * radius;
                double y = cy + heightOff;
                double z = cz + Math.sin(angle) * radius;

                double vx = (cx - x) * 0.02;
                double vy = (cy - y) * 0.02;
                double vz = (cz - z) * 0.02;

                level.addParticle(ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
            }
        }

        // Sphere infall particles
        for (int p = 0; p < 3; p++) {
            if (level.random.nextFloat() < 0.5f) {
                float angle = level.random.nextFloat() * (float)(2 * Math.PI);
                float phi = (level.random.nextFloat() - 0.5f) * (float) Math.PI;
                float radius = SPHERE_RADIUS + 1.5f * SCALE;

                double x = cx + Math.cos(angle) * Math.cos(phi) * radius;
                double y = cy + Math.sin(phi) * radius;
                double z = cz + Math.sin(angle) * Math.cos(phi) * radius;

                double toX = cx - x;
                double toY = cy - y;
                double toZ = cz - z;
                double dist = Math.sqrt(toX * toX + toY * toY + toZ * toZ);
                double speed = dist / 50.0;

                level.addParticle(ModParticles.ACCRETION_DISK.get(), x, y, z,
                        (toX / dist) * speed, (toY / dist) * speed, (toZ / dist) * speed);
            }
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private void iVertex(PoseStack.Pose pose, VertexConsumer consumer,
                         float x, float y, float z,
                         float nx, float ny, float nz,
                         int r, int g, int b, int a,
                         int light, int overlay) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(SingularityControllerBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        int r = (int)(DISK_FADE + 2);
        return new AABB(pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                         pos.getX() + r + 1, pos.getY() + r + 1, pos.getZ() + r + 1);
    }

    @Override
    public boolean shouldRenderOffScreen(SingularityControllerBlockEntity be) {
        return true;
    }

    // =========================================================================
    // ICOSPHERE GENERATION (same as SingularityDebugRenderer)
    // =========================================================================
    private static List<float[]> generateIcosphere(float radius, int subdivisions) {
        float t = (float) ((1.0 + Math.sqrt(5.0)) / 2.0);

        float[][] baseVerts = {
            {-1,  t,  0}, { 1,  t,  0}, {-1, -t,  0}, { 1, -t,  0},
            { 0, -1,  t}, { 0,  1,  t}, { 0, -1, -t}, { 0,  1, -t},
            { t,  0, -1}, { t,  0,  1}, {-t,  0, -1}, {-t,  0,  1}
        };
        for (float[] v : baseVerts) {
            float len = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            v[0] /= len; v[1] /= len; v[2] /= len;
        }

        int[][] baseFaces = {
            {0,11,5}, {0,5,1}, {0,1,7}, {0,7,10}, {0,10,11},
            {1,5,9}, {5,11,4}, {11,10,2}, {10,7,6}, {7,1,8},
            {3,9,4}, {3,4,2}, {3,2,6}, {3,6,8}, {3,8,9},
            {4,9,5}, {2,4,11}, {6,2,10}, {8,6,7}, {9,8,1}
        };

        List<float[]> triangles = new ArrayList<>();
        for (int[] face : baseFaces) {
            triangles.add(new float[]{
                baseVerts[face[0]][0], baseVerts[face[0]][1], baseVerts[face[0]][2],
                baseVerts[face[1]][0], baseVerts[face[1]][1], baseVerts[face[1]][2],
                baseVerts[face[2]][0], baseVerts[face[2]][1], baseVerts[face[2]][2]
            });
        }

        for (int s = 0; s < subdivisions; s++) {
            List<float[]> next = new ArrayList<>();
            for (float[] tri : triangles) {
                float[] v0 = {tri[0], tri[1], tri[2]};
                float[] v1 = {tri[3], tri[4], tri[5]};
                float[] v2 = {tri[6], tri[7], tri[8]};
                float[] m01 = normalize(midpoint(v0, v1));
                float[] m12 = normalize(midpoint(v1, v2));
                float[] m20 = normalize(midpoint(v2, v0));
                next.add(new float[]{v0[0],v0[1],v0[2], m01[0],m01[1],m01[2], m20[0],m20[1],m20[2]});
                next.add(new float[]{v1[0],v1[1],v1[2], m12[0],m12[1],m12[2], m01[0],m01[1],m01[2]});
                next.add(new float[]{v2[0],v2[1],v2[2], m20[0],m20[1],m20[2], m12[0],m12[1],m12[2]});
                next.add(new float[]{m01[0],m01[1],m01[2], m12[0],m12[1],m12[2], m20[0],m20[1],m20[2]});
            }
            triangles = next;
        }

        List<float[]> result = new ArrayList<>();
        for (float[] tri : triangles) {
            for (int i = 0; i < 9; i++) tri[i] *= radius;
            float e1x = tri[3]-tri[0], e1y = tri[4]-tri[1], e1z = tri[5]-tri[2];
            float e2x = tri[6]-tri[0], e2y = tri[7]-tri[1], e2z = tri[8]-tri[2];
            float nx = e1y*e2z - e1z*e2y;
            float ny = e1z*e2x - e1x*e2z;
            float nz = e1x*e2y - e1y*e2x;
            float nLen = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
            if (nLen > 0) { nx /= nLen; ny /= nLen; nz /= nLen; }
            result.add(new float[]{tri[0],tri[1],tri[2], tri[3],tri[4],tri[5], tri[6],tri[7],tri[8], nx,ny,nz});
        }
        return result;
    }

    private static float[] midpoint(float[] a, float[] b) {
        return new float[]{(a[0]+b[0])*0.5f, (a[1]+b[1])*0.5f, (a[2]+b[2])*0.5f};
    }

    private static float[] normalize(float[] v) {
        float len = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
        return new float[]{v[0]/len, v[1]/len, v[2]/len};
    }
}
