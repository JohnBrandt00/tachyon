package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.block.TachyonLightGeneratorBlock;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TachyonLightGeneratorBlockEntity extends BlockEntity {
    private long lastPoweredTick = -100;
    private int checkCounter = 0;

    public TachyonLightGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TACHYON_LIGHT_GENERATOR.get(), pos, state);
    }

    public void setLastPoweredTick(long gameTime) {
        this.lastPoweredTick = gameTime;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TachyonLightGeneratorBlockEntity be) {
        if (!state.getValue(TachyonLightGeneratorBlock.ACTIVE)) {
            return;
        }

        be.checkCounter++;
        if (be.checkCounter >= 20) {
            be.checkCounter = 0;
            if (level.getGameTime() - be.lastPoweredTick > 40) {
                level.setBlock(pos, state.setValue(TachyonLightGeneratorBlock.ACTIVE, false), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("LastPoweredTick", lastPoweredTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastPoweredTick = tag.getLong("LastPoweredTick");
    }
}
