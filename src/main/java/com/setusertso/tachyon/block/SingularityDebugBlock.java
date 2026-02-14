package com.setusertso.tachyon.block;

import org.jetbrains.annotations.Nullable;

import com.setusertso.tachyon.block.entity.SingularityDebugBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SingularityDebugBlock extends Block implements EntityBlock {

    public SingularityDebugBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SingularityDebugBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof SingularityDebugBlockEntity singularity) {
                SingularityDebugBlockEntity.tick(lvl, pos, st, singularity);
            }
        };
    }
}
