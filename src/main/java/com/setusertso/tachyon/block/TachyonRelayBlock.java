package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.TachyonRelayBlockEntity;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import org.jetbrains.annotations.Nullable;

public class TachyonRelayBlock extends Block implements EntityBlock {

    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public TachyonRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LINKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LINKED);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TachyonRelayBlockEntity relay) {
                relay.dropContents(level, pos);
                // Notify linked partner to clear its link
                if (!level.isClientSide) {
                    relay.notifyPartnerOfRemoval();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TachyonRelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.TACHYON_RELAY.get()
                ? (lvl, pos, st, be) -> TachyonRelayBlockEntity.serverTick(lvl, pos, st, (TachyonRelayBlockEntity) be)
                : null;
    }
}
