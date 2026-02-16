package com.setusertso.tachyon.block;

import java.util.ArrayList;
import java.util.List;

import com.setusertso.tachyon.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Procedural validation for the Void Miner multiblock.
 *
 * Environmental Tech-inspired design: frame blocks on edges + solid floor,
 * with open faces for visibility. Each tier wraps the previous creating
 * a layered cage/pillar effect.
 *
 * Tier 1: 5x5x5 (void_frame) — edge pillars + solid floor
 * Tier 2: 7x7x7 shell around T1 (stabilized_void_frame)
 * Tier 3: 9x9x9 shell around T2 (reinforced_void_frame)
 * Tier 4: 11x11x11 shell around T3 (quantum_void_frame)
 *
 * Each shell has:
 *   - All 12 edges filled (pillars at corners, beams along edges)
 *   - Solid bottom face (floor)
 *   - Open side faces and top face (except for edges)
 *   - 3x3 chimney opening in top face center (already open since faces are open)
 *
 * Requires sky access (10 blocks of air) above the chimney.
 */
public class VoidMinerPattern {

    public static final int[] SHELL_SIZES = {0, 5, 7, 9, 11}; // indexed by tier
    public static final int INTERIOR_SIZE = 3; // 3x3x3 hollow center
    private static final int SKY_ACCESS_HEIGHT = 10;

    public record ValidationResult(
            boolean valid,
            List<BlockPos> structurePositions,
            int tier,
            BlockPos origin,
            BlockPos riftCenter,
            BlockPos beamTop
    ) {}

    /**
     * Validate the void miner structure from the controller position.
     * Tries each tier from highest to lowest.
     */
    public static ValidationResult validate(Level level, BlockPos controllerPos) {
        for (int tier = 4; tier >= 1; tier--) {
            int outerSize = SHELL_SIZES[tier];
            // Try every possible structure position as the controller location
            for (int y = 0; y < outerSize; y++) {
                for (int x = 0; x < outerSize; x++) {
                    for (int z = 0; z < outerSize; z++) {
                        if (!isStructureBlock(x, y, z, outerSize)) continue;
                        // This position on the outer shell could be the controller
                        int originX = controllerPos.getX() - x;
                        int originY = controllerPos.getY() - y;
                        int originZ = controllerPos.getZ() - z;
                        BlockPos origin = new BlockPos(originX, originY, originZ);

                        ValidationResult result = validateAtOrigin(level, controllerPos, origin, tier);
                        if (result.valid()) {
                            return result;
                        }
                    }
                }
            }
        }
        return new ValidationResult(false, List.of(), 0, null, null, null);
    }

    private static ValidationResult validateAtOrigin(Level level, BlockPos controllerPos,
                                                      BlockPos origin, int maxTier) {
        List<BlockPos> structurePositions = new ArrayList<>();
        int outerSize = SHELL_SIZES[maxTier];

        // Validate each shell from outermost down to 1
        for (int tier = maxTier; tier >= 1; tier--) {
            int shellSize = SHELL_SIZES[tier];
            int offset = (outerSize - shellSize) / 2;

            for (int y = 0; y < shellSize; y++) {
                for (int x = 0; x < shellSize; x++) {
                    for (int z = 0; z < shellSize; z++) {
                        if (!isStructureBlock(x, y, z, shellSize)) continue;

                        BlockPos worldPos = new BlockPos(
                                origin.getX() + offset + x,
                                origin.getY() + offset + y,
                                origin.getZ() + offset + z
                        );

                        if (worldPos.equals(controllerPos)) {
                            // Controller is always valid
                            structurePositions.add(worldPos);
                            continue;
                        }

                        BlockState state = level.getBlockState(worldPos);
                        Block block = state.getBlock();

                        // Must be the correct frame type for this tier, OR controller/port
                        if (block == ModBlocks.VOID_MINER_CONTROLLER.get()
                                || block == ModBlocks.VOID_MINER_PORT.get()) {
                            structurePositions.add(worldPos);
                            continue;
                        }

                        Block requiredBlock = getFrameBlockForTier(tier);
                        if (block != requiredBlock) {
                            return new ValidationResult(false, List.of(), 0, null, null, null);
                        }

                        structurePositions.add(worldPos);
                    }
                }
            }
        }

        // Validate 3x3x3 interior is air
        int interiorOffset = (outerSize - INTERIOR_SIZE) / 2;
        for (int y = 0; y < INTERIOR_SIZE; y++) {
            for (int x = 0; x < INTERIOR_SIZE; x++) {
                for (int z = 0; z < INTERIOR_SIZE; z++) {
                    BlockPos airPos = new BlockPos(
                            origin.getX() + interiorOffset + x,
                            origin.getY() + interiorOffset + y,
                            origin.getZ() + interiorOffset + z
                    );
                    if (!level.getBlockState(airPos).isAir()) {
                        return new ValidationResult(false, List.of(), 0, null, null, null);
                    }
                }
            }
        }

        // Validate chimney is air (3x3 column from top of inner shell up through structure)
        int chimneyStartY = origin.getY() + interiorOffset + INTERIOR_SIZE;
        int chimneyEndY = origin.getY() + outerSize;
        int chimneyXStart = origin.getX() + (outerSize - INTERIOR_SIZE) / 2;
        int chimneyZStart = origin.getZ() + (outerSize - INTERIOR_SIZE) / 2;
        for (int y = chimneyStartY; y < chimneyEndY; y++) {
            for (int x = 0; x < INTERIOR_SIZE; x++) {
                for (int z = 0; z < INTERIOR_SIZE; z++) {
                    BlockPos chimneyPos = new BlockPos(chimneyXStart + x, y, chimneyZStart + z);
                    if (!level.getBlockState(chimneyPos).isAir()) {
                        return new ValidationResult(false, List.of(), 0, null, null, null);
                    }
                }
            }
        }

        // Validate sky access: 10 blocks of air above the structure
        if (!checkSkyAccess(level, origin, outerSize)) {
            return new ValidationResult(false, List.of(), 0, null, null, null);
        }

        int center = outerSize / 2;
        BlockPos riftCenter = new BlockPos(
                origin.getX() + center,
                origin.getY() + center,
                origin.getZ() + center
        );

        // Beam top: top of structure + beam height
        int beamHeight = getBeamHeight(maxTier);
        BlockPos beamTop = new BlockPos(
                origin.getX() + center,
                origin.getY() + outerSize + beamHeight,
                origin.getZ() + center
        );

        return new ValidationResult(true, structurePositions, maxTier, origin, riftCenter, beamTop);
    }

