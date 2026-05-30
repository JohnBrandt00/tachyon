package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.CondenserCasingBlockEntity;
import com.setusertso.tachyon.block.entity.CondenserControllerBlockEntity;

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

public class CondenserCasingBlock extends Block implements EntityBlock {

    public CondenserCasingBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CondenserCasingBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CondenserCasingBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    if (level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
                        sp.openMenu(controller, masterPos);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof CondenserCasingBlockEntity be) {
                BlockPos masterPos = be.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
                    controller.onNeighborChanged(pos);
                }
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CondenserCasingBlockEntity be) {
                BlockPos masterPos = be.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
                    controller.disassembleStructure();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
