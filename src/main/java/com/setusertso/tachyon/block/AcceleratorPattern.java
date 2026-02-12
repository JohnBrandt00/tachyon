package com.setusertso.tachyon.block;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.setusertso.tachyon.tachyon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AcceleratorPattern {

    public record RingOffset(int localX, int localZ) {}

    public record LayerRule(List<ResourceLocation> blockIds, List<TagKey<Block>> blockTags) {
        public boolean matches(BlockState state) {
            Block block = state.getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            for (ResourceLocation accepted : blockIds) {
                if (accepted.equals(id)) return true;
            }
            for (TagKey<Block> tag : blockTags) {
                if (state.is(tag)) return true;
            }
            return false;
        }
    }

    private static List<RingOffset> ringOffsets;
    private static int[] controllerOffset;
    private static LayerRule[] layerRules;
    // Key: "layer,localX,localZ" -> per-position override rule
    private static Map<String, LayerRule> positionOverrides;
    private static int height;
    private static boolean loaded = false;

    public static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            InputStream is = AcceleratorPattern.class.getResourceAsStream(
                    "/data/tachyon/multiblock/particle_accelerator.json");
            if (is == null) {
                tachyon.LOGGER.error("Failed to load particle_accelerator.json pattern!");
                ringOffsets = List.of();
                controllerOffset = new int[]{0, 0};
                layerRules = new LayerRule[0];
                height = 0;
                return;
            }
            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
            is.close();

            // Parse controller offset
            JsonArray ctrlArr = root.getAsJsonArray("controller_offset");
            controllerOffset = new int[]{ctrlArr.get(0).getAsInt(), ctrlArr.get(1).getAsInt()};

            // Parse ring positions
            JsonArray posArr = root.getAsJsonArray("ring_positions");
            ringOffsets = new ArrayList<>();
            for (JsonElement elem : posArr) {
                JsonArray pair = elem.getAsJsonArray();
                ringOffsets.add(new RingOffset(pair.get(0).getAsInt(), pair.get(1).getAsInt()));
            }

            // Parse height
            height = root.get("height").getAsInt();

            // Parse layer rules
            JsonObject layers = root.getAsJsonObject("layers");
            layerRules = new LayerRule[height];
            for (int y = 0; y < height; y++) {
                JsonObject layer = layers.getAsJsonObject(String.valueOf(y));
                JsonArray accepts = layer.getAsJsonArray("accepts");
                List<ResourceLocation> blockIds = new ArrayList<>();
                List<TagKey<Block>> blockTags = new ArrayList<>();
                for (JsonElement a : accepts) {
                    String s = a.getAsString();
                    if (s.startsWith("#")) {
                        blockTags.add(TagKey.create(Registries.BLOCK,
                                ResourceLocation.parse(s.substring(1))));
                    } else {
                        blockIds.add(ResourceLocation.parse(s));
                    }
                }
                layerRules[y] = new LayerRule(blockIds, blockTags);
            }

            // Parse position overrides (optional)
            positionOverrides = new HashMap<>();
            if (root.has("position_overrides")) {
                JsonObject overrides = root.getAsJsonObject("position_overrides");
                for (String layerKey : overrides.keySet()) {
                    JsonObject layerOverrides = overrides.getAsJsonObject(layerKey);
                    for (String posKey : layerOverrides.keySet()) {
                        JsonObject overrideObj = layerOverrides.getAsJsonObject(posKey);
                        JsonArray accepts = overrideObj.getAsJsonArray("accepts");
                        List<ResourceLocation> blockIds = new ArrayList<>();
                        List<TagKey<Block>> blockTags = new ArrayList<>();
                        for (JsonElement a : accepts) {
                            String s = a.getAsString();
                            if (s.startsWith("#")) {
                                blockTags.add(TagKey.create(Registries.BLOCK,
                                        ResourceLocation.parse(s.substring(1))));
                            } else {
                                blockIds.add(ResourceLocation.parse(s));
                            }
                        }
                        // Store as "layer,x,z" key
                        String normalizedKey = layerKey + "," + posKey;
                        positionOverrides.put(normalizedKey, new LayerRule(blockIds, blockTags));
                    }
                }
            }

            tachyon.LOGGER.info("Loaded accelerator pattern: {} ring positions, {} layers, {} overrides",
                    ringOffsets.size(), height, positionOverrides.size());
        } catch (Exception e) {
            tachyon.LOGGER.error("Error loading accelerator pattern", e);
            ringOffsets = List.of();
            controllerOffset = new int[]{0, 0};
            layerRules = new LayerRule[0];
            positionOverrides = new HashMap<>();
            height = 0;
        }
    }

    public static int getHeight() {
        ensureLoaded();
        return height;
    }

    public static List<RingOffset> getRingOffsets() {
        ensureLoaded();
        return ringOffsets;
    }

    /**
     * Compute all world positions for the ring given controller pos and facing.
     * The controller occupies one of the ring positions (the controller_offset position).
     * Returns positions for all layers (y=0, y=1, y=2).
     */
    public static List<BlockPos> computeWorldPositions(BlockPos controllerPos, Direction facing) {
        ensureLoaded();
        List<BlockPos> positions = new ArrayList<>();
        int baseY = controllerPos.getY();

        for (RingOffset offset : ringOffsets) {
            // local coordinates relative to ring center
            int localX = offset.localX();
            int localZ = offset.localZ();

            // Rotate based on facing
            int worldDx, worldDz;
            switch (facing) {
                case NORTH -> { worldDx = localZ; worldDz = -localX; }
                case SOUTH -> { worldDx = -localZ; worldDz = localX; }
                case EAST  -> { worldDx = localX; worldDz = localZ; }
                case WEST  -> { worldDx = -localX; worldDz = -localZ; }
                default    -> { worldDx = localX; worldDz = localZ; }
            }

            // Translate: ring center is at controllerPos + rotated(-controllerOffset)
            int ctrlLocalX = controllerOffset[0];
            int ctrlLocalZ = controllerOffset[1];
            int ctrlDx, ctrlDz;
            switch (facing) {
                case NORTH -> { ctrlDx = ctrlLocalZ; ctrlDz = -ctrlLocalX; }
                case SOUTH -> { ctrlDx = -ctrlLocalZ; ctrlDz = ctrlLocalX; }
                case EAST  -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
                case WEST  -> { ctrlDx = -ctrlLocalX; ctrlDz = -ctrlLocalZ; }
                default    -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
            }

            int finalX = controllerPos.getX() - ctrlDx + worldDx;
            int finalZ = controllerPos.getZ() - ctrlDz + worldDz;

            for (int y = 0; y < height; y++) {
                positions.add(new BlockPos(finalX, baseY + y, finalZ));
            }
        }
        return positions;
    }

    /**
     * Compute the ordered ring path (for particle rendering) at a specific Y layer.
     * Positions are sorted by angle around the ring center.
     */
    public static List<BlockPos> computeRingPath(BlockPos controllerPos, Direction facing, int layer) {
        ensureLoaded();
        List<BlockPos> path = new ArrayList<>();
        int baseY = controllerPos.getY();

        int ctrlLocalX = controllerOffset[0];
        int ctrlLocalZ = controllerOffset[1];
        int ctrlDx, ctrlDz;
        switch (facing) {
            case NORTH -> { ctrlDx = ctrlLocalZ; ctrlDz = -ctrlLocalX; }
            case SOUTH -> { ctrlDx = -ctrlLocalZ; ctrlDz = ctrlLocalX; }
            case EAST  -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
            case WEST  -> { ctrlDx = -ctrlLocalX; ctrlDz = -ctrlLocalZ; }
            default    -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
        }

        int centerX = controllerPos.getX() - ctrlDx;
        int centerZ = controllerPos.getZ() - ctrlDz;

        for (RingOffset offset : ringOffsets) {
            int worldDx, worldDz;
            switch (facing) {
                case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
            }
            path.add(new BlockPos(centerX + worldDx, baseY + layer, centerZ + worldDz));
        }

        // Sort by angle around center
        path.sort(Comparator.comparingDouble(p ->
                Math.atan2(p.getZ() - centerZ, p.getX() - centerX)));

        return path;
    }

    /**
     * Validate the structure. Returns true if all positions are valid.
     * The controller position itself is skipped (it's already the controller block).
     */
    public static boolean validate(Level level, BlockPos controllerPos, Direction facing) {
        ensureLoaded();
        int baseY = controllerPos.getY();

        int ctrlLocalX = controllerOffset[0];
        int ctrlLocalZ = controllerOffset[1];
        int ctrlDx, ctrlDz;
        switch (facing) {
            case NORTH -> { ctrlDx = ctrlLocalZ; ctrlDz = -ctrlLocalX; }
            case SOUTH -> { ctrlDx = -ctrlLocalZ; ctrlDz = ctrlLocalX; }
            case EAST  -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
            case WEST  -> { ctrlDx = -ctrlLocalX; ctrlDz = -ctrlLocalZ; }
            default    -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
        }

        for (RingOffset offset : ringOffsets) {
            int worldDx, worldDz;
            switch (facing) {
                case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
            }

            int finalX = controllerPos.getX() - ctrlDx + worldDx;
            int finalZ = controllerPos.getZ() - ctrlDz + worldDz;

            for (int y = 0; y < height; y++) {
                BlockPos checkPos = new BlockPos(finalX, baseY + y, finalZ);

                // Skip the controller position itself
                if (checkPos.equals(controllerPos)) continue;

                BlockState state = level.getBlockState(checkPos);

                // Check per-position override first, fall back to layer rule
                String overrideKey = y + "," + offset.localX() + "," + offset.localZ();
                LayerRule rule = positionOverrides.getOrDefault(overrideKey, layerRules[y]);
                if (!rule.matches(state)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get all structure positions excluding the controller position itself.
     * Used by the controller to track slave blocks.
     */
    public static List<BlockPos> getStructurePositions(BlockPos controllerPos, Direction facing) {
        List<BlockPos> all = computeWorldPositions(controllerPos, facing);
        all.removeIf(p -> p.equals(controllerPos));
        return all;
    }

    /**
     * Check if a given ring position + layer has a position override that accepts glass.
     */
    public static boolean isGlassPosition(int layer, int localX, int localZ) {
        ensureLoaded();
        String key = layer + "," + localX + "," + localZ;
        LayerRule rule = positionOverrides.get(key);
        if (rule == null) return false;
        for (TagKey<Block> tag : rule.blockTags()) {
            if (tag.location().getPath().contains("glass")) return true;
        }
        return false;
    }

    public static int[] getControllerOffset() {
        ensureLoaded();
        return controllerOffset;
    }
}
