package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.VoidFrameBlockEntity;
import com.setusertso.tachyon.block.entity.VoidMinerControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class VoidFrameBlock extends Block implements EntityBlock {
    private final int tier;

    public VoidFrameBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidFrameBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VoidFrameBlockEntity frame) {
                BlockPos masterPos = frame.getMasterPos();
                if (masterPos != null) {
                    BlockEntity masterBE = level.getBlockEntity(masterPos);
                    if (masterBE instanceof VoidMinerControllerBlockEntity controller) {
                        sp.openMenu(controller, masterPos);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VoidFrameBlockEntity frame) {
                BlockPos masterPos = frame.getMasterPos();
                if (masterPos != null) {
                    BlockEntity masterBE = level.getBlockEntity(masterPos);
                    if (masterBE instanceof VoidMinerControllerBlockEntity controller) {
                        controller.disassembleStructure();
                    }
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VoidFrameBlockEntity frame) {
                BlockPos masterPos = frame.getMasterPos();
                if (masterPos != null) {
                    BlockEntity masterBE = level.getBlockEntity(masterPos);
                    if (masterBE instanceof VoidMinerControllerBlockEntity controller) {
                        controller.onNeighborChanged(pos);
                    }
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }
}
