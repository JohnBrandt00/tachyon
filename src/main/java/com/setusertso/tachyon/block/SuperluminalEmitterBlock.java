package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.SuperluminalEmitterBlockEntity;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SuperluminalEmitterBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SuperluminalEmitterBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperluminalEmitterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ModBlockEntities.SUPERLUMINAL_EMITTER.get()
                ? (lvl, pos, st, be) -> SuperluminalEmitterBlockEntity.serverTick(lvl, pos, st, (SuperluminalEmitterBlockEntity) be)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof SuperluminalEmitterBlockEntity be) {
                serverPlayer.openMenu(be, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (level.getBlockEntity(pos) instanceof SuperluminalEmitterBlockEntity be) {
                be.setRedstonePowered(powered);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SuperluminalEmitterBlockEntity be) {
                be.deactivateAllGenerators();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
