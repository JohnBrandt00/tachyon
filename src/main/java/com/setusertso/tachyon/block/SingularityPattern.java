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
import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.tachyon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SingularityPattern {

    public record ValidationResult(boolean valid, List<BlockPos> structurePositions, List<BlockPos> corePositions) {}

    private static Map<Integer, List<int[]>> shellPositions;
    private static Map<Integer, List<int[]>> airPositions;
    private static int[] controllerOffset; // [x, y, z] from center
    private static int height;
    private static List<ResourceLocation>[] layerAccepts;
    private static int requiredCores;
    private static boolean loaded = false;

    @SuppressWarnings("unchecked")
    public static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            InputStream is = SingularityPattern.class.getResourceAsStream(
                    "/data/tachyon/multiblock/singularity_engine.json");
            if (is == null) {
                tachyon.LOGGER.error("Failed to load singularity_engine.json pattern!");
                initEmpty();
                return;
            }
            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
            is.close();

            // Parse controller offset [x, y, z]
            JsonArray ctrlArr = root.getAsJsonArray("controller_offset");
            controllerOffset = new int[]{ctrlArr.get(0).getAsInt(), ctrlArr.get(1).getAsInt(), ctrlArr.get(2).getAsInt()};

            height = root.get("height").getAsInt();

            // Parse layers array — each layer has y, shell, air, accepts
            shellPositions = new HashMap<>();
            airPositions = new HashMap<>();
            layerAccepts = new List[height];

            JsonArray layersArr = root.getAsJsonArray("layers");
            for (JsonElement layerElem : layersArr) {
                JsonObject layerObj = layerElem.getAsJsonObject();
                int y = layerObj.get("y").getAsInt();

                // Parse shell positions for this layer
                List<int[]> shellOffsets = new ArrayList<>();
                JsonArray shellArr = layerObj.getAsJsonArray("shell");
                for (JsonElement elem : shellArr) {
                    JsonArray pair = elem.getAsJsonArray();
                    shellOffsets.add(new int[]{pair.get(0).getAsInt(), pair.get(1).getAsInt()});
                }
                shellPositions.put(y, shellOffsets);

                // Parse air positions for this layer
                List<int[]> airOffsets = new ArrayList<>();
                JsonArray airArr = layerObj.getAsJsonArray("air");
                if (airArr != null) {
                    for (JsonElement elem : airArr) {
                        JsonArray pair = elem.getAsJsonArray();
                        airOffsets.add(new int[]{pair.get(0).getAsInt(), pair.get(1).getAsInt()});
                    }
                }
                airPositions.put(y, airOffsets);

                // Parse accepts for this layer
                if (y < height) {
                    layerAccepts[y] = new ArrayList<>();
                    JsonArray accepts = layerObj.getAsJsonArray("accepts");
                    for (JsonElement a : accepts) {
                        layerAccepts[y].add(ResourceLocation.parse(a.getAsString()));
                    }
                }
            }

            // Parse required blocks
            requiredCores = 1;
            if (root.has("required_blocks")) {
                JsonObject req = root.getAsJsonObject("required_blocks");
                if (req.has("tachyon:exotic_matter_core")) {
                    requiredCores = req.get("tachyon:exotic_matter_core").getAsInt();
                }
            }

            int totalShell = 0;
            int totalAir = 0;
            for (int y = 0; y < height; y++) {
                totalShell += shellPositions.getOrDefault(y, List.of()).size();
                totalAir += airPositions.getOrDefault(y, List.of()).size();
            }
            tachyon.LOGGER.info("Loaded singularity sphere: {} layers, {} shell blocks, {} air blocks",
                    height, totalShell, totalAir);
        } catch (Exception e) {
            tachyon.LOGGER.error("Error loading singularity pattern", e);
            initEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    private static void initEmpty() {
        shellPositions = new HashMap<>();
        airPositions = new HashMap<>();
        controllerOffset = new int[]{0, 0, 0};
        layerAccepts = new List[0];
        height = 0;
        requiredCores = 1;
    }

    /**
     * Get the world position of the sphere center from the controller position.
     * Controller is at offset [-5, 0, 0] from center, so center = controller - offset.
     */
    public static BlockPos getCenterPosition(BlockPos controllerPos) {
        ensureLoaded();
        return new BlockPos(
                controllerPos.getX() - controllerOffset[0],
                controllerPos.getY() - controllerOffset[1],
                controllerPos.getZ() - controllerOffset[2]
        );
    }

    /**
     * Validate the sphere structure and return positions + core locations.
     * No rotation needed — sphere is symmetric.
     */
    public static ValidationResult validate(Level level, BlockPos controllerPos) {
        ensureLoaded();

        BlockPos center = getCenterPosition(controllerPos);
        int radius = height / 2;
        int baseY = center.getY() - radius;

        List<BlockPos> structurePositions = new ArrayList<>();
        List<BlockPos> corePositions = new ArrayList<>();

        // Validate shell positions
        for (int layer = 0; layer < height; layer++) {
            List<int[]> offsets = shellPositions.getOrDefault(layer, List.of());
            List<ResourceLocation> accepts = layer < layerAccepts.length ? layerAccepts[layer] : List.of();

            for (int[] offset : offsets) {
                BlockPos checkPos = new BlockPos(center.getX() + offset[0], baseY + layer, center.getZ() + offset[1]);

                // Skip the controller position itself
                if (checkPos.equals(controllerPos)) {
                    structurePositions.add(checkPos);
                    continue;
                }

                BlockState state = level.getBlockState(checkPos);
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

                if (!accepts.contains(blockId)) {
                    return new ValidationResult(false, List.of(), List.of());
                }

                structurePositions.add(checkPos);

                // Track exotic matter cores
                if (state.getBlock() == ModBlocks.EXOTIC_MATTER_CORE.get()) {
                    corePositions.add(checkPos);
                }
            }
        }

        // Validate air positions (interior must be air)
        for (int layer = 0; layer < height; layer++) {
            List<int[]> offsets = airPositions.getOrDefault(layer, List.of());
            for (int[] offset : offsets) {
                BlockPos airPos = new BlockPos(center.getX() + offset[0], baseY + layer, center.getZ() + offset[1]);
                if (!level.getBlockState(airPos).isAir()) {
                    return new ValidationResult(false, List.of(), List.of());
                }
            }
        }

        // Check required cores
        if (corePositions.size() < requiredCores) {
            return new ValidationResult(false, List.of(), List.of());
        }

        return new ValidationResult(true, structurePositions, corePositions);
    }

    public static int getHeight() {
        ensureLoaded();
        return height;
    }

    public static List<int[]> getShellOffsetsForLayer(int layer) {
        ensureLoaded();
        return shellPositions.getOrDefault(layer, List.of());
    }

    public static List<int[]> getAirOffsetsForLayer(int layer) {
        ensureLoaded();
        return airPositions.getOrDefault(layer, List.of());
    }

    public static int[] getControllerOffset() {
        ensureLoaded();
        return controllerOffset;
    }
}
