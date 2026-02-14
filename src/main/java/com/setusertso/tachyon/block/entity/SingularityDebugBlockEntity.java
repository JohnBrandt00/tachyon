package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SingularityDebugBlockEntity extends BlockEntity {

    public SingularityDebugBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_DEBUG.get(), pos, state);
    }
}
