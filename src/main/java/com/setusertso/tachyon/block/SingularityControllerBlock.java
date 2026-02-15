package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SingularityControllerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public SingularityControllerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SingularityControllerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.SINGULARITY_CONTROLLER.get()) return null;
        if (level.isClientSide()) {
            return (lvl, pos, st, be) -> SingularityControllerBlockEntity.clientTick(lvl, pos, st, (SingularityControllerBlockEntity) be);
        }
        return (lvl, pos, st, be) -> SingularityControllerBlockEntity.serverTick(lvl, pos, st, (SingularityControllerBlockEntity) be);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (level.getBlockEntity(pos) instanceof SingularityControllerBlockEntity be) {
                if (!state.getValue(FORMED)) {
                    be.tryFormStructure();
                }
                if (be.isFormed()) {
                    sp.openMenu(be, pos);
                } else {
                    sp.sendSystemMessage(Component.translatable("message.tachyon.singularity_not_formed"));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide() && state.getValue(FORMED)) {
            if (level.getBlockEntity(pos) instanceof SingularityControllerBlockEntity be) {
                be.onNeighborChanged(fromPos);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SingularityControllerBlockEntity be) {
                be.disassembleStructure();
                be.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
