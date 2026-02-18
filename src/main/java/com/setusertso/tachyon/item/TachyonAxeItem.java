package com.setusertso.tachyon.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TachyonAxeItem extends AxeItem {

    private static final int MAX_LOGS = 256;

    public TachyonAxeItem(Properties properties) {
        super(ModToolTier.TACHYON_ALLOY, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);

        if (!level.isClientSide && state.is(BlockTags.LOGS) && level instanceof ServerLevel serverLevel) {
            // Check if there are leaves nearby above — confirms it's a tree, not a building
            if (hasLeavesAbove(level, pos)) {
                Set<BlockPos> toBreak = findConnectedLogs(level, pos);
                for (BlockPos logPos : toBreak) {
                    BlockState logState = level.getBlockState(logPos);
                    if (logState.is(BlockTags.LOGS)) {
                        // Drop the block
                        Block.dropResources(logState, level, logPos, null, miner, stack);
                        level.removeBlock(logPos, false);
                        stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
                        if (stack.isEmpty()) break;
                    }
                }
            }
        }

        return result;
    }

    private boolean hasLeavesAbove(Level level, BlockPos origin) {
        // Search for leaf blocks in a 5-block radius above the origin, up to 20 blocks high
        for (int dy = 1; dy <= 20; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos checkPos = origin.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<BlockPos> findConnectedLogs(Level level, BlockPos origin) {
        Set<BlockPos> found = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        // Start searching above the broken block
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos above = origin.offset(dx, 1, dz);
                if (level.getBlockState(above).is(BlockTags.LOGS)) {
                    queue.add(above);
                    found.add(above);
                }
            }
        }

        while (!queue.isEmpty() && found.size() < MAX_LOGS) {
            BlockPos current = queue.poll();
            // Check all adjacent and diagonal positions on the same Y or above
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (!found.contains(neighbor) && level.getBlockState(neighbor).is(BlockTags.LOGS)
                                && neighbor.getY() > origin.getY()) {
                            found.add(neighbor);
                            queue.add(neighbor);
                            if (found.size() >= MAX_LOGS) return found;
                        }
                    }
                }
            }
        }

        return found;
    }
}
