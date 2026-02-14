package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.SingularityDebugBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SingularityDebugBlock extends Block implements EntityBlock {

    public SingularityDebugBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SingularityDebugBlockEntity(pos, state);
    }
}
