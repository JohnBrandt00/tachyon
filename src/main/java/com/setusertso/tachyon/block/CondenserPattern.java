package com.setusertso.tachyon.block;

import java.util.ArrayList;
import java.util.List;

import com.setusertso.tachyon.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Procedural validation for the Tachyon Condenser multiblock.
 * 3x3x5 vertical tower with hollow 1x1x3 interior column.
 *
 * Layout (controller facing NORTH, controller at front-center-bottom):
 *   Controller is at (0,0,0), structure extends:
 *     - Left/Right: -1 to +1 on the perpendicular horizontal axis
 *     - Back: 0 to +2 along the facing direction (behind the controller)
 *     - Up: 0 to +4
 *
 *   Interior air column: center of the 3x3 footprint, y=1 to y=3
 */
public class CondenserPattern {

    private static final int WIDTH = 3;   // perpendicular to facing
    private static final int DEPTH = 3;   // along facing (controller at front)
    private static final int HEIGHT = 5;

    /**
     * Validate the structure and return the list of structure positions (excluding controller).
     * Returns null if invalid.
     */
    public static List<BlockPos> validate(Level level, BlockPos controllerPos, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();

        // Compute local-to-world direction vectors
        // "right" is 90 degrees clockwise from facing direction
        Direction right = facing.getClockWise();
        // "back" is opposite of facing (controller faces outward, structure is behind it)
        Direction back = facing.getOpposite();

        // Controller is at the front-center-bottom of the structure
        // Origin (local 0,0,0) = controllerPos
        // Local X = right direction (-1 to +1)
        // Local Z = back direction (0 to +2, 0 = controller front face)
        // Local Y = up (0 to +4)

        for (int ly = 0; ly < HEIGHT; ly++) {
            for (int lx = -1; lx <= 1; lx++) {
                for (int lz = 0; lz < DEPTH; lz++) {
                    BlockPos worldPos = controllerPos
                            .relative(right, lx)
                            .relative(back, lz)
                            .above(ly);

                    // Skip controller position itself
                    if (worldPos.equals(controllerPos)) continue;

                    // Interior air column: center (lx=0, lz=1) at y=1,2,3
                    boolean isInterior = (lx == 0 && lz == 1 && ly >= 1 && ly <= 3);

                    if (isInterior) {
                        // Must be air
                        if (!level.getBlockState(worldPos).isAir()) {
                            return null;
                        }
                    } else {
                        // Must be condenser casing, condenser port, or controller
                        Block block = level.getBlockState(worldPos).getBlock();
                        if (block != ModBlocks.CONDENSER_CASING.get()
                                && block != ModBlocks.CONDENSER_PORT.get()) {
                            return null;
                        }
                        positions.add(worldPos);
                    }
                }
            }
        }

        return positions;
    }

    /**
     * Get all structure positions (for use after validation).
     */
    public static List<BlockPos> getStructurePositions(BlockPos controllerPos, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();

        for (int ly = 0; ly < HEIGHT; ly++) {
            for (int lx = -1; lx <= 1; lx++) {
                for (int lz = 0; lz < DEPTH; lz++) {
                    BlockPos worldPos = controllerPos
                            .relative(right, lx)
                            .relative(back, lz)
                            .above(ly);

                    if (worldPos.equals(controllerPos)) continue;

                    boolean isInterior = (lx == 0 && lz == 1 && ly >= 1 && ly <= 3);
                    if (!isInterior) {
                        positions.add(worldPos);
                    }
                }
            }
        }

        return positions;
    }
}
