package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CreativePowerSinkBlockEntity extends BlockEntity {

    private static final int[] RATE_OPTIONS = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};
    private int consumeRate = 100_000;
    private int rateIndex = 2; // index into RATE_OPTIONS (default 100k)

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return Math.min(maxReceive, consumeRate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return consumeRate;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public CreativePowerSinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_POWER_SINK.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getConsumeRate() {
        return consumeRate;
    }

    public void cycleRate() {
        rateIndex = (rateIndex + 1) % RATE_OPTIONS.length;
        consumeRate = RATE_OPTIONS[rateIndex];
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativePowerSinkBlockEntity be) {
        int remaining = be.consumeRate;
        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighbor = pos.relative(dir);
            IEnergyStorage source = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighbor, dir.getOpposite());
            if (source != null && source.canExtract()) {
                int extracted = source.extractEnergy(remaining, false);
                remaining -= extracted;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("RateIndex", rateIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rateIndex = tag.contains("RateIndex") ? Math.max(0, Math.min(RATE_OPTIONS.length - 1, tag.getInt("RateIndex"))) : 2;
        consumeRate = RATE_OPTIONS[rateIndex];
    }
}