    /**
     * Determines if a local position (x,y,z) within a shell of given size
     * should have a structure block placed.
     *
     * Design: Edge frame + solid floor
     * - Bottom face (y==0): fully solid
     * - Edges: all 12 edges of the cube are filled (where 2+ face coords are at min/max)
     * - Side/top faces interior: air (open)
     *
     * This creates a cage/pillar look with a solid foundation.
     */
    public static boolean isStructureBlock(int x, int y, int z, int size) {
        int max = size - 1;

        // Not on any face at all = interior, skip
        if (x > 0 && x < max && y > 0 && y < max && z > 0 && z < max) {
            return false;
        }

        // Bottom face (y == 0): solid floor
        if (y == 0) {
            return true;
        }

        // Edge detection: a position is on an edge if it's at the boundary
        // of at least 2 out of 3 axes (i.e., sits where two faces meet)
        int edgeCount = 0;
        if (x == 0 || x == max) edgeCount++;
        if (y == 0 || y == max) edgeCount++;
        if (z == 0 || z == max) edgeCount++;

        // On an edge (or corner) = structure block
        if (edgeCount >= 2) {
            // But skip the chimney hole area on the top face edges
            if (y == max) {
                int center = size / 2;
                if (x >= center - 1 && x <= center + 1 && z >= center - 1 && z <= center + 1) {
                    return false; // chimney opening
                }
            }
            return true;
        }

        // Single-face interior positions: air (open faces)
        return false;
    }

    /**
     * Check if a position is part of the 3x3 chimney hole on the top face.
     * Used by the build command.
     */
    public static boolean isChimneyHole(int x, int y, int z, int size) {
        if (y != size - 1) return false;
        int center = size / 2;
        return x >= center - 1 && x <= center + 1
                && z >= center - 1 && z <= center + 1;
    }

    /**
     * Get the correct frame block for a tier.
     */
    public static Block getFrameBlockForTier(int tier) {
        return switch (tier) {
            case 1 -> ModBlocks.VOID_FRAME.get();
            case 2 -> ModBlocks.STABILIZED_VOID_FRAME.get();
            case 3 -> ModBlocks.REINFORCED_VOID_FRAME.get();
            case 4 -> ModBlocks.QUANTUM_VOID_FRAME.get();
            default -> ModBlocks.VOID_FRAME.get();
        };
    }

    /**
     * Get the shell size for a tier.
     */
    public static int getShellSize(int tier) {
        if (tier < 1 || tier > 4) return 5;
        return SHELL_SIZES[tier];
    }

    /**
     * Get the beam height for a tier.
     */
    public static int getBeamHeight(int tier) {
        return switch (tier) {
            case 1 -> 12;
            case 2 -> 18;
            case 3 -> 25;
            case 4 -> 35;
            default -> 12;
        };
    }

    /**
     * Check for sky access above the chimney.
     */
    public static boolean checkSkyAccess(Level level, BlockPos origin, int outerSize) {
        int center = outerSize / 2;
        int topY = origin.getY() + outerSize;
        int chimneyXStart = origin.getX() + center - 1;
        int chimneyZStart = origin.getZ() + center - 1;

        for (int dy = 0; dy < SKY_ACCESS_HEIGHT; dy++) {
            for (int x = 0; x < INTERIOR_SIZE; x++) {
                for (int z = 0; z < INTERIOR_SIZE; z++) {
                    BlockPos checkPos = new BlockPos(chimneyXStart + x, topY + dy, chimneyZStart + z);
                    if (!level.getBlockState(checkPos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Get the rift center position given origin and tier.
     */
    public static BlockPos getRiftCenter(BlockPos origin, int tier) {
        int outerSize = SHELL_SIZES[tier];
        int center = outerSize / 2;
        return new BlockPos(origin.getX() + center, origin.getY() + center, origin.getZ() + center);
    }
}
