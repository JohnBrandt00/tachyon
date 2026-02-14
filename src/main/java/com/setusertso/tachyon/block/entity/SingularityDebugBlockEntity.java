package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SingularityDebugBlockEntity extends BlockEntity {

    private int wardenSoundCooldown = 0;
    private SimpleSoundInstance customSound = null;
    private boolean soundStarted = false;

    public SingularityDebugBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_DEBUG.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SingularityDebugBlockEntity blockEntity) {
        if (!level.isClientSide) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Play warden ambient sound every 60 ticks (3 seconds)
        blockEntity.wardenSoundCooldown--;
        if (blockEntity.wardenSoundCooldown <= 0) {
            level.playLocalSound(
                x, y, z,
                SoundEvents.WARDEN_AMBIENT,
                SoundSource.BLOCKS,
                1.5f,  // volume
                0.5f,  // pitch (lower = deeper)
                false
            );
            blockEntity.wardenSoundCooldown = 60;
        }

        // Start looping custom sound once
        if (!blockEntity.soundStarted) {
            blockEntity.customSound = new SimpleSoundInstance(
                ModSounds.BLACK_HOLE_AMBIENT.get().getLocation(),
                SoundSource.BLOCKS,
                2.0f,  // volume
                1.0f,  // pitch
                SoundInstance.createUnseededRandom(),
                true,  // looping
                0,     // delay
                SoundInstance.Attenuation.LINEAR,
                x, y, z,
                false  // relative
            );
            Minecraft.getInstance().getSoundManager().play(blockEntity.customSound);
            blockEntity.soundStarted = true;
        }
    }

    public void stopSounds() {
        if (customSound != null && level != null && level.isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(customSound);
            customSound = null;
            soundStarted = false;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        stopSounds();
    }
}
