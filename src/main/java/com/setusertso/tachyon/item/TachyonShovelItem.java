package com.setusertso.tachyon.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class TachyonShovelItem extends ShovelItem {

    public TachyonShovelItem(Properties properties) {
        super(ModToolTier.TACHYON_ALLOY, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);

        if (!level.isClientSide && miner instanceof ServerPlayer player && !player.isShiftKeyDown()) {
            // Get the face the player is looking at
            HitResult hitResult = player.pick(5.0, 0.0f, false);
            if (hitResult instanceof BlockHitResult blockHit) {
                Direction face = blockHit.getDirection();
                BlockPos[] extraPositions = getExtraPositions(pos, face, player);

                for (BlockPos extraPos : extraPositions) {
                    BlockState extraState = level.getBlockState(extraPos);
                    if (isShovelMineable(extraState) && !extraState.isAir()) {
                        Block.dropResources(extraState, level, extraPos, null, player, stack);
                        level.removeBlock(extraPos, false);
                        stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
                        if (stack.isEmpty()) break;
                    }
                }
            }
        }

        return result;
    }

    private boolean isShovelMineable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    private BlockPos[] getExtraPositions(BlockPos center, Direction face, ServerPlayer player) {
        // Determine the perpendicular axis based on the face we're looking at
        if (face == Direction.UP || face == Direction.DOWN) {
            // Looking at ground/ceiling — break a row perpendicular to player facing
            Direction playerFacing = player.getDirection();
            if (playerFacing == Direction.NORTH || playerFacing == Direction.SOUTH) {
                // Player faces N/S, break in X axis
                return new BlockPos[]{center.offset(-1, 0, 0), center.offset(1, 0, 0)};
            } else {
                // Player faces E/W, break in Z axis
                return new BlockPos[]{center.offset(0, 0, -1), center.offset(0, 0, 1)};
            }
        } else if (face == Direction.NORTH || face == Direction.SOUTH) {
            // Looking at a N/S wall — break horizontally (X axis)
            return new BlockPos[]{center.offset(-1, 0, 0), center.offset(1, 0, 0)};
        } else {
            // Looking at an E/W wall — break horizontally (Z axis)
            return new BlockPos[]{center.offset(0, 0, -1), center.offset(0, 0, 1)};
        }
    }
}
