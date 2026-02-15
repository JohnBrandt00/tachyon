package com.setusertso.tachyon.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.ExoticMatterCoreBlock;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.block.SingularityCasingBlock;
import com.setusertso.tachyon.block.SingularityControllerBlock;
import com.setusertso.tachyon.block.SingularityPattern;
import com.setusertso.tachyon.block.SingularityPortBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModSounds;
import com.setusertso.tachyon.menu.SingularityControllerMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SingularityControllerBlockEntity extends BlockEntity implements MenuProvider {

    // Slots
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    // Processing constants
    public static final int ENERGY_CAPACITY = 5_000_000;
    public static final int MAX_ENERGY_RECEIVE = 50_000;
    public static final int ENERGY_PER_TICK = 2_000;
    public static final int MAX_LIGHT_BUFFER = 64;
    public static final int LIGHT_CONSUME_INTERVAL = 100;
    public static final int PROCESS_TIME = 400;

    // Stability constants
    public static final double BASE_DECAY = 0.01;
    public static final double INJECTOR_GAIN = 0.02;
    public static final double MIN_STABILITY_TO_OPERATE = 25.0;
    public static final double MAX_STABILITY = 100.0;
    public static final double STARTING_STABILITY = 50.0;
    public static final double RESTABILIZE_THRESHOLD = 10.0;

    // Normal gravity constants
    private static final double GRAVITY_RADIUS = 16.0;
    private static final double PULL_STRENGTH = 0.06;
    private static final double MAX_PULL = 0.6;
    private static final double KILL_RADIUS = 1.5;
    private static final double DAMAGE_RADIUS = 4.5;
    private static final float DAMAGE_PER_TICK = 2.0f;
    private static final float KILL_DAMAGE = 20.0f;

    // Meltdown gravity constants (4x normal)
    private static final double MELTDOWN_GRAVITY_RADIUS = 64.0;
    private static final double MELTDOWN_PULL_STRENGTH = 0.24;
    private static final double MELTDOWN_MAX_PULL = 2.4;
    private static final double MELTDOWN_KILL_RADIUS = 3.0;
    private static final double MELTDOWN_DAMAGE_RADIUS = 9.0;
    private static final double MELTDOWN_WITHER_RADIUS = 48.0;
    private static final int WITHER_DURATION = 200;
    private static final int WITHER_AMPLIFIER = 1;

    // Storage
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final CustomEnergyStorage energy = new CustomEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, 0);

    // Processing state
    private int progress = 0;
    private int condensedLightBuffer = 0;
    private double stability = 0.0;
    private double savedStability = STARTING_STABILITY;
    private int activeInjectors = 0;
    private int coreCount = 0;
    private boolean formed = false;
    private boolean processing = false;
    private List<BlockPos> structurePositions = new ArrayList<>();
    private List<BlockPos> corePositions = new ArrayList<>();
    private List<BlockPos> portPositions = new ArrayList<>();
    private List<PortMode> portModes = new ArrayList<>();
    private BlockPos centerPos = null;

    // Engine state machine
    private EngineState engineState = EngineState.NORMAL;
    private float blackHoleScale = 1.0f;

    // Tick counters
    private int injectorScanTick = 0;
    private int damageTick = 0;
    private int witherTick = 0;

    // Client-side sound fields
    private int wardenSoundCooldown = 0;
    private SimpleSoundInstance customSound = null;
    private boolean soundStarted = false;

    // Container data for syncing to client GUI
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> PROCESS_TIME;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> condensedLightBuffer;
                case 5 -> MAX_LIGHT_BUFFER;
                case 6 -> (int) (stability * 100);
                case 7 -> (int) (MAX_STABILITY * 100);
                case 8 -> activeInjectors;
                case 9 -> coreCount;
                case 10 -> engineState.ordinal();
                case 11 -> (int) (blackHoleScale * 100);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> energy.setEnergy(value);
                case 4 -> condensedLightBuffer = value;
                case 6 -> stability = value / 100.0;
                case 10 -> engineState = EngineState.fromOrdinal(value);
                case 11 -> blackHoleScale = value / 100.0f;
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    public SingularityControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_CONTROLLER.get(), pos, state);
    }

    // --- Accessors ---

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public boolean isFormed() {
        return formed;
    }

    public boolean isProcessing() {
        return processing;
    }

    public double getStability() {
        return stability;
    }

    public int getActiveInjectors() {
        return activeInjectors;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public BlockPos getCenterPos() {
        return centerPos;
    }

    public List<BlockPos> getPortPositions() {
        return portPositions;
    }

    public List<PortMode> getPortModes() {
        return portModes;
    }

    public EngineState getEngineState() {
        return engineState;
    }

    public float getBlackHoleScale() {
        return blackHoleScale;
    }

    // --- Structure Management ---

    public void tryFormStructure() {
        if (level == null || level.isClientSide()) return;

        SingularityPattern.ensureLoaded();
        SingularityPattern.ValidationResult result = SingularityPattern.validate(level, worldPosition);

        if (result.valid()) {
            formed = true;
            engineState = EngineState.NORMAL;
            blackHoleScale = 1.0f;
            structurePositions = result.structurePositions();
            corePositions = result.corePositions();
            coreCount = corePositions.size();
            centerPos = SingularityPattern.getCenterPosition(worldPosition);
            stability = savedStability;
            portPositions.clear();
            portModes.clear();

            // Set master pos on all slave block entities and toggle FORMED states
            for (BlockPos sPos : structurePositions) {
                if (sPos.equals(worldPosition)) continue;
                BlockEntity be = level.getBlockEntity(sPos);
                BlockState sState = level.getBlockState(sPos);
                if (be instanceof SingularityCasingBlockEntity casing) {
                    casing.setMasterPos(worldPosition);
                    level.setBlock(sPos, sState.setValue(SingularityCasingBlock.FORMED, true)
                            .setValue(SingularityCasingBlock.MELTDOWN, false), Block.UPDATE_ALL);
                } else if (be instanceof SingularityPortBlockEntity port) {
                    port.setMasterPos(worldPosition);
                    portPositions.add(sPos);
                    portModes.add(port.getMode());
                    level.setBlock(sPos, sState.setValue(SingularityPortBlock.FORMED, true), Block.UPDATE_ALL);
                } else if (be instanceof ExoticMatterCoreBlockEntity core) {
                    core.setMasterPos(worldPosition);
                    level.setBlock(sPos, sState.setValue(ExoticMatterCoreBlock.FORMED, true)
                            .setValue(ExoticMatterCoreBlock.MELTDOWN, false), Block.UPDATE_ALL);
                }
            }

            // Update controller blockstate
            level.setBlock(worldPosition, getBlockState()
                    .setValue(SingularityControllerBlock.FORMED, true)
                    .setValue(SingularityControllerBlock.MELTDOWN, false),
                    Block.UPDATE_ALL);
            setChanged();
            syncToClient();
        }
    }

    /**
     * Full disassembly — only allowed in NEUTRALIZED state.
     * In NORMAL or MELTDOWN states, the structure is permanent.
     */
    public void disassembleStructure() {
        if (level == null || level.isClientSide()) return;
        if (!formed) return;

        // Structure is permanent in NORMAL and MELTDOWN states
        if (engineState == EngineState.NORMAL || engineState == EngineState.MELTDOWN) return;

        // Only allow disassembly in NEUTRALIZED state
        for (BlockPos sPos : structurePositions) {
            if (sPos.equals(worldPosition)) continue;
            BlockEntity be = level.getBlockEntity(sPos);
            BlockState sState = level.getBlockState(sPos);
            if (be instanceof SingularityCasingBlockEntity casing) {
                casing.setMasterPos(null);
                if (sState.hasProperty(SingularityCasingBlock.FORMED)) {
                    level.setBlock(sPos, sState.setValue(SingularityCasingBlock.FORMED, false)
                            .setValue(SingularityCasingBlock.MELTDOWN, false), Block.UPDATE_ALL);
                }
            } else if (be instanceof SingularityPortBlockEntity port) {
                port.setMasterPos(null);
                if (sState.hasProperty(SingularityPortBlock.FORMED)) {
                    level.setBlock(sPos, sState.setValue(SingularityPortBlock.FORMED, false), Block.UPDATE_ALL);
                }
            } else if (be instanceof ExoticMatterCoreBlockEntity core) {
                core.setMasterPos(null);
                if (sState.hasProperty(ExoticMatterCoreBlock.FORMED)) {
                    level.setBlock(sPos, sState.setValue(ExoticMatterCoreBlock.FORMED, false)
                            .setValue(ExoticMatterCoreBlock.MELTDOWN, false), Block.UPDATE_ALL);
                }
            }
        }

        if (stability > 0) {
            savedStability = stability;
        }

        formed = false;
        structurePositions.clear();
        corePositions.clear();
        portPositions.clear();
        portModes.clear();
        coreCount = 0;
        centerPos = null;
        progress = 0;
        stability = 0.0;
        activeInjectors = 0;
        processing = false;

        BlockState state = getBlockState();
        if (state.getValue(SingularityControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(SingularityControllerBlock.FORMED, false)
                    .setValue(SingularityControllerBlock.MELTDOWN, false),
                    Block.UPDATE_ALL);
        }
        setChanged();
        syncToClient();
    }

    /**
     * Enter meltdown state — stability hit 0.
     * Black hole doubles in size, gravity quadruples, wither effect applied.
     */
    public void enterMeltdown() {
        if (level == null || level.isClientSide()) return;

        engineState = EngineState.MELTDOWN;
        blackHoleScale = 2.0f;
        processing = false;
        progress = 0;

        // Set MELTDOWN=true on all structure blocks so they reappear visually
        setStructureBlocksMeltdown(true);

        // Play warden sonic boom sound
        if (centerPos != null) {
            level.playSound(null, centerPos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 3.0f, 0.5f);
        }

        setChanged();
        syncToClient();
    }

    /**
     * Neutralize the engine — antimatter neutralizer consumed.
     * Black hole disappears, all blocks become breakable.
     */
    public void neutralize() {
        if (level == null || level.isClientSide()) return;

        engineState = EngineState.NEUTRALIZED;
        blackHoleScale = 0.0f;
        processing = false;
        progress = 0;
        stability = 0.0;
        savedStability = STARTING_STABILITY;

        // Clear master pos on all slaves and reset FORMED
        for (BlockPos sPos : structurePositions) {
            if (sPos.equals(worldPosition)) continue;
            BlockEntity be = level.getBlockEntity(sPos);
            BlockState sState = level.getBlockState(sPos);
            if (be instanceof SingularityCasingBlockEntity casing) {
                casing.setMasterPos(null);
                if (sState.hasProperty(SingularityCasingBlock.FORMED)) {
                    level.setBlock(sPos, sState.setValue(SingularityCasingBlock.FORMED, false)
                            .setValue(SingularityCasingBlock.MELTDOWN, false), Block.UPDATE_ALL);
                }
            } else if (be instanceof SingularityPortBlockEntity port) {
                port.setMasterPos(null);
                if (sState.hasProperty(SingularityPortBlock.FORMED)) {
                    level.setBlock(sPos, sState.setValue(SingularityPortBlock.FORMED, false), Block.UPDATE_ALL);
                }
            } else if (be instanceof ExoticMatterCoreBlockEntity core) {
                core.setMasterPos(null);
                if (sState.hasProperty(ExoticMatterCoreBlock.FORMED)) {
                    level.setBlock(sPos, sState.setValue(ExoticMatterCoreBlock.FORMED, false)
                            .setValue(ExoticMatterCoreBlock.MELTDOWN, false), Block.UPDATE_ALL);
                }
            }
        }

        formed = false;
        structurePositions.clear();
        corePositions.clear();
        portPositions.clear();
        portModes.clear();
        coreCount = 0;
        centerPos = null;

        // Update controller blockstate
        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(SingularityControllerBlock.FORMED, false)
                .setValue(SingularityControllerBlock.MELTDOWN, false), Block.UPDATE_ALL);

        setChanged();
        syncToClient();
    }

    /**
     * Reform the structure after neutralization.
     */
    public void reformStructure() {
        if (level == null || level.isClientSide()) return;
        if (engineState != EngineState.NEUTRALIZED) return;

        engineState = EngineState.NORMAL;
        blackHoleScale = 1.0f;
        savedStability = STARTING_STABILITY;
        tryFormStructure();
    }

    private void setStructureBlocksMeltdown(boolean meltdown) {
        if (level == null) return;

        for (BlockPos sPos : structurePositions) {
            BlockState sState = level.getBlockState(sPos);
            if (sState.hasProperty(SingularityCasingBlock.MELTDOWN)) {
                level.setBlock(sPos, sState.setValue(SingularityCasingBlock.MELTDOWN, meltdown), Block.UPDATE_ALL);
            } else if (sState.hasProperty(ExoticMatterCoreBlock.MELTDOWN)) {
                level.setBlock(sPos, sState.setValue(ExoticMatterCoreBlock.MELTDOWN, meltdown), Block.UPDATE_ALL);
            } else if (sState.hasProperty(SingularityControllerBlock.MELTDOWN)) {
                level.setBlock(sPos, sState.setValue(SingularityControllerBlock.MELTDOWN, meltdown), Block.UPDATE_ALL);
            }
        }
        // Also update controller itself
        BlockState controllerState = level.getBlockState(worldPosition);
        if (controllerState.hasProperty(SingularityControllerBlock.MELTDOWN)) {
            level.setBlock(worldPosition, controllerState.setValue(SingularityControllerBlock.MELTDOWN, meltdown), Block.UPDATE_ALL);
        }
    }

    public void updatePortMode(BlockPos portPos, PortMode newMode) {
        for (int i = 0; i < portPositions.size(); i++) {
            if (portPositions.get(i).equals(portPos)) {
                portModes.set(i, newMode);
                setChanged();
                syncToClient();
                return;
            }
        }
    }

    public void onNeighborChanged(BlockPos changedPos) {
        // Structure is permanent once formed — no validation/disassembly
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
                    worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    // --- Processing ---

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                   SingularityControllerBlockEntity be) {
        if (!be.formed) return;

        // Skip everything if neutralized
        if (be.engineState == EngineState.NEUTRALIZED) return;

        boolean isMeltdown = be.engineState == EngineState.MELTDOWN;

        // --- Gravitational pull ---
        be.damageTick++;
        if (be.centerPos != null) {
            double gravRadius = isMeltdown ? MELTDOWN_GRAVITY_RADIUS : GRAVITY_RADIUS;
            double pullStr = isMeltdown ? MELTDOWN_PULL_STRENGTH : PULL_STRENGTH;
            double maxPull = isMeltdown ? MELTDOWN_MAX_PULL : MAX_PULL;
            double killRadius = isMeltdown ? MELTDOWN_KILL_RADIUS : KILL_RADIUS;
            double damageRadius = isMeltdown ? MELTDOWN_DAMAGE_RADIUS : DAMAGE_RADIUS;

            double cx = be.centerPos.getX() + 0.5;
            double cy = be.centerPos.getY() + 0.5;
            double cz = be.centerPos.getZ() + 0.5;

            AABB pullArea = new AABB(
                    cx - gravRadius, cy - gravRadius, cz - gravRadius,
                    cx + gravRadius, cy + gravRadius, cz + gravRadius);

            for (Entity entity : level.getEntitiesOfClass(Entity.class, pullArea)) {
                double dx = cx - entity.getX();
                double dy = cy - (entity.getY() + entity.getBbHeight() * 0.5);
                double dz = cz - entity.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > gravRadius || distance < 0.1) continue;
                if (entity instanceof Player player && player.isCreative()) continue;

                double nx = dx / distance;
                double ny = dy / distance;
                double nz = dz / distance;

                double strength = Math.min(maxPull, pullStr / (distance * 0.15));
                Vec3 motion = entity.getDeltaMovement();
                entity.setDeltaMovement(motion.x + nx * strength, motion.y + ny * strength, motion.z + nz * strength);
                entity.hurtMarked = true;

                if (distance < killRadius) {
                    if (entity instanceof ItemEntity) {
                        entity.discard();
                    } else {
                        entity.hurt(level.damageSources().generic(), KILL_DAMAGE);
                    }
                } else if (distance < damageRadius && be.damageTick % 10 == 0) {
                    entity.hurt(level.damageSources().generic(), DAMAGE_PER_TICK);
                }
            }

            // Wither effect during meltdown
            if (isMeltdown) {
                be.witherTick++;
                if (be.witherTick >= 40) {
                    be.witherTick = 0;
                    AABB witherArea = new AABB(
                            cx - MELTDOWN_WITHER_RADIUS, cy - MELTDOWN_WITHER_RADIUS, cz - MELTDOWN_WITHER_RADIUS,
                            cx + MELTDOWN_WITHER_RADIUS, cy + MELTDOWN_WITHER_RADIUS, cz + MELTDOWN_WITHER_RADIUS);
                    for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, witherArea)) {
                        if (living instanceof Player player && player.isCreative()) continue;
                        living.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION, WITHER_AMPLIFIER));
                    }
                }
            }
        }

        boolean changed = false;
        boolean wasProcessing = be.processing;

        // Scan for injectors every 20 ticks
        be.injectorScanTick++;
        if (be.injectorScanTick >= 20) {
            be.injectorScanTick = 0;
            int oldCount = be.activeInjectors;
            be.scanForInjectors();
            if (be.activeInjectors != oldCount) changed = true;
        }

        // Update stability
        double decay = BASE_DECAY / (1.0 + 0.5 * be.coreCount);
        be.stability -= decay;
        be.stability += INJECTOR_GAIN * be.activeInjectors;
        be.stability = Math.max(0.0, Math.min(MAX_STABILITY, be.stability));
        changed = true;

        // State transitions based on stability
        if (be.engineState == EngineState.NORMAL && be.stability <= 0.0) {
            // NORMAL -> MELTDOWN
            be.enterMeltdown();
            return;
        }

        if (be.engineState == EngineState.MELTDOWN && be.stability >= RESTABILIZE_THRESHOLD) {
            // MELTDOWN -> NORMAL: restabilized
            be.engineState = EngineState.NORMAL;
            be.setStructureBlocksMeltdown(false);
            be.setChanged();
            be.syncToClient();
        }

        // Gradually shrink black hole scale back to 1.0 when in NORMAL state and scale > 1.0
        if (be.engineState == EngineState.NORMAL && be.blackHoleScale > 1.0f) {
            float shrinkRate = 0.002f * (float)(be.stability / MAX_STABILITY);
            be.blackHoleScale = Math.max(1.0f, be.blackHoleScale - shrinkRate);
            changed = true;
        }

        // Skip processing during meltdown
        if (isMeltdown) {
            be.processing = false;
            be.progress = 0;
            if (changed || wasProcessing != be.processing) {
                be.setChanged();
                if (wasProcessing != be.processing || level.getGameTime() % 10 == 0) {
                    be.syncToClient();
                }
            }
            return;
        }

        // Process if conditions met (NORMAL state only)
        if (be.stability >= MIN_STABILITY_TO_OPERATE
                && be.energy.getEnergyStored() >= ENERGY_PER_TICK
                && be.condensedLightBuffer > 0
                && be.canOutputExoticMatter()) {

            be.energy.consumeEnergy(ENERGY_PER_TICK);
            be.progress++;
            be.processing = true;

            if (be.progress % LIGHT_CONSUME_INTERVAL == 0) {
                be.condensedLightBuffer--;
            }

            if (be.condensedLightBuffer < MAX_LIGHT_BUFFER) {
                ItemStack input = be.items.getStackInSlot(INPUT_SLOT);
                if (!input.isEmpty() && input.is(ModItems.CONDENSED_LIGHT.get())) {
                    int canTake = Math.min(input.getCount(), MAX_LIGHT_BUFFER - be.condensedLightBuffer);
                    be.condensedLightBuffer += canTake;
                    be.items.extractItem(INPUT_SLOT, canTake, false);
                }
            }

            if (be.progress >= PROCESS_TIME) {
                be.produceExoticMatter();
                be.progress = 0;
            }
        } else {
            if (be.condensedLightBuffer < MAX_LIGHT_BUFFER) {
                ItemStack input = be.items.getStackInSlot(INPUT_SLOT);
                if (!input.isEmpty() && input.is(ModItems.CONDENSED_LIGHT.get())) {
                    int canTake = Math.min(input.getCount(), MAX_LIGHT_BUFFER - be.condensedLightBuffer);
                    be.condensedLightBuffer += canTake;
                    be.items.extractItem(INPUT_SLOT, canTake, false);
                }
            }

            if (be.progress > 0) {
                be.progress = 0;
            }
            be.processing = false;
        }

        if (changed || wasProcessing != be.processing) {
            be.setChanged();
            if (wasProcessing != be.processing || level.getGameTime() % 10 == 0) {
                be.syncToClient();
            }
        }
    }

    // --- Client Tick (sound) ---

    public static void clientTick(Level level, BlockPos pos, BlockState state,
                                   SingularityControllerBlockEntity be) {
        if (!be.formed) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Play warden ambient sound every 60 ticks
        be.wardenSoundCooldown--;
        if (be.wardenSoundCooldown <= 0) {
            level.playLocalSound(x, y, z,
                    SoundEvents.WARDEN_AMBIENT, SoundSource.BLOCKS,
                    1.5f, 0.5f, false);
            be.wardenSoundCooldown = 60;
        }

        // Start looping custom sound once
        if (!be.soundStarted) {
            be.customSound = new SimpleSoundInstance(
                    ModSounds.BLACK_HOLE_AMBIENT.get().getLocation(),
                    SoundSource.BLOCKS,
                    2.0f, 1.0f,
                    SoundInstance.createUnseededRandom(),
                    true, 0,
                    SoundInstance.Attenuation.LINEAR,
                    x, y, z, false);
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

    private boolean canOutputExoticMatter() {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!output.is(ModItems.EXOTIC_MATTER.get())) return false;
        return output.getCount() < output.getMaxStackSize();
    }

    private void produceExoticMatter() {
        ItemStack existing = items.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.EXOTIC_MATTER.get()));
        } else {
            existing.grow(1);
        }
    }

    private void scanForInjectors() {
        if (centerPos == null || level == null) return;

        SingularityPattern.ensureLoaded();
        int shellRadius = SingularityPattern.getHeight() / 2;

        int count = 0;
        for (Direction dir : Direction.values()) {
            for (int dist = shellRadius; dist <= shellRadius + 20; dist++) {
                BlockPos checkPos = centerPos.relative(dir, dist);
                BlockState checkState = level.getBlockState(checkPos);

                if (checkState.getBlock() instanceof PhotonicInjectorBlock) {
                    if (checkState.getValue(PhotonicInjectorBlock.ACTIVE)) {
                        Direction injectorFacing = checkState.getValue(PhotonicInjectorBlock.FACING);
                        if (injectorFacing == dir.getOpposite()) {
                            count++;
                        }
                    }
                    break;
                }

                if (!checkState.isAir() && !structurePositions.contains(checkPos)) {
                    break;
                }
            }
        }
        activeInjectors = count;
    }

    // --- Menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.singularity_controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SingularityControllerMenu(containerId, playerInventory, items, dataAccess, worldPosition);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putInt("LightBuffer", condensedLightBuffer);
        tag.putDouble("Stability", stability);
        tag.putDouble("SavedStability", savedStability);
        tag.putInt("Injectors", activeInjectors);
        tag.putInt("CoreCount", coreCount);
        tag.putBoolean("Formed", formed);
        tag.putBoolean("Processing", processing);
        tag.putInt("EngineState", engineState.ordinal());
        tag.putFloat("BlackHoleScale", blackHoleScale);

        if (centerPos != null) {
            tag.putInt("CenterX", centerPos.getX());
            tag.putInt("CenterY", centerPos.getY());
            tag.putInt("CenterZ", centerPos.getZ());
        }

        if (formed && !structurePositions.isEmpty()) {
            ListTag posList = new ListTag();
            for (BlockPos sPos : structurePositions) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", sPos.getX());
                posTag.putInt("Y", sPos.getY());
                posTag.putInt("Z", sPos.getZ());
                posList.add(posTag);
            }
            tag.put("StructurePositions", posList);

            ListTag coreList = new ListTag();
            for (BlockPos cPos : corePositions) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", cPos.getX());
                posTag.putInt("Y", cPos.getY());
                posTag.putInt("Z", cPos.getZ());
                coreList.add(posTag);
            }
            tag.put("CorePositions", coreList);

            ListTag portList = new ListTag();
            for (int i = 0; i < portPositions.size(); i++) {
                CompoundTag portTag = new CompoundTag();
                portTag.putInt("X", portPositions.get(i).getX());
                portTag.putInt("Y", portPositions.get(i).getY());
                portTag.putInt("Z", portPositions.get(i).getZ());
                portTag.putInt("Mode", i < portModes.size() ? portModes.get(i).ordinal() : 0);
                portList.add(portTag);
            }
            tag.put("PortPositions", portList);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        condensedLightBuffer = tag.getInt("LightBuffer");
        stability = tag.getDouble("Stability");
        savedStability = tag.contains("SavedStability") ? tag.getDouble("SavedStability") : STARTING_STABILITY;
        activeInjectors = tag.getInt("Injectors");
        coreCount = tag.getInt("CoreCount");
        formed = tag.getBoolean("Formed");
        processing = tag.getBoolean("Processing");
        engineState = tag.contains("EngineState") ? EngineState.fromOrdinal(tag.getInt("EngineState")) : EngineState.NORMAL;
        blackHoleScale = tag.contains("BlackHoleScale") ? tag.getFloat("BlackHoleScale") : 1.0f;

        if (tag.contains("CenterX")) {
            centerPos = new BlockPos(tag.getInt("CenterX"), tag.getInt("CenterY"), tag.getInt("CenterZ"));
        } else {
            centerPos = null;
        }

        structurePositions.clear();
        if (tag.contains("StructurePositions")) {
            ListTag posList = tag.getList("StructurePositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < posList.size(); i++) {
                CompoundTag posTag = posList.getCompound(i);
                structurePositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        corePositions.clear();
        if (tag.contains("CorePositions")) {
            ListTag coreList = tag.getList("CorePositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < coreList.size(); i++) {
                CompoundTag posTag = coreList.getCompound(i);
                corePositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        portPositions.clear();
        portModes.clear();
        if (tag.contains("PortPositions")) {
            ListTag portList = tag.getList("PortPositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < portList.size(); i++) {
                CompoundTag portTag = portList.getCompound(i);
                portPositions.add(new BlockPos(portTag.getInt("X"), portTag.getInt("Y"), portTag.getInt("Z")));
                portModes.add(PortMode.fromOrdinal(portTag.getInt("Mode")));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        handleUpdateTag(pkt.getTag(), registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
