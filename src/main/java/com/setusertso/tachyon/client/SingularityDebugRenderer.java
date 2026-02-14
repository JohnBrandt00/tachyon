package com.setusertso.tachyon.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.setusertso.tachyon.block.entity.SingularityDebugBlockEntity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helper to access protected RenderStateShard constants for custom RenderType creation.
 */
class BlackHoleRenderTypes extends RenderStateShard {
    // Dummy constructor — never instantiated, just used to access protected fields
    private BlackHoleRenderTypes() { super("dummy", () -> {}, () -> {}); }

    /**
     * Additive emissive render type. Uses additive blending (SRC_ALPHA, ONE) so colors
     * add light rather than alpha-blend. Keeps LEQUAL depth test so the opaque sphere
     * properly occludes the disk behind it. Renders to WEATHER_TARGET so it draws
     * after translucent geometry (water). Does not write depth (COLOR_WRITE only).
     */
    public static RenderType emissiveNoDepth(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(LIGHTNING_TRANSPARENCY) // additive: SRC_ALPHA, ONE
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .setOverlayState(OVERLAY)
                .setDepthTestState(LEQUAL_DEPTH_TEST) // sphere occludes disk behind it
                .setOutputState(WEATHER_TARGET) // renders after water/translucent
                .createCompositeState(false);
        return RenderType.create("black_hole_emissive",
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                1536, true, true, state);
    }
}

public class SingularityDebugRenderer implements BlockEntityRenderer<SingularityDebugBlockEntity> {

    // === SCALE FACTOR — change this one value to resize everything ===
    private static final float SCALE = 3.0f;

    // Black hole event horizon sphere
    private static final float SPHERE_RADIUS = 1.5f * SCALE;

    // Photon ring (bright thin ring hugging the event horizon)
    private static final float PHOTON_RING_RADIUS = 1.65f * SCALE;
    private static final float PHOTON_RING_WIDTH = 0.08f * SCALE;

    // Accretion disk layers
    private static final float DISK_INNER = 1.8f * SCALE;
    private static final float DISK_FADE = 4.5f * SCALE;
    private static final int DISK_SEGMENTS = 64;
    private static final int DISK_RADIAL_STEPS = 24; // smooth radial gradient
    private static final float DISK_TILT = 12.0f;
    private static final float DISK_ROTATION_SPEED = 1.5f;

    // Lensing arc
    private static final float LENS_ARC_INNER = 1.6f * SCALE;
    private static final float LENS_ARC_OUTER = 2.1f * SCALE;
    private static final int LENS_ARC_SEGMENTS = 32;

    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    // Cached geometry
    private static List<float[]> sphereTriangles;

    public SingularityDebugRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SingularityDebugBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;

