package com.setusertso.tachyon.block;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    // Explicit positions per layer (read directly from JSON)
    private static Map<Integer, List<RingOffset>> shellPositions;
    private static Map<Integer, List<RingOffset>> airPositions;
    private static List<RingOffset> ringPathOffsets;

    private static int[] controllerOffset;
    private static LayerRule[] layerRules;
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
                initEmpty();
                return;
            }
            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
            is.close();

            // Parse controller offset
            JsonArray ctrlArr = root.getAsJsonArray("controller_offset");
            controllerOffset = new int[]{ctrlArr.get(0).getAsInt(), ctrlArr.get(1).getAsInt()};

            height = root.get("height").getAsInt();

            // Parse shell positions (explicit per-layer lists)
            shellPositions = new HashMap<>();
            JsonObject shellObj = root.getAsJsonObject("shell");
            for (String layerKey : shellObj.keySet()) {
                int layer = Integer.parseInt(layerKey);
                JsonArray posArr = shellObj.getAsJsonArray(layerKey);
                List<RingOffset> offsets = new ArrayList<>();
                for (JsonElement elem : posArr) {
                    JsonArray pair = elem.getAsJsonArray();
                    offsets.add(new RingOffset(pair.get(0).getAsInt(), pair.get(1).getAsInt()));
                }
                shellPositions.put(layer, offsets);
            }

            // Parse air positions (explicit per-layer lists)
            airPositions = new HashMap<>();
            if (root.has("air")) {
                JsonObject airObj = root.getAsJsonObject("air");
                for (String layerKey : airObj.keySet()) {
                    int layer = Integer.parseInt(layerKey);
                    JsonArray posArr = airObj.getAsJsonArray(layerKey);
                    List<RingOffset> offsets = new ArrayList<>();
                    for (JsonElement elem : posArr) {
                        JsonArray pair = elem.getAsJsonArray();
                        offsets.add(new RingOffset(pair.get(0).getAsInt(), pair.get(1).getAsInt()));
                    }
                    airPositions.put(layer, offsets);
                }
            }

            // Parse ring path (ordered positions for particle animation)
            ringPathOffsets = new ArrayList<>();
            if (root.has("ring_path")) {
                JsonArray pathArr = root.getAsJsonArray("ring_path");
                for (JsonElement elem : pathArr) {
                    JsonArray pair = elem.getAsJsonArray();
                    ringPathOffsets.add(new RingOffset(pair.get(0).getAsInt(), pair.get(1).getAsInt()));
                }
            }

            // Parse layer rules
            JsonObject layers = root.getAsJsonObject("layers");
            layerRules = new LayerRule[height];
            for (int y = 0; y < height; y++) {
                JsonObject layer = layers.getAsJsonObject(String.valueOf(y));
                layerRules[y] = parseLayerRule(layer.getAsJsonArray("accepts"));
            }

            // Parse position overrides (optional)
            positionOverrides = new HashMap<>();
            if (root.has("position_overrides")) {
                JsonObject overrides = root.getAsJsonObject("position_overrides");
                for (String layerKey : overrides.keySet()) {
                    JsonObject layerOverrides = overrides.getAsJsonObject(layerKey);
                    for (String posKey : layerOverrides.keySet()) {
                        JsonObject overrideObj = layerOverrides.getAsJsonObject(posKey);
                        LayerRule rule = parseLayerRule(overrideObj.getAsJsonArray("accepts"));
                        String normalizedKey = layerKey + "," + posKey;
                        positionOverrides.put(normalizedKey, rule);
                    }
                }
            }

            int totalShell = 0;
            int totalAir = 0;
            for (int y = 0; y < height; y++) {
                totalShell += shellPositions.getOrDefault(y, List.of()).size();
                totalAir += airPositions.getOrDefault(y, List.of()).size();
            }
            tachyon.LOGGER.info("Loaded accelerator torus: {} layers, {} shell blocks, {} air blocks, {} ring path positions",
                    height, totalShell, totalAir, ringPathOffsets.size());
        } catch (Exception e) {
            tachyon.LOGGER.error("Error loading accelerator pattern", e);
            initEmpty();
        }
    }

    private static LayerRule parseLayerRule(JsonArray accepts) {
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
        return new LayerRule(blockIds, blockTags);
    }

    private static void initEmpty() {
        shellPositions = new HashMap<>();
        airPositions = new HashMap<>();
        ringPathOffsets = List.of();
        controllerOffset = new int[]{0, 0};
        layerRules = new LayerRule[0];
        positionOverrides = new HashMap<>();
        height = 0;
    }

    public static List<RingOffset> getBlockOffsetsForLayer(int layer) {
        ensureLoaded();
        return shellPositions.getOrDefault(layer, List.of());
    }

    public static List<RingOffset> getAirOffsetsForLayer(int layer) {
        ensureLoaded();
        return airPositions.getOrDefault(layer, List.of());
    }

    public static int getHeight() {
        ensureLoaded();
        return height;
    }

    public static List<RingOffset> getRingPathOffsets() {
        ensureLoaded();
        return ringPathOffsets;
    }

    /**
     * Compute all world positions for shell blocks (all layers).
     */
    public static List<BlockPos> computeWorldPositions(BlockPos controllerPos, Direction facing) {
        ensureLoaded();
        List<BlockPos> positions = new ArrayList<>();
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

        for (int y = 0; y < height; y++) {
            for (RingOffset offset : getBlockOffsetsForLayer(y)) {
                int worldDx, worldDz;
                switch (facing) {
                    case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                    case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                    case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                    case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                    default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                }

                positions.add(new BlockPos(centerX + worldDx, baseY + y, centerZ + worldDz));
            }
        }
        return positions;
    }

    /**
     * Compute the ring path (tube center) at a specific Y layer.
     * Used for particle rendering — the particle orbits through the air tube.
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

        for (RingOffset offset : ringPathOffsets) {
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

        return path;
    }

    /**
     * Validate the torus structure.
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

        int centerX = controllerPos.getX() - ctrlDx;
        int centerZ = controllerPos.getZ() - ctrlDz;

        // Validate shell positions (must be correct block types)
        for (int y = 0; y < height; y++) {
            for (RingOffset offset : getBlockOffsetsForLayer(y)) {
                int worldDx, worldDz;
                switch (facing) {
                    case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                    case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                    case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                    case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                    default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                }

                BlockPos checkPos = new BlockPos(centerX + worldDx, baseY + y, centerZ + worldDz);

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

        // Validate air positions (must be air)
        for (int y = 0; y < height; y++) {
            for (RingOffset offset : getAirOffsetsForLayer(y)) {
                int worldDx, worldDz;
                switch (facing) {
                    case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                    case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                    case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                    case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                    default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                }

                BlockPos tubePos = new BlockPos(centerX + worldDx, baseY + y, centerZ + worldDz);
                if (!level.getBlockState(tubePos).isAir()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get all structure positions excluding the controller position itself.
     */
    public static List<BlockPos> getStructurePositions(BlockPos controllerPos, Direction facing) {
        List<BlockPos> all = computeWorldPositions(controllerPos, facing);
        all.removeIf(p -> p.equals(controllerPos));
        return all;
    }

    /**
     * Check if a given block position + layer has a position override that accepts glass.
     */
    public static boolean isGlassPosition(int layer, int localX, int localZ) {
        ensureLoaded();
        String key = layer + "," + localX + "," + localZ;
        LayerRule rule = positionOverrides.get(key);
        if (rule == null) return false;
        for (ResourceLocation id : rule.blockIds()) {
            if (id.getPath().contains("glass")) return true;
        }
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
