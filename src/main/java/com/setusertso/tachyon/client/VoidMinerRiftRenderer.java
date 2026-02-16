package com.setusertso.tachyon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.setusertso.tachyon.block.entity.VoidMinerControllerBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

/**
 * Renderer for the Void Miner rift effect.
 * - Thin beacon-style beam shooting up from the structure
 * - Large asymmetrical rift with actual end-portal parallax shader
 * - Actual items spiraling slowly down the beam
 * - Sparse lightning
 */
public class VoidMinerRiftRenderer implements BlockEntityRenderer<VoidMinerControllerBlockEntity> {

    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static final float[][] TIER_COLORS = {
            {0.0f, 0.0f, 0.0f},
            {0.6f, 0.3f, 0.7f},       // T1: Purple
            {0.8f, 0.2f, 1.0f},       // T2: Bright magenta
            {0.3f, 0.5f, 1.0f},       // T3: Electric blue
            {1.0f, 0.7f, 0.2f},       // T4: Molten gold
    };

    private static final int RIFT_SEGMENTS = 96;
    private static final int ITEM_FALL_DURATION = 120;
    private static final int ITEM_COUNT = 3;
    private static final int ITEM_STAGGER_TICKS = 15;

    // Rift sits at 40% of beam height
    private static final float RIFT_HEIGHT_FRACTION = 0.40f;

    private final Minecraft mc;

    public VoidMinerRiftRenderer(BlockEntityRendererProvider.Context context) {
        this.mc = Minecraft.getInstance();
    }

    @Override
    public void render(VoidMinerControllerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isFormed()) return;

        BlockPos riftCenter = be.getRiftCenter();
        BlockPos beamTop = be.getBeamTop();
        if (riftCenter == null || beamTop == null) return;

        int tier = be.getStructureTier();
        if (tier < 1 || tier > 4) return;

        float[] color = TIER_COLORS[tier];
        long gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0;
        float time = gameTime + partialTick;

        BlockPos controllerPos = be.getBlockPos();
        float dx = riftCenter.getX() - controllerPos.getX() + 0.5f;
        float dy = riftCenter.getY() - controllerPos.getY() + 0.5f;
        float dz = riftCenter.getZ() - controllerPos.getZ() + 0.5f;

        float beamHeight = beamTop.getY() - riftCenter.getY();
        float riftY = beamHeight * RIFT_HEIGHT_FRACTION;
        int light = LightTexture.FULL_BRIGHT;

        // --- 1. Rift body with actual end portal parallax shader ---
        // Uses RenderType.endGateway() which provides the real end-portal effect.
        // Vertex format is POSITION only — no color, UV, normal, or light.
        {
            VertexConsumer portalConsumer = bufferSource.getBuffer(RenderType.endGateway());

            poseStack.pushPose();
            poseStack.translate(dx, dy + riftY, dz);
            // No rotation — static jagged shape
            renderRiftPortalBody(poseStack, portalConsumer, tier, time);
            poseStack.popPose();
        }

        // --- 2. All emissive geometry (beam, edge glow, lightning) ---
        VertexConsumer emissive = bufferSource.getBuffer(
                BlackHoleRenderTypes.emissiveNoDepth(WHITE_TEX));

