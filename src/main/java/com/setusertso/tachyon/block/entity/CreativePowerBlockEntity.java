package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativePowerBlockEntity extends BlockEntity {

    private static final int OUTPUT_PER_TICK = 100_000;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return Math.min(maxExtract, OUTPUT_PER_TICK);
        }

        @Override
        public int getEnergyStored() {
            return OUTPUT_PER_TICK;
        }

        @Override
        public int getMaxEnergyStored() {
            return OUTPUT_PER_TICK;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public CreativePowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_POWER_SOURCE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativePowerBlockEntity be) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighbor, dir.getOpposite());
            if (target != null && target.canReceive()) {
                target.receiveEnergy(OUTPUT_PER_TICK, false);
            }
        }
    }
}