        float gameTime = be.getLevel().getGameTime() + partialTick;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        BlockPos blockPos = be.getBlockPos();
        double centerX = blockPos.getX() + 0.5;
        double centerY = blockPos.getY() + 1.5 * SCALE;
        double centerZ = blockPos.getZ() + 0.5;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5 * SCALE, 0.5);

        renderBlackHoleSphere(poseStack, bufferSource, packedOverlay, gameTime);
        renderPhotonRing(poseStack, bufferSource, packedOverlay, gameTime);
        renderAccretionDisk(poseStack, bufferSource, packedOverlay, gameTime);
        renderLensingArc(poseStack, bufferSource, packedOverlay, gameTime,
                cameraPos, centerX, centerY, centerZ);

        poseStack.popPose();

        spawnParticles(be, gameTime);
    }

    // =========================================================================
    // 1. BLACK HOLE SPHERE
    // =========================================================================
    private void renderBlackHoleSphere(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedOverlay, float gameTime) {
        if (sphereTriangles == null) {
            sphereTriangles = generateIcosphere(SPHERE_RADIUS, 3);
        }

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
            iVertex(pose, consumer, tri[6], tri[7], tri[8], nx, ny, nz,
                    3, 3, 5, 255, 0, packedOverlay);
        }

        poseStack.popPose();
    }

    // =========================================================================
    // 2. PHOTON RING
    // =========================================================================
    private void renderPhotonRing(PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedOverlay, float gameTime) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();
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

            // Zero normals to prevent directional lighting (avoids bright/dark split)
            iVertex(pose, consumer, c1*innerR, 0, s1*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);
            iVertex(pose, consumer, c1*outerR, 0, s1*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c2*outerR, 0, s2*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c2*innerR, 0, s2*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);

            iVertex(pose, consumer, c2*innerR, 0, s2*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);
            iVertex(pose, consumer, c2*outerR, 0, s2*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c1*outerR, 0, s1*outerR, 0,0,0, br,bg,(int)(bb*0.7),150, light, packedOverlay);
            iVertex(pose, consumer, c1*innerR, 0, s1*innerR, 0,0,0, br,bg,bb,240, light, packedOverlay);
        }

        poseStack.popPose();
    }

    // =========================================================================
    // 3. ACCRETION DISK — smooth continuous radial gradient, no per-segment flicker
    // =========================================================================
    private void renderAccretionDisk(PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedOverlay, float gameTime) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        float rotation = (gameTime * DISK_ROTATION_SPEED) % 360.0f;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(DISK_TILT));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        PoseStack.Pose pose = poseStack.last();

        // Pre-compute color at each radial step using continuous gradient
        // t=0 inner edge, t=1 outer edge
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

                // Top face — inner ring uses c0, outer ring uses c1
                // Zero normals to prevent directional lighting (avoids bright/dark split)
                iVertex(pose, consumer, cos1*r0, 0, sin1*r0, 0,0,0,
                        c0[0], c0[1], c0[2], c0[3], light, packedOverlay);
                iVertex(pose, consumer, cos1*r1, 0, sin1*r1, 0,0,0,
                        c1[0], c1[1], c1[2], c1[3], light, packedOverlay);
                iVertex(pose, consumer, cos2*r1, 0, sin2*r1, 0,0,0,
                        c1[0], c1[1], c1[2], c1[3], light, packedOverlay);
                iVertex(pose, consumer, cos2*r0, 0, sin2*r0, 0,0,0,
                        c0[0], c0[1], c0[2], c0[3], light, packedOverlay);

                // Bottom face
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

    /** Continuous color gradient for the accretion disk. t=0 is inner edge, t=1 is outer edge.
     *  Returns {R, G, B, A} in 0-255 range. */
    private static int[] diskColorAt(float t) {
        // Color stops:
        // t=0.00: white-hot     (255, 245, 230, 230)
        // t=0.20: bright gold   (255, 210, 100, 220)
        // t=0.45: vivid orange   (255, 160, 40, 200)
        // t=0.65: deep orange   (230, 80, 20, 170)
        // t=0.85: deep red      (180, 40, 10, 100)
        // t=1.00: faded out     (100, 20, 5, 0)

        float[][] stops = {
            {0.00f, 255, 255, 240, 255},  // white-hot core — fully bright
            {0.15f, 255, 240, 160, 250},  // warm white-gold
            {0.30f, 255, 210, 80,  245},  // bright gold
            {0.50f, 255, 150, 30,  230},  // vivid orange
            {0.70f, 240, 80,  10,  200},  // deep orange-red
            {0.85f, 200, 40,  5,   130},  // deep red
            {1.00f, 120, 20,  5,   0},    // fades to nothing
        };

        // Find which two stops we're between
        int idx = 0;
        for (int i = 0; i < stops.length - 1; i++) {
            if (t >= stops[i][0] && t <= stops[i + 1][0]) {
                idx = i;
                break;
            }
        }

        float localT = (t - stops[idx][0]) / (stops[idx + 1][0] - stops[idx][0]);
        // Smooth interpolation
        localT = localT * localT * (3 - 2 * localT); // smoothstep

        return new int[]{
            clamp255((int)(stops[idx][1] + (stops[idx + 1][1] - stops[idx][1]) * localT)),
            clamp255((int)(stops[idx][2] + (stops[idx + 1][2] - stops[idx][2]) * localT)),
            clamp255((int)(stops[idx][3] + (stops[idx + 1][3] - stops[idx][3]) * localT)),
            clamp255((int)(stops[idx][4] + (stops[idx + 1][4] - stops[idx][4]) * localT)),
        };
    }

    // =========================================================================
    // 4. GRAVITATIONAL LENSING ARC — billboarded, smooth gradient, zero normals
    // =========================================================================
    private void renderLensingArc(PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedOverlay, float gameTime,
                                   Vec3 cameraPos, double cx, double cy, double cz) {
        VertexConsumer consumer = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));
        int light = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();

        double dx = cameraPos.x - cx;
        double dz = cameraPos.z - cz;
        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(DISK_TILT));

        PoseStack.Pose pose = poseStack.last();

        float pulse = 0.9f + 0.1f * (float) Math.sin(gameTime * 0.08);

        // Render with multiple radial bands for smooth inner→outer gradient
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

                // Use average of both edge sin values for smooth angular brightness
                float vertFactor1 = Math.abs(s1);
                float vertFactor2 = Math.abs(s2);
                float sharpFactor1 = vertFactor1 * vertFactor1;
                float sharpFactor2 = vertFactor2 * vertFactor2;

                // Inner ring color (brighter)
                float innerBright = (0.15f + 0.85f * sharpFactor1) * pulse * (1.0f - rt0 * 0.6f);
                int ir0 = clamp255((int)(255 * innerBright));
                int ig0 = clamp255((int)(240 * innerBright));
                int ib0 = clamp255((int)(200 * innerBright));
                int ia0 = clamp255((int)(230 * (0.1f + 0.9f * sharpFactor1) * (1.0f - rt0 * 0.8f)));

                // Outer ring color (dimmer, more transparent)
                float outerBright = (0.15f + 0.85f * sharpFactor1) * pulse * (1.0f - rt1 * 0.6f);
                int ir1 = clamp255((int)(255 * outerBright));
                int ig1 = clamp255((int)(240 * outerBright));
                int ib1 = clamp255((int)(200 * outerBright));
                int ia1 = clamp255((int)(230 * (0.1f + 0.9f * sharpFactor1) * (1.0f - rt1 * 0.8f)));

                // Second edge angular brightness
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

                // Front face — zero normals
                iVertex(pose, consumer, ix1, iy1, 0, 0,0,0, ir0,ig0,ib0,ia0, light, packedOverlay);
                iVertex(pose, consumer, ox1, oy1, 0, 0,0,0, ir1,ig1,ib1,ia1, light, packedOverlay);
                iVertex(pose, consumer, ox2, oy2, 0, 0,0,0, ir1b,ig1b,ib1b,ia1b, light, packedOverlay);
                iVertex(pose, consumer, ix2, iy2, 0, 0,0,0, ir0b,ig0b,ib0b,ia0b, light, packedOverlay);

                // Back face — zero normals
                iVertex(pose, consumer, ix2, iy2, 0, 0,0,0, ir0b,ig0b,ib0b,ia0b, light, packedOverlay);
                iVertex(pose, consumer, ox2, oy2, 0, 0,0,0, ir1b,ig1b,ib1b,ia1b, light, packedOverlay);
                iVertex(pose, consumer, ox1, oy1, 0, 0,0,0, ir1,ig1,ib1,ia1, light, packedOverlay);
                iVertex(pose, consumer, ix1, iy1, 0, 0,0,0, ir0,ig0,ib0,ia0, light, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    // =========================================================================
    // 5. PARTICLES — much denser, more variety
    // =========================================================================
    private void spawnParticles(SingularityDebugBlockEntity be, float gameTime) {
        if (!(be.getLevel() instanceof ClientLevel level)) return;

        BlockPos pos = be.getBlockPos();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.5 * SCALE;
        double cz = pos.getZ() + 0.5;

        float diskTiltRad = (float) Math.toRadians(DISK_TILT);

        // === DISK PLANE PARTICLES — multiple per tick, spiraling ===
        for (int p = 0; p < 3; p++) {
            if (level.random.nextFloat() < 0.7f) {
                float angle = level.random.nextFloat() * (float)(2 * Math.PI);
                float radius = DISK_INNER + level.random.nextFloat() * (DISK_FADE - DISK_INNER);

                double lx = Math.cos(angle) * radius;
                double ly = Math.sin(angle) * radius * Math.sin(diskTiltRad);
                double lz = Math.sin(angle) * radius * Math.cos(diskTiltRad);

                double x = cx + lx;
                double y = cy + ly;
                double z = cz + lz;

                double toX = (cx - x);
                double toY = (cy - y);
                double toZ = (cz - z);
                double tanX = -toZ * 0.015;
                double tanZ = toX * 0.015;
                double vx = toX * 0.015 + tanX;
                double vy = toY * 0.015;
                double vz = toZ * 0.015 + tanZ;

                // Inner particles are flame, outer are smoke
                float diskMid = (DISK_INNER + DISK_FADE) * 0.5f;
                if (radius < diskMid) {
                    level.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
                } else {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
                }
            }
        }

        // === INFALL PARTICLES — being sucked in from far away ===
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

        // === EVENT HORIZON SPARKS ===
        if (level.random.nextFloat() < 0.3f) {
            float angle = level.random.nextFloat() * (float)(2 * Math.PI);
            float phi = (level.random.nextFloat() - 0.5f) * (float) Math.PI;
            float radius = SPHERE_RADIUS + 0.2f * SCALE;

            double x = cx + Math.cos(angle) * Math.cos(phi) * radius;
            double y = cy + Math.sin(phi) * radius;
            double z = cz + Math.sin(angle) * Math.cos(phi) * radius;

            double vx = (cx - x) * 0.05;
            double vy = (cy - y) * 0.05;
            double vz = (cz - z) * 0.05;

            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, vx, vy, vz);
        }

        // === LENSING ARC PARTICLES — above and below the sphere ===
        for (int p = 0; p < 2; p++) {
            if (level.random.nextFloat() < 0.4f) {
                float angle = level.random.nextFloat() * (float)(2 * Math.PI);
                float radius = LENS_ARC_INNER + level.random.nextFloat() * (LENS_ARC_OUTER - LENS_ARC_INNER);

                float vertBias = (float) Math.sin(angle);
                if (Math.abs(vertBias) < 0.3f) continue;

                double x = cx + (level.random.nextFloat() - 0.5f) * 0.6f;
                double y = cy + vertBias * radius;
                double z = cz + (level.random.nextFloat() - 0.5f) * 0.6f;

                // Slight pull toward center
                double vx = (cx - x) * 0.01;
                double vy = (cy - y) * 0.005;
                double vz = (cz - z) * 0.01;

                level.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
            }
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** Vertex with int colors (0-255) */
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

    // =========================================================================
    // ICOSPHERE GENERATION
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

    @Override
    public boolean shouldRenderOffScreen(SingularityDebugBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(SingularityDebugBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        int r = (int)(6 * SCALE) + 1;
        return new AABB(
                pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                pos.getX() + r + 1, pos.getY() + r + 2, pos.getZ() + r + 1);
    }
}