        // Beacon beam — from structure base up to the rift (not through it)
        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);
        renderBeaconBeam(poseStack, emissive, light, color, tier, riftY, time);
        poseStack.popPose();

        // Rift edge glow
        poseStack.pushPose();
        poseStack.translate(dx, dy + riftY, dz);
        renderRiftEdgeGlow(poseStack, emissive, light, color, tier, time);
        poseStack.popPose();

        // Lightning (very sparse — 1 bolt, renders 1/4 of the time)
        if ((gameTime / 10) % 4 == 0) {
            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            renderLightning(poseStack, emissive, light, color, tier, beamHeight, gameTime);
            poseStack.popPose();
        }

        // --- 3. Falling items spiraling down ---
        // Items fall from rift (dy + riftY) all the way down to the structure top (y ~ 1)
        // We render relative to controller pos, so fall range is from (dy + riftY) to 1.0
        float itemStartY = dy + riftY;
        float itemEndY = 0.0f; // controller level (Y=0 in local space)
        long lastProd = be.getLastProductionTick();
        ItemStack lastItem = be.getLastProducedItem();
        if (lastProd >= 0 && !lastItem.isEmpty()) {
            for (int i = 0; i < ITEM_COUNT; i++) {
                long itemElapsed = gameTime - lastProd - (long) i * ITEM_STAGGER_TICKS;
                if (itemElapsed >= 0 && itemElapsed < ITEM_FALL_DURATION) {
                    float progress = (itemElapsed + partialTick) / (float) ITEM_FALL_DURATION;
                    poseStack.pushPose();
                    poseStack.translate(dx, 0, dz); // translate X/Z to beam center, Y stays at controller
                    renderFallingItem(poseStack, bufferSource, lastItem, itemStartY, itemEndY, progress, time, i, be.getLevel());
                    poseStack.popPose();
                }
            }
        }
    }

    // =========================================================================
    // 1. Beacon beam — thin, clean
    // =========================================================================

    private void renderBeaconBeam(PoseStack poseStack, VertexConsumer consumer,
                                   int light, float[] color, int tier,
                                   float beamHeight, float time) {
        PoseStack.Pose pose = poseStack.last();
        float pulse = 0.85f + 0.15f * (float) Math.sin(time * 0.08);

        // Outer glow
        float outerWidth = 0.35f + tier * 0.04f;
        beamQuads(consumer, pose, light, outerWidth, beamHeight,
                c255(color[0] * 200 * pulse), c255(color[1] * 200 * pulse),
                c255(color[2] * 200 * pulse), c255(25 * pulse));

        // Main colored beam
        float mainWidth = 0.12f + tier * 0.015f;
        beamQuads(consumer, pose, light, mainWidth, beamHeight,
                c255(color[0] * 255 * pulse), c255(color[1] * 255 * pulse),
                c255(color[2] * 255 * pulse), c255(130 * pulse));

        // White core
        float cp = 0.9f + 0.1f * (float) Math.sin(time * 0.15);
        beamQuads(consumer, pose, light, 0.05f, beamHeight, 255, 255, 255, c255(190 * cp));
    }

    private void beamQuads(VertexConsumer consumer, PoseStack.Pose pose, int light,
                            float halfWidth, float height, int r, int g, int b, int a) {
        v(consumer, pose, -halfWidth, 0, 0, r, g, b, a, light);
        v(consumer, pose, halfWidth, 0, 0, r, g, b, a, light);
        v(consumer, pose, halfWidth, height, 0, r, g, b, a, light);
        v(consumer, pose, -halfWidth, height, 0, r, g, b, a, light);

        v(consumer, pose, 0, 0, -halfWidth, r, g, b, a, light);
        v(consumer, pose, 0, 0, halfWidth, r, g, b, a, light);
        v(consumer, pose, 0, height, halfWidth, r, g, b, a, light);
        v(consumer, pose, 0, height, -halfWidth, r, g, b, a, light);
    }

    // =========================================================================
    // 2. Rift — end portal parallax shader, jagged irregular shape
    // =========================================================================

    private static final int RIFT_RINGS = 8; // concentric rings for tessellation

    /**
     * Renders the rift body using RenderType.endGateway() for real end-portal parallax.
     * Uses concentric rings of quads radiating from center to edge.
     * Ring 0 inner edge is near the center, ring N-1 outer edge is at the jagged boundary.
     * The shader only takes POSITION — no color/UV/normal/light.
     *
     * Renders both top-facing and bottom-facing quads so rift is visible from any angle.
     */
    private void renderRiftPortalBody(PoseStack poseStack, VertexConsumer portal,
                                       int tier, float time) {
        Matrix4f matrix = poseStack.last().pose();
        float baseRadius = 7.5f + tier * 1.8f;

        for (int ring = 0; ring < RIFT_RINGS; ring++) {
            // Ring fractions from 0 (center) to 1 (edge)
            float fracInner = (float) ring / RIFT_RINGS;
            float fracOuter = (float) (ring + 1) / RIFT_RINGS;

            for (int seg = 0; seg < RIFT_SEGMENTS; seg++) {
                int seg2 = (seg + 1) % RIFT_SEGMENTS; // wrap so last segment connects to first
                float a1 = seg * 2.0f * (float) Math.PI / RIFT_SEGMENTS;
                float a2 = (seg + 1) * 2.0f * (float) Math.PI / RIFT_SEGMENTS;

                // Edge multiplier for this segment — gentle wobble at the boundary
                float edge1 = getJaggedRadius(seg, time);
                float edge2 = getJaggedRadius(seg2, time);

                // Radius: inner rings are smooth circles, jaggedness increases toward edge
                float rInner1 = baseRadius * fracInner * lerp(1.0f, edge1, fracInner);
                float rOuter1 = baseRadius * fracOuter * lerp(1.0f, edge1, fracOuter);
                float rInner2 = baseRadius * fracInner * lerp(1.0f, edge2, fracInner);
                float rOuter2 = baseRadius * fracOuter * lerp(1.0f, edge2, fracOuter);

                float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
                float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

                // Slight Y displacement scales with distance from center
                float h1 = (pseudoRandom(seg * 31L, 5) - 0.5f) * 0.3f;
                float h2 = (pseudoRandom(seg2 * 31L, 5) - 0.5f) * 0.3f;
                float yInner1 = h1 * fracInner * fracInner;
                float yOuter1 = h1 * fracOuter * fracOuter;
                float yInner2 = h2 * fracInner * fracInner;
                float yOuter2 = h2 * fracOuter * fracOuter;

                // Top-facing quad (visible from below — player looks up)
                portal.addVertex(matrix, cos1 * rInner1, yInner1, sin1 * rInner1);
                portal.addVertex(matrix, cos1 * rOuter1, yOuter1, sin1 * rOuter1);
                portal.addVertex(matrix, cos2 * rOuter2, yOuter2, sin2 * rOuter2);
                portal.addVertex(matrix, cos2 * rInner2, yInner2, sin2 * rInner2);

                // Bottom-facing quad (visible from above)
                portal.addVertex(matrix, cos1 * rInner1, yInner1, sin1 * rInner1);
                portal.addVertex(matrix, cos2 * rInner2, yInner2, sin2 * rInner2);
                portal.addVertex(matrix, cos2 * rOuter2, yOuter2, sin2 * rOuter2);
                portal.addVertex(matrix, cos1 * rOuter1, yOuter1, sin1 * rOuter1);
            }
        }
    }

    /**
     * Returns a multiplier for the rift radius at a given segment.
     * Very gentle variation — smooth organic edge, not spiky.
     */
    private float getJaggedRadius(int segment, float time) {
        float s = segment * 1.0f;
        float slow = (float) Math.sin(s * 0.3f + time * 0.004f) * 0.04f;
        float med = (float) Math.sin(s * 0.8f + time * 0.01f) * 0.025f;
        float staticVar = pseudoRandom(segment * 7919L, 3) * 0.04f - 0.02f;
        return 0.98f + slow + med + staticVar;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** Emissive edge glow around the rift boundary. */
    private void renderRiftEdgeGlow(PoseStack poseStack, VertexConsumer emissive,
                                     int light, float[] color, int tier, float time) {
        PoseStack.Pose pose = poseStack.last();
        float baseRadius = 7.5f + tier * 1.8f;

        float glowWidth = 0.4f + tier * 0.06f;
        float pulse = 0.7f + 0.3f * (float) Math.sin(time * 0.06);

        // Bright white glow with a hint of tier color
        int gr = c255((color[0] * 0.3f + 0.7f) * 255 * pulse);
        int gg = c255((color[1] * 0.3f + 0.7f) * 255 * pulse);
        int gb = c255((color[2] * 0.3f + 0.7f) * 255 * pulse);

        for (int seg = 0; seg < RIFT_SEGMENTS; seg++) {
            int seg2 = (seg + 1) % RIFT_SEGMENTS;
            float a1 = seg * 2.0f * (float) Math.PI / RIFT_SEGMENTS;
            float a2 = (seg + 1) * 2.0f * (float) Math.PI / RIFT_SEGMENTS;

            float edgeR1 = baseRadius * getJaggedRadius(seg, time);
            float edgeR2 = baseRadius * getJaggedRadius(seg2, time);

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            float rIn1 = edgeR1 - glowWidth * 0.5f, rOut1 = edgeR1 + glowWidth * 0.5f;
            float rIn2 = edgeR2 - glowWidth * 0.5f, rOut2 = edgeR2 + glowWidth * 0.5f;

            v(emissive, pose, cos1 * rIn1, 0, sin1 * rIn1, gr, gg, gb, c255(100 * pulse), light);
            v(emissive, pose, cos1 * rOut1, 0, sin1 * rOut1, gr, gg, gb, c255(20 * pulse), light);
            v(emissive, pose, cos2 * rOut2, 0, sin2 * rOut2, gr, gg, gb, c255(20 * pulse), light);
            v(emissive, pose, cos2 * rIn2, 0, sin2 * rIn2, gr, gg, gb, c255(100 * pulse), light);
        }
    }

    // =========================================================================
    // 3. Lightning — very sparse
    // =========================================================================

    private void renderLightning(PoseStack poseStack, VertexConsumer consumer,
                                  int light, float[] color, int tier,
                                  float beamHeight, long gameTime) {
        PoseStack.Pose pose = poseStack.last();

        long seed = (gameTime / 10) * 7919L;
        long boltSeed = seed + 104729L;

        int segments = 3 + tier;
        float maxLength = 2.0f + tier * 0.8f;

        float startY = pseudoRandom(boltSeed, 0) * beamHeight * 0.5f + beamHeight * 0.25f;
        float dirAngle = pseudoRandom(boltSeed, 1) * (float) (2.0 * Math.PI);
        float dirX = (float) Math.cos(dirAngle);
        float dirZ = (float) Math.sin(dirAngle);

        float segLen = maxLength / segments;
        float x = 0, y = startY, z = 0;

        for (int seg = 0; seg < segments; seg++) {
            float jitterY = (pseudoRandom(boltSeed, 10 + seg * 3) - 0.5f) * 1.2f;
            float jitterPerp = (pseudoRandom(boltSeed, 11 + seg * 3) - 0.5f) * 0.8f;

            float nx = x + dirX * segLen + (-dirZ) * jitterPerp;
            float ny = y + jitterY;
            float nz = z + dirZ * segLen + dirX * jitterPerp;

            float fade = 1.0f - (float) seg / segments * 0.5f;
            float thickness = (0.03f + tier * 0.008f) * fade;

            int br = c255((color[0] * 0.4f + 0.6f) * 255 * fade);
            int bg = c255((color[1] * 0.4f + 0.6f) * 255 * fade);
            int bb = c255((color[2] * 0.4f + 0.6f) * 255 * fade);
            renderLineQuad(consumer, pose, light,
                    x, y, z, nx, ny, nz, thickness,
                    br, bg, bb, c255(200 * fade));

            renderLineQuad(consumer, pose, light,
                    x, y, z, nx, ny, nz, thickness * 0.3f,
                    255, 255, 255, c255(150 * fade));

            x = nx; y = ny; z = nz;
        }
    }

    private void renderLineQuad(VertexConsumer consumer, PoseStack.Pose pose, int light,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float thickness,
                                 int r, int g, int b, int a) {
        float ddx = x2 - x1, ddy = y2 - y1, ddz = z2 - z1;
        float len = (float) Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);
        if (len < 0.001f) return;

        float px, py, pz;
        if (Math.abs(ddy) < 0.9f * len) {
            px = -ddz; py = 0; pz = ddx;
        } else {
            px = 0; py = ddz; pz = -ddy;
        }
        float plen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (plen < 0.001f) return;
        px = px / plen * thickness;
        py = py / plen * thickness;
        pz = pz / plen * thickness;

        v(consumer, pose, x1 - px, y1 - py, z1 - pz, r, g, b, a, light);
        v(consumer, pose, x1 + px, y1 + py, z1 + pz, r, g, b, a, light);
        v(consumer, pose, x2 + px, y2 + py, z2 + pz, r, g, b, a, light);
        v(consumer, pose, x2 - px, y2 - py, z2 - pz, r, g, b, a, light);
    }

    // =========================================================================
    // 4. Falling items — spiral down from rift, multiple staggered, slower
    // =========================================================================

    private void renderFallingItem(PoseStack poseStack, MultiBufferSource bufferSource,
                                    ItemStack item, float startY, float endY,
                                    float progress, float time, int itemIndex,
                                    net.minecraft.world.level.Level level) {
        float easedProgress = progress * progress;
        float y = lerp(startY, endY, easedProgress);

        float angleOffset = itemIndex * 2.0f * (float) Math.PI / ITEM_COUNT;
        float spiralAngle = progress * 6.0f * (float) Math.PI + angleOffset;
        float spiralRadius = 1.2f * (1.0f - progress * 0.7f);
        float sx = (float) Math.cos(spiralAngle) * spiralRadius;
        float sz = (float) Math.sin(spiralAngle) * spiralRadius;

        float spin = time * 3.0f + itemIndex * 120.0f;

        poseStack.pushPose();
        poseStack.translate(sx, y, sz);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));

        float scale = 1.5f + progress * 0.5f;
        poseStack.scale(scale, scale, scale);

        mc.getItemRenderer().renderStatic(
                item,
                ItemDisplayContext.GROUND,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                0
        );

        poseStack.popPose();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Vertex helper for emissive geometry (full vertex format). */
    private void v(VertexConsumer consumer, PoseStack.Pose pose,
                   float x, float y, float z, int r, int g, int b, int a, int light) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(0.5f, 0.5f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(pose, 0, 1, 0);
    }

    private static int c255(float v) {
        return Math.max(0, Math.min(255, (int) v));
    }

    private static float pseudoRandom(long seed, int index) {
        long h = seed + index * 6364136223846793005L;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        h = h ^ (h >>> 33);
        return (float) ((h & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL);
    }

    // =========================================================================
    // Render bounds
    // =========================================================================

    @Override
    public boolean shouldRenderOffScreen(VoidMinerControllerBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(VoidMinerControllerBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        BlockPos origin = be.getStructureOrigin();
        BlockPos beamTop = be.getBeamTop();

        if (origin != null && beamTop != null) {
            int tier = be.getStructureTier();
            int structureSize = 3 + tier * 2;
            float riftRadius = (7.5f + tier * 1.8f) * 1.3f;
            int margin = (int) Math.max(structureSize + 5, riftRadius + 5);
            return new AABB(
                    origin.getX() - margin, origin.getY() - 2, origin.getZ() - margin,
                    origin.getX() + structureSize + margin, beamTop.getY() + 10,
                    origin.getZ() + structureSize + margin
            );
        }

        return new AABB(
                pos.getX() - 25, pos.getY() - 5, pos.getZ() - 25,
                pos.getX() + 26, pos.getY() + 50, pos.getZ() + 26
        );
    }
}
