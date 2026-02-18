package com.setusertso.tachyon.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class TachyonWrenchItem extends Item {

    public TachyonWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            // Shift + right-click: dismantle
            // Don't dismantle multiblock controllers (safety)
            String blockName = state.getBlock().getClass().getSimpleName();
            if (blockName.contains("Controller") || blockName.contains("Accelerator")) {
                return InteractionResult.FAIL;
            }

            // Break the block and drop it
            Block.dropResources(state, level, pos, level.getBlockEntity(pos));
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // Normal right-click: rotate
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
            Direction current = state.getValue(facingProp);
            Direction rotated = current.getClockWise();
            level.setBlock(pos, state.setValue(facingProp, rotated), 3);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.0f);
            return InteractionResult.SUCCESS;
        } else if (state.hasProperty(DirectionalBlock.FACING)) {
            DirectionProperty facingProp = DirectionalBlock.FACING;
            Direction current = state.getValue(facingProp);
            // Cycle through all 6 directions
            Direction[] order = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
            int idx = 0;
            for (int i = 0; i < order.length; i++) {
                if (order[i] == current) { idx = i; break; }
            }
            Direction next = order[(idx + 1) % order.length];
            level.setBlock(pos, state.setValue(facingProp, next), 3);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.0f);
            return InteractionResult.SUCCESS;
        } else {
            // Try generic rotation
            BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);
            if (rotated != state) {
                level.setBlock(pos, rotated, 3);
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
