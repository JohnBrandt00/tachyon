package com.setusertso.tachyon.block.entity;

import java.util.List;

import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SingularityDebugBlockEntity extends BlockEntity {

    // Gravity constants
    private static final double BASE_SCALE = 3.0;
    private static final double PULL_STRENGTH = 0.08;
    private static final double MAX_PULL = 0.8;
    private static final float DAMAGE_PER_TICK = 2.0f;
    private static final float KILL_DAMAGE = 20.0f;
    private static final double GROWTH_PER_ITEM = 0.02;
    private static final double GROWTH_PER_MOB = 0.1;
    private static final double MAX_SCALE = 10.0;

    // Dynamic scale — grows as things fall in
    private double scale = BASE_SCALE;

    // Client-side sound fields
    private int wardenSoundCooldown = 0;
    private SimpleSoundInstance customSound = null;
    private boolean soundStarted = false;

    // Server-side tick counter for periodic damage
    private int damageTick = 0;

    public SingularityDebugBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_DEBUG.get(), pos, state);
    }

    public double getScale() {
        return scale;
    }

    /** Get the world-space center of the black hole */
    public Vec3 getCenter() {
        return new Vec3(
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 1.5 * scale,
            worldPosition.getZ() + 0.5
        );
    }

    private double getGravityRadius() {
        return 5.0 * scale; // extends slightly beyond accretion disk
    }

    private double getKillRadius() {
        return 0.5 * scale; // deep inside the event horizon
    }

    private double getDamageRadius() {
        return 1.5 * scale; // near the event horizon sphere
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SingularityDebugBlockEntity be) {
        if (level.isClientSide) {
            clientTick(level, pos, be);
        } else {
            serverTick(level, pos, be);
        }
    }

    // === SERVER-SIDE: Gravity pull, damage, and growth ===
    private static void serverTick(Level level, BlockPos pos, SingularityDebugBlockEntity be) {
        be.damageTick++;

        Vec3 center = be.getCenter();
        double gravRadius = be.getGravityRadius();
        double killRadius = be.getKillRadius();
        double damageRadius = be.getDamageRadius();

        AABB aabb = new AABB(
            center.x - gravRadius, center.y - gravRadius, center.z - gravRadius,
            center.x + gravRadius, center.y + gravRadius, center.z + gravRadius
        );

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, aabb);
        boolean grew = false;

        for (Entity entity : entities) {
            double dx = center.x - entity.getX();
            double dy = center.y - (entity.getY() + entity.getBbHeight() * 0.5);
            double dz = center.z - entity.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > gravRadius || distance < 0.1) continue;

            // Creative mode players are immune to gravity and damage
            if (entity instanceof Player player && player.isCreative()) continue;

            // Normalize direction
            double nx = dx / distance;
            double ny = dy / distance;
            double nz = dz / distance;

            // Pull strength: inverse-distance (stronger when closer), capped
            double strength = Math.min(MAX_PULL, PULL_STRENGTH / (distance * 0.15));

            Vec3 currentMotion = entity.getDeltaMovement();
            entity.setDeltaMovement(
                currentMotion.x + nx * strength,
                currentMotion.y + ny * strength,
                currentMotion.z + nz * strength
            );
            entity.hurtMarked = true; // force velocity sync to client

            // Kill zone — destroy items, heavy damage to everything else
            if (distance < killRadius) {
                if (entity instanceof ItemEntity) {
                    entity.discard();
                    be.grow(GROWTH_PER_ITEM);
                    grew = true;
                } else if (!(entity instanceof Player)) {
                    entity.hurt(level.damageSources().generic(), KILL_DAMAGE);
                    if (!entity.isAlive()) {
                        be.grow(GROWTH_PER_MOB);
                        grew = true;
                    }
                } else {
                    entity.hurt(level.damageSources().generic(), KILL_DAMAGE);
                }
            }
            // Damage zone — periodic damage
            else if (distance < damageRadius && be.damageTick % 10 == 0) {
                entity.hurt(level.damageSources().generic(), DAMAGE_PER_TICK);
            }
        }

        // Sync scale to client when it changes
        if (grew) {
            be.setChanged();
            level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);
        }
    }

    private void grow(double amount) {
        scale = Math.min(MAX_SCALE, scale + amount);
    }

    // === CLIENT-SIDE: Sound management ===
    private static void clientTick(Level level, BlockPos pos, SingularityDebugBlockEntity be) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Play warden ambient sound every 60 ticks (3 seconds)
        be.wardenSoundCooldown--;
        if (be.wardenSoundCooldown <= 0) {
            level.playLocalSound(
                x, y, z,
                SoundEvents.WARDEN_AMBIENT,
                SoundSource.BLOCKS,
                1.5f,
                0.5f,
                false
            );
            be.wardenSoundCooldown = 60;
        }

        // Start looping custom sound once
        if (!be.soundStarted) {
            be.customSound = new SimpleSoundInstance(
                ModSounds.BLACK_HOLE_AMBIENT.get().getLocation(),
                SoundSource.BLOCKS,
                2.0f,
                1.0f,
                SoundInstance.createUnseededRandom(),
                true,
                0,
                SoundInstance.Attenuation.LINEAR,
                x, y, z,
                false
            );
            Minecraft.getInstance().getSoundManager().play(be.customSound);
            be.soundStarted = true;
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

    // === NBT save/load for scale persistence ===
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("Scale", scale);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Scale")) {
            scale = tag.getDouble("Scale");
        }
    }

    // === Sync to client ===
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putDouble("Scale", scale);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
