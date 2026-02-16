package com.setusertso.tachyon.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.setusertso.tachyon.block.entity.EngineState;
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

    // Wireframe ring rendering
    private static final float RING_RADIUS = 16.0f;
    private static final int RING_SEGMENTS = 128;
    private static final float RING_TUBE_RADIUS = 0.4f;
    private static final int RING_TUBE_SIDES = 12;
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static List<float[]> sphereTriangles;
    private static List<float[]> shieldTriangles;

    private float cachedBillboardYaw = 0;
    private float cachedDiskRotation = 0;
    private float prevRenderScale = -1;

    public SingularityControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SingularityControllerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isFormed() && be.getEngineState() != EngineState.MELTDOWN) return;

        BlockPos centerPos = be.getCenterPos();
        if (centerPos == null) return;
        if (be.getLevel() == null) return;

        float rawScale = be.getBlackHoleScale();
        // Client-side lerp for smooth inter-tick interpolation
        if (prevRenderScale < 0) prevRenderScale = rawScale;
        float bhScale = prevRenderScale + (rawScale - prevRenderScale) * 0.05f;
        prevRenderScale = bhScale;
        // Skip all rendering if neutralized (scale near zero)
        if (bhScale <= 0.01f) return;

        float gameTime = be.getLevel().getGameTime() + partialTick;
        EngineState engineState = be.getEngineState();

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

        // Apply dynamic black hole scale
        poseStack.pushPose();
        poseStack.scale(bhScale, bhScale, bhScale);

        // Render all black hole effects (scaled)
        renderBlackHoleSphere(poseStack, bufferSource, packedOverlay, gameTime);
        renderPhotonRing(poseStack, bufferSource, packedOverlay, gameTime, billboardYaw);
        renderAccretionDisk(poseStack, bufferSource, packedOverlay, gameTime, billboardYaw);
        renderLensingArc(poseStack, bufferSource, packedOverlay, gameTime,
                cameraPos, centerX, centerY, centerZ);

        poseStack.popPose();
        // End of scaled black hole effects

        // Render containment shield based on stability (not scaled by bhScale)
        float stability = (float) be.getStability();
        renderContainmentShield(poseStack, bufferSource, packedOverlay, gameTime, stability);

        // Render meltdown warning sphere during MELTDOWN state
        if (engineState == EngineState.MELTDOWN) {
            renderMeltdownWarning(poseStack, bufferSource, packedOverlay, gameTime);
        }

        // Render wireframe rings (replacing physical blocks) — only during NORMAL
        if (engineState == EngineState.NORMAL) {
            renderWireframeRings(poseStack, bufferSource, packedOverlay, gameTime, stability);
        }

        poseStack.popPose();

        // Spawn particles (scale particle distance with bhScale)
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
    // 5b. MELTDOWN WARNING SPHERE — pulsing red sphere during meltdown
    // =========================================================================
    private void renderMeltdownWarning(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedOverlay, float gameTime) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        // Pulsing red warning glow
        float pulse = 0.5f + 0.5f * (float) Math.sin(gameTime * 0.15);
        float fastPulse = 0.3f + 0.7f * (float) Math.sin(gameTime * 0.4);
        float combined = pulse * 0.6f + fastPulse * 0.4f;

        int r = clamp255((int)(255 * combined));
        int g = clamp255((int)(30 * combined));
        int b = clamp255((int)(10 * combined));
        int alpha = clamp255((int)(80 * combined));
        if (alpha < 3) return;

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
    // 6. WIREFRAME RINGS — 3 great circles rendered as thick metallic tubes
    // =========================================================================
    private void renderWireframeRings(PoseStack poseStack, MultiBufferSource bufferSource,
                                       int packedOverlay, float gameTime, float stability) {
        int light = LightTexture.FULL_BRIGHT;
        PoseStack.Pose pose = poseStack.last();

        // PASS 1: Render ALL solid metallic base geometry
        {
            VertexConsumer solidConsumer = bufferSource.getBuffer(
                    RenderType.entitySolid(ResourceLocation.withDefaultNamespace("textures/block/iron_block.png")));
            for (int axis = 0; axis < 3; axis++) {
                renderMetallicTubeRingSolid(pose, solidConsumer, light, packedOverlay,
                        axis, RING_RADIUS, RING_TUBE_RADIUS, RING_SEGMENTS, RING_TUBE_SIDES, gameTime);
            }
        }

        // PASS 2: Render ALL specular highlight geometry
        {
            VertexConsumer glowConsumer = bufferSource.getBuffer(
                    BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
            for (int axis = 0; axis < 3; axis++) {
                renderMetallicTubeRingGlow(pose, glowConsumer, light, packedOverlay,
                        axis, RING_RADIUS, RING_TUBE_RADIUS, RING_SEGMENTS, RING_TUBE_SIDES, gameTime);
            }
        }
    }

    /** Computes metallic shade: dark on faces away from light, bright highlight on faces toward light */
    private static int[] metallicShade(float nx, float ny, float nz, float gameTime, int axis) {
        // Simulated directional light from upper-right
        float lightDot = Math.max(0, ny * 0.6f + nx * 0.35f + nz * 0.25f);
        // Ambient term so shadow faces aren't pure black
        float ambient = 0.25f;
        float diffuse = ambient + (1.0f - ambient) * lightDot;
        // Specular highlight: sharp reflection
        float specPow = lightDot * lightDot * lightDot * lightDot;
        float shimmer = 0.7f + 0.3f * (float)(Math.sin(gameTime * 0.015 + axis * 2.1));
        float spec = specPow * shimmer;
        // Base metallic color: cool steel with slight blue tint
        int r = clamp255((int)(110 * diffuse + 180 * spec));
        int g = clamp255((int)(115 * diffuse + 175 * spec));
        int b = clamp255((int)(130 * diffuse + 200 * spec));
        return new int[]{r, g, b, 255};
    }

    /**
     * Returns the perpendicular frame (radial outward, ring normal) for a point on a great circle.
     * This is analytically derived per-axis so the frame is smooth and continuous around the ring,
     * eliminating gaps between tube segments.
     * Returns float[6]: {radialX, radialY, radialZ, normalX, normalY, normalZ}
     */
    private static float[] ringFrame(int axis, float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
        // perp1 = radial outward direction (from ring center to ring point)
        // perp2 = ring plane normal (constant for each axis)
        return switch (axis) {
            case 0 -> new float[]{c, 0, s,  0, 1, 0};  // XZ equator: radial in XZ, normal is Y
            case 1 -> new float[]{c, s, 0,  0, 0, 1};  // XY meridian: radial in XY, normal is Z
            case 2 -> new float[]{0, s, c,  1, 0, 0};  // YZ meridian: radial in YZ, normal is +X
            default -> new float[]{c, 0, s, 0, 1, 0};
        };
    }

    /** Pass 1: solid metallic base quads with per-face shading */
    private void renderMetallicTubeRingSolid(PoseStack.Pose pose, VertexConsumer consumer,
                                              int light, int overlay,
                                              int axis, float radius, float tubeRadius,
                                              int segments, int tubeSides, float gameTime) {
        for (int i = 0; i < segments; i++) {
            float angle0 = (float)(i * 2 * Math.PI / segments);
            float angle1 = (float)((i + 1) * 2 * Math.PI / segments);

            float[] c0 = ringPoint(axis, radius, angle0);
            float[] c1 = ringPoint(axis, radius, angle1);

            float[] frame0 = ringFrame(axis, angle0);
            float[] frame1 = ringFrame(axis, angle1);

            for (int j = 0; j < tubeSides; j++) {
                float ta0 = (float)(j * 2 * Math.PI / tubeSides);
                float ta1 = (float)((j + 1) * 2 * Math.PI / tubeSides);
                float cos0 = (float) Math.cos(ta0), sin0 = (float) Math.sin(ta0);
                float cos1 = (float) Math.cos(ta1), sin1 = (float) Math.sin(ta1);

                // Use per-vertex frame so edges match between adjacent segments
                float x00 = c0[0] + tubeRadius * (cos0 * frame0[0] + sin0 * frame0[3]);
                float y00 = c0[1] + tubeRadius * (cos0 * frame0[1] + sin0 * frame0[4]);
                float z00 = c0[2] + tubeRadius * (cos0 * frame0[2] + sin0 * frame0[5]);

                float x01 = c0[0] + tubeRadius * (cos1 * frame0[0] + sin1 * frame0[3]);
                float y01 = c0[1] + tubeRadius * (cos1 * frame0[1] + sin1 * frame0[4]);
                float z01 = c0[2] + tubeRadius * (cos1 * frame0[2] + sin1 * frame0[5]);

                float x10 = c1[0] + tubeRadius * (cos0 * frame1[0] + sin0 * frame1[3]);
                float y10 = c1[1] + tubeRadius * (cos0 * frame1[1] + sin0 * frame1[4]);
                float z10 = c1[2] + tubeRadius * (cos0 * frame1[2] + sin0 * frame1[5]);

                float x11 = c1[0] + tubeRadius * (cos1 * frame1[0] + sin1 * frame1[3]);
                float y11 = c1[1] + tubeRadius * (cos1 * frame1[1] + sin1 * frame1[4]);
                float z11 = c1[2] + tubeRadius * (cos1 * frame1[2] + sin1 * frame1[5]);

                float nx0 = cos0 * frame0[0] + sin0 * frame0[3];
                float ny0 = cos0 * frame0[1] + sin0 * frame0[4];
                float nz0 = cos0 * frame0[2] + sin0 * frame0[5];

                int[] shade = metallicShade(nx0, ny0, nz0, gameTime, axis);
                // Front face
                iVertex(pose, consumer, x00, y00, z00, nx0, ny0, nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                iVertex(pose, consumer, x01, y01, z01, nx0, ny0, nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                iVertex(pose, consumer, x11, y11, z11, nx0, ny0, nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                iVertex(pose, consumer, x10, y10, z10, nx0, ny0, nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                // Back face (reversed winding)
                iVertex(pose, consumer, x10, y10, z10, -nx0, -ny0, -nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                iVertex(pose, consumer, x11, y11, z11, -nx0, -ny0, -nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                iVertex(pose, consumer, x01, y01, z01, -nx0, -ny0, -nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
                iVertex(pose, consumer, x00, y00, z00, -nx0, -ny0, -nz0, shade[0], shade[1], shade[2], shade[3], light, overlay);
            }
        }
    }

    /** Pass 2: bright specular edge highlights */
    private void renderMetallicTubeRingGlow(PoseStack.Pose pose, VertexConsumer consumer,
                                             int light, int overlay,
                                             int axis, float radius, float tubeRadius,
                                             int segments, int tubeSides, float gameTime) {
        float shimmer = 0.4f + 0.6f * (float)(0.5 + 0.5 * Math.sin(gameTime * 0.02 + axis * 2.1));

        for (int i = 0; i < segments; i++) {
            float angle0 = (float)(i * 2 * Math.PI / segments);
            float angle1 = (float)((i + 1) * 2 * Math.PI / segments);

            float[] c0 = ringPoint(axis, radius, angle0);
            float[] c1 = ringPoint(axis, radius, angle1);

            float[] frame0 = ringFrame(axis, angle0);
            float[] frame1 = ringFrame(axis, angle1);

            for (int j = 0; j < tubeSides; j++) {
                float ta0 = (float)(j * 2 * Math.PI / tubeSides);
                float ta1 = (float)((j + 1) * 2 * Math.PI / tubeSides);
                float cos0 = (float) Math.cos(ta0), sin0 = (float) Math.sin(ta0);
                float cos1 = (float) Math.cos(ta1), sin1 = (float) Math.sin(ta1);

                float nx0 = cos0 * frame0[0] + sin0 * frame0[3];
                float ny0 = cos0 * frame0[1] + sin0 * frame0[4];
                float nz0 = cos0 * frame0[2] + sin0 * frame0[5];

                float upDot = Math.max(0, ny0 * 0.7f + nx0 * 0.3f + nz0 * 0.2f);
                float specular = upDot * upDot * upDot * shimmer;
                if (specular > 0.08f) {
                    float x00 = c0[0] + tubeRadius * (cos0 * frame0[0] + sin0 * frame0[3]);
                    float y00 = c0[1] + tubeRadius * (cos0 * frame0[1] + sin0 * frame0[4]);
                    float z00 = c0[2] + tubeRadius * (cos0 * frame0[2] + sin0 * frame0[5]);

                    float x01 = c0[0] + tubeRadius * (cos1 * frame0[0] + sin1 * frame0[3]);
                    float y01 = c0[1] + tubeRadius * (cos1 * frame0[1] + sin1 * frame0[4]);
                    float z01 = c0[2] + tubeRadius * (cos1 * frame0[2] + sin1 * frame0[5]);

                    float x10 = c1[0] + tubeRadius * (cos0 * frame1[0] + sin0 * frame1[3]);
                    float y10 = c1[1] + tubeRadius * (cos0 * frame1[1] + sin0 * frame1[4]);
                    float z10 = c1[2] + tubeRadius * (cos0 * frame1[2] + sin0 * frame1[5]);

                    float x11 = c1[0] + tubeRadius * (cos1 * frame1[0] + sin1 * frame1[3]);
                    float y11 = c1[1] + tubeRadius * (cos1 * frame1[1] + sin1 * frame1[4]);
                    float z11 = c1[2] + tubeRadius * (cos1 * frame1[2] + sin1 * frame1[5]);

                    int sr = clamp255((int)(255 * specular));
                    int sg = clamp255((int)(250 * specular));
                    int sb = clamp255((int)(240 * specular));
                    int sa = clamp255((int)(120 * specular));
                    float offset = 0.003f;
                    iVertex(pose, consumer, x00 + nx0*offset, y00 + ny0*offset, z00 + nz0*offset,
                            nx0, ny0, nz0, sr, sg, sb, sa, light, overlay);
                    iVertex(pose, consumer, x01 + nx0*offset, y01 + ny0*offset, z01 + nz0*offset,
                            nx0, ny0, nz0, sr, sg, sb, sa, light, overlay);
                    iVertex(pose, consumer, x11 + nx0*offset, y11 + ny0*offset, z11 + nz0*offset,
                            nx0, ny0, nz0, sr, sg, sb, sa, light, overlay);
                    iVertex(pose, consumer, x10 + nx0*offset, y10 + ny0*offset, z10 + nz0*offset,
                            nx0, ny0, nz0, sr, sg, sb, sa, light, overlay);
                }
            }
        }
    }

    /** Returns a point on a great circle ring. axis: 0=XZ(equator), 1=XY(meridian z=0), 2=YZ(meridian x=0) */
    private static float[] ringPoint(int axis, float radius, float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
        return switch (axis) {
            case 0 -> new float[]{c * radius, 0, s * radius};
            case 1 -> new float[]{c * radius, s * radius, 0};
            case 2 -> new float[]{0, s * radius, c * radius};
            default -> new float[]{c * radius, 0, s * radius};
        };
    }

    // =========================================================================
    // 7. PARTICLES
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

        // Scale particle count with black hole energy
        float energyFactor = Math.max(0.1f, be.getBlackHoleScale());
        int diskParticleCount = (int)(10 * energyFactor);
        int infallParticleCount = (int)(2 * energyFactor);
        int sphereParticleCount = (int)(3 * energyFactor);

        // Disk plane particles
        for (int p = 0; p < diskParticleCount; p++) {
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
        for (int p = 0; p < infallParticleCount; p++) {
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
        for (int p = 0; p < sphereParticleCount; p++) {
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
        float bhScale = Math.max(1.0f, be.getBlackHoleScale());
        int r = (int)(SHIELD_RADIUS * bhScale + 2);
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
