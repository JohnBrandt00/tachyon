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

    // === SLOTS ===
    public static final int OUTPUT_SLOT = 0;
    public static final int SLOT_COUNT = 1;

    // === BLACK HOLE INTERNAL ENERGY ===
    public static final double MAX_ENERGY = 2000.0;
    public static final double SWEET_SPOT_ENERGY = 1000.0;        // stability target — deviation from here causes decay
    public static final double STARTING_ENERGY = 200.0;
    public static final double ENERGY_PER_PHOTON = 1.0;

    // === HAWKING RADIATION (RF Generation) ===
    // RF output scales EXPONENTIALLY with mass. More mass = dramatically more power.
    // At sweet spot (1000): ~50k RF/t. At 1500: ~500k RF/t. At 2000: ~10M RF/t.
    // This makes pushing above the sweet spot incredibly rewarding but equally dangerous.
    // Formula: RF = RF_BASE * e^(RF_EXPONENT * (mass / MAX_ENERGY))
    public static final double RF_BASE = 500.0;       // base RF at near-zero mass
    public static final double RF_EXPONENT = 10.0;     // exponential growth factor
    public static final double RADIATION_IGNITION_THRESHOLD = 10.0; // no radiation below this mass

    // === RF OUTPUT BUFFER ===
    public static final int RF_BUFFER_CAPACITY = 100_000_000;  // 100M RF buffer
    public static final int MAX_RF_EXTRACT = 50_000_000;       // 50M RF/t max extraction

    // === CONTAINMENT TEMPERATURE ===
    // Temperature is the second axis of danger. The black hole radiates heat proportional
    // to its mass. Shields cool the containment, but if heat > cooling, temp rises.
    // High temperature EXPONENTIALLY amplifies stability decay — this is the real killer.
    // Temperature has inertia — it changes slowly, punishing reactive play.
    public static final double MAX_TEMPERATURE = 10000.0;         // max containment temp (display cap)
    public static final double HEAT_GENERATION_MULT = 0.001;      // multiplier on heat generation (mass^1.5 * this)
    public static final double HEAT_EXPONENT = 1.5;               // heat scales superlinearly with mass
    public static final double COOLING_COEFFICIENT = 0.001;       // cooling per RF/t of shield, scaled by temp
    public static final double PASSIVE_COOLING_COEFF = 5.0;       // passive cooling coefficient (containment field baseline)
    public static final double REFERENCE_TEMP = 1000.0;           // reference temp for cooling scaling
    public static final double TEMP_SAFE_ZONE = 1500.0;           // temp below this causes NO stability decay
    public static final double TEMP_STABILITY_MULT = 0.00003;     // decay multiplier for temp above safe zone
    public static final double TEMP_STABILITY_EXPONENT = 1.3;     // exponential scaling of temp→decay
    public static final double CONTAINMENT_FAILURE_THRESHOLD = 7000.0; // temp above this: random failures
    public static final double CONTAINMENT_FAILURE_CHANCE = 0.02; // chance per tick of failure event at max temp
    public static final double CONTAINMENT_FAILURE_STABILITY_HIT = 5.0; // stability lost per failure event
    public static final double EXOTIC_MATTER_HEAT_SPIKE = 500.0;  // instant temp boost per exotic matter produced
    public static final double THERMAL_INERTIA = 0.015;           // temp changes by 1.5% of gap per tick (slow)

    // === STABILITY ===
    // Stability decays based on distance from SWEET_SPOT, deviation from equilibrium,
    // and TEMPERATURE. Temperature is the main danger amplifier.
    public static final double EQUILIBRIUM_RATE = 0.01;           // equilibrium closes 1% of gap/tick (slow — changes linger)
    public static final double BASE_STABILITY_DECAY = 0.005;      // baseline decay even at sweet spot
    public static final double UNDERSHOOT_DECAY_MULT = 0.15;      // decay when below sweet spot (gentle)
    public static final double OVERSHOOT_DECAY_MULT = 2.0;        // decay when above sweet spot
    public static final double DEVIATION_DECAY_MULT = 3.0;        // rate-of-change penalty (harsher)
    public static final double SHIELD_STABILITY_EFFICIENCY = 0.00003; // RF/t → stability recovery
    public static final double CORE_PASSIVE_STABILIZATION = 0.005; // passive recovery per core per tick (halved)
    public static final double SHIELD_EFFECTIVENESS_DROPOFF = 0.6; // shields lose effectiveness at high mass
    public static final double MIN_STABILITY_TO_OPERATE = 25.0;
    public static final double MAX_STABILITY = 100.0;
    public static final double STARTING_STABILITY = 50.0;
    public static final double RESTABILIZE_THRESHOLD = 10.0;

    // === EXOTIC MATTER PRODUCTION ===
    public static final double EXOTIC_MATTER_ENERGY_THRESHOLD = 0.5; // 50% of sweet spot = 500 mass minimum
    public static final double EXOTIC_MATTER_CRAFT_SPEED_MULT = 2.5;
    public static final double EXOTIC_MATTER_ENERGY_COST = 50.0;
    public static final int EXOTIC_MATTER_BASE_TIME = 200;
    public static final double EXOTIC_MATTER_STABILITY_COST = 0.01; // doubled stability cost

    // === GRAVITY (scaled by energy) ===
    public static final double BASE_GRAVITY_RADIUS = 16.0;
    public static final double BASE_PULL_STRENGTH = 0.06;
    public static final double BASE_MAX_PULL = 0.6;
    public static final double BASE_KILL_RADIUS = 1.5;
    public static final double BASE_DAMAGE_RADIUS = 4.5;
    public static final float DAMAGE_PER_TICK = 2.0f;
    public static final float KILL_DAMAGE = 20.0f;

    // === MELTDOWN ===
    public static final double MELTDOWN_MASS_DECAY = 1.0;        // mass lost per tick during meltdown
    public static final double MELTDOWN_GRAVITY_MULTIPLIER = 4.0;
    public static final double MELTDOWN_WITHER_RADIUS = 48.0;
    public static final int WITHER_DURATION = 200;
    public static final int WITHER_AMPLIFIER = 1;

    // === CONTAINER DATA SLOT INDICES ===
    public static final int DATA_CRAFT_PROGRESS = 0;
    public static final int DATA_CRAFT_MAX = 1;
    public static final int DATA_RF_STORED = 2;
    public static final int DATA_RF_CAPACITY = 3;
    public static final int DATA_BH_ENERGY = 4;
    public static final int DATA_BH_MAX_ENERGY = 5;
    public static final int DATA_STABILITY = 6;
    public static final int DATA_MAX_STABILITY = 7;
    public static final int DATA_INJECTORS = 8;
    public static final int DATA_CORES = 9;
    public static final int DATA_ENGINE_STATE = 10;
    public static final int DATA_BH_SCALE = 11;
    public static final int DATA_HAWKING_RF = 12;
    public static final int DATA_TIDAL_STRESS = 13;
    public static final int DATA_NET_STABILITY = 14;
    public static final int DATA_PHOTON_RATE = 15;
    public static final int DATA_SHIELD_POWER = 16;
    public static final int DATA_CRAFTING = 17;
    public static final int DATA_TEMPERATURE = 18;
    public static final int DATA_COUNT = 19;

    // === STORAGE ===
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // RF generation buffer (Hawking radiation output)
    private final CustomEnergyStorage rfBuffer = new CustomEnergyStorage(RF_BUFFER_CAPACITY, 0, MAX_RF_EXTRACT);

    // === BLACK HOLE STATE ===
    private double blackHoleEnergy = 0.0;
    private double equilibriumEnergy = 0.0; // tracks where mass "wants" to be — lags behind actual mass
    private double stability = 0.0;
    private double savedStability = STARTING_STABILITY;
    private double temperature = 0.0;       // containment temperature — the second axis of danger
    private double targetTemperature = 0.0; // what temp is trending toward (inertia delays actual change)

    // Cached rates (recomputed each tick, synced to client)
    private double hawkingRadiationRate = 0.0;
    private double totalPhotonRate = 0.0;
    private double totalShieldPower = 0.0;
    private double tidalStressFactor = 1.0;
    private double netStabilityRate = 0.0;
    private double exoticMatterCraftSpeed = 0.0;

    // Exotic matter production
    private double craftProgress = 0.0;
    private boolean crafting = false;

    // Structure
    private int activeInjectors = 0;
    private int coreCount = 0;
    private boolean formed = false;
    private List<BlockPos> structurePositions = new ArrayList<>();
    private List<BlockPos> corePositions = new ArrayList<>();
    private List<BlockPos> portPositions = new ArrayList<>();
    private List<PortMode> portModes = new ArrayList<>();
    private BlockPos centerPos = null;

    // Engine state machine
    private EngineState engineState = EngineState.NORMAL;
    private float blackHoleScale = 0.1f;

    // Tick counters
    private int injectorScanTick = 0;
    private int damageTick = 0;
    private int witherTick = 0;

    // Client-side sound fields
    private int wardenSoundCooldown = 0;
    private SimpleSoundInstance customSound = null;
    private boolean soundStarted = false;

    // === CONTAINER DATA ===
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_CRAFT_PROGRESS -> (int) craftProgress;
                case DATA_CRAFT_MAX -> EXOTIC_MATTER_BASE_TIME;
                case DATA_RF_STORED -> rfBuffer.getEnergyStored();
                case DATA_RF_CAPACITY -> rfBuffer.getMaxEnergyStored();
                case DATA_BH_ENERGY -> (int) (blackHoleEnergy * 10);
                case DATA_BH_MAX_ENERGY -> (int) (MAX_ENERGY * 10);
                case DATA_STABILITY -> (int) (stability * 100);
                case DATA_MAX_STABILITY -> (int) (MAX_STABILITY * 100);
                case DATA_INJECTORS -> activeInjectors;
                case DATA_CORES -> coreCount;
                case DATA_ENGINE_STATE -> engineState.ordinal();
                case DATA_BH_SCALE -> (int) (blackHoleScale * 100);
                case DATA_HAWKING_RF -> (int) hawkingRadiationRate;
                case DATA_TIDAL_STRESS -> (int) (tidalStressFactor * 100);
                case DATA_NET_STABILITY -> (int) (netStabilityRate * 10000);
                case DATA_PHOTON_RATE -> (int) (totalPhotonRate * 10);
                case DATA_SHIELD_POWER -> (int) totalShieldPower;
                case DATA_CRAFTING -> crafting ? 1 : 0;
                case DATA_TEMPERATURE -> (int) temperature;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_CRAFT_PROGRESS -> craftProgress = value;
                case DATA_RF_STORED -> rfBuffer.setEnergy(value);
                case DATA_BH_ENERGY -> blackHoleEnergy = value / 10.0;
                case DATA_STABILITY -> stability = value / 100.0;
                case DATA_ENGINE_STATE -> engineState = EngineState.fromOrdinal(value);
                case DATA_BH_SCALE -> blackHoleScale = value / 100.0f;
                case DATA_TEMPERATURE -> temperature = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public SingularityControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_CONTROLLER.get(), pos, state);
    }

    // --- Accessors ---

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public IEnergyStorage getRfBuffer() {
        return rfBuffer;
    }

    public CustomEnergyStorage getRfBufferInternal() {
        return rfBuffer;
    }

    public boolean isFormed() {
        return formed;
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

    public double getBlackHoleEnergy() {
        return blackHoleEnergy;
    }

    public double getHawkingRadiationRate() {
        return hawkingRadiationRate;
    }

    public double getTidalStressFactor() {
        return tidalStressFactor;
    }

    // --- Structure Management ---

    public void tryFormStructure() {
        if (level == null || level.isClientSide()) return;

        SingularityPattern.ensureLoaded();
        SingularityPattern.ValidationResult result = SingularityPattern.validate(level, worldPosition);

        if (result.valid()) {
            formed = true;
            engineState = EngineState.NORMAL;
            structurePositions = result.structurePositions();
            corePositions = result.corePositions();
            coreCount = corePositions.size();
            centerPos = SingularityPattern.getCenterPosition(worldPosition);
            stability = savedStability;
            blackHoleEnergy = STARTING_ENERGY;
            equilibriumEnergy = STARTING_ENERGY; // start in equilibrium — no initial shock
            temperature = 0.0;
            targetTemperature = 0.0;
            blackHoleScale = (float) (0.1 + 0.9 * (blackHoleEnergy / MAX_ENERGY));
            portPositions.clear();
            portModes.clear();

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

            level.setBlock(worldPosition, getBlockState()
                    .setValue(SingularityControllerBlock.FORMED, true)
                    .setValue(SingularityControllerBlock.MELTDOWN, false),
                    Block.UPDATE_ALL);
            setChanged();
            syncToClient();
        }
    }

    public void disassembleStructure() {
        if (level == null || level.isClientSide()) return;
        if (!formed) return;

        // Structure is permanent in NORMAL and MELTDOWN states
        if (engineState == EngineState.NORMAL || engineState == EngineState.MELTDOWN) return;

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
        craftProgress = 0;
        stability = 0.0;
        activeInjectors = 0;
        crafting = false;
        blackHoleEnergy = 0;
        equilibriumEnergy = 0;
        temperature = 0;
        targetTemperature = 0;

        BlockState state = getBlockState();
        if (state.getValue(SingularityControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(SingularityControllerBlock.FORMED, false)
                    .setValue(SingularityControllerBlock.MELTDOWN, false),
                    Block.UPDATE_ALL);
        }
        setChanged();
        syncToClient();
    }

    public void enterMeltdown() {
        if (level == null || level.isClientSide()) return;

        engineState = EngineState.MELTDOWN;
        crafting = false;
        craftProgress = 0;

        setStructureBlocksMeltdown(true);

        if (centerPos != null) {
            level.playSound(null, centerPos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 3.0f, 0.5f);
        }

        setChanged();
        syncToClient();
    }

    public void neutralize() {
        if (level == null || level.isClientSide()) return;

        engineState = EngineState.NEUTRALIZED;
        blackHoleScale = 0.0f;
        blackHoleEnergy = 0.0;
        equilibriumEnergy = 0.0;
        temperature = 0.0;
        targetTemperature = 0.0;
        crafting = false;
        craftProgress = 0;
        stability = 0.0;
        savedStability = STARTING_STABILITY;

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

        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(SingularityControllerBlock.FORMED, false)
                .setValue(SingularityControllerBlock.MELTDOWN, false), Block.UPDATE_ALL);

        setChanged();
        syncToClient();
    }

    public void reformStructure() {
        if (level == null || level.isClientSide()) return;
        if (engineState != EngineState.NEUTRALIZED) return;

        engineState = EngineState.NORMAL;
        blackHoleScale = 0.1f;
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
        // Structure is permanent once formed
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
                    worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    // --- Physics Tick ---

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                   SingularityControllerBlockEntity be) {
        if (!be.formed) return;
        if (be.engineState == EngineState.NEUTRALIZED) return;

        boolean isMeltdown = be.engineState == EngineState.MELTDOWN;
        long gameTime = level.getGameTime();
        boolean changed = false;

        // ===== PHASE 1 & 2: SCAN INJECTORS AND SHIELD PORTS (every 20 ticks) =====
        be.injectorScanTick++;
        if (be.injectorScanTick >= 20) {
            be.injectorScanTick = 0;
            int oldCount = be.activeInjectors;
            be.scanForInjectorsWithRates();
            be.scanForShieldPorts();
            if (be.activeInjectors != oldCount) changed = true;
        }

        // ===== PHASE 3: PHOTONIC INJECTION (Energy Input) =====
        if (!isMeltdown && be.totalPhotonRate > 0) {
            double energyRatio = be.blackHoleEnergy / MAX_ENERGY;
            double diminishing = 1.0 - energyRatio * energyRatio;
            double energyGain = be.totalPhotonRate * ENERGY_PER_PHOTON * diminishing;
            be.blackHoleEnergy = Math.min(MAX_ENERGY, be.blackHoleEnergy + energyGain);
            changed = true;
        }

        // ===== PHASE 4: HAWKING RADIATION (RF Generation) =====
        // RF output scales EXPONENTIALLY with mass. More mass = dramatically more power.
        // At sweet spot (1000): ~50k RF/t. At 1500: ~500k. At 1800+: millions.
        // This is the core risk/reward — pushing mass high gives insane power but stability crumbles.
        // No RF generation during meltdown — the black hole is unstable.
        if (!isMeltdown && be.blackHoleEnergy >= RADIATION_IGNITION_THRESHOLD) {
            double massRatio = be.blackHoleEnergy / MAX_ENERGY;
            int rfGenerated = (int) (RF_BASE * Math.exp(RF_EXPONENT * massRatio));
            be.hawkingRadiationRate = rfGenerated;
            be.rfBuffer.addEnergy(rfGenerated);
            changed = true;
        } else {
            be.hawkingRadiationRate = 0;
        }

        // ===== PHASE 5: PUSH RF TO OUTPUT PORTS =====
        be.pushRfToOutputPorts();

        // ===== PHASE 5.5: EQUILIBRIUM TRACKING =====
        // Equilibrium closes only 1% of the gap per tick — changes linger for ~5 seconds.
        // This means sudden injection rate changes create sustained deviation penalties.
        // You can't just spike and recover — the system remembers.
        double gap = be.blackHoleEnergy - be.equilibriumEnergy;
        be.equilibriumEnergy += gap * EQUILIBRIUM_RATE;

        // ===== PHASE 5.6: CONTAINMENT TEMPERATURE =====
        // Real thermal physics: heat_in is constant (mass-based), heat_out scales with temperature.
        // This naturally finds an equilibrium: temp rises until cooling matches generation.
        // Higher mass → higher equilibrium temp. More shields → lower equilibrium temp.
        // Temperature has INERTIA — actual temp lags behind equilibrium, creating dangerous delays.
        // Exotic matter production causes instant temp spikes that take time to cool back down.
        //
        // Equilibrium formula: heatIn = coolingCoeff * (temp / refTemp)
        //   → tempEquilibrium = heatIn * refTemp / coolingCoeff
        {
            if (be.blackHoleEnergy >= RADIATION_IGNITION_THRESHOLD) {
                // Heat generation: mass^1.5 — superlinear, gets brutal at high mass
                double heatIn = HEAT_GENERATION_MULT * Math.pow(be.blackHoleEnergy, HEAT_EXPONENT);

                // Cooling coefficient: passive + shields. Cooling power scales with temperature.
                double coolingCoeff = PASSIVE_COOLING_COEFF + be.totalShieldPower * COOLING_COEFFICIENT;

                // Target temperature = equilibrium point where heat_in = heat_out
                be.targetTemperature = heatIn * REFERENCE_TEMP / coolingCoeff;
                be.targetTemperature = Math.min(MAX_TEMPERATURE, be.targetTemperature);

                // Actual temperature moves toward target with inertia — slow, dangerous lag
                double tempGap = be.targetTemperature - be.temperature;
                be.temperature += tempGap * THERMAL_INERTIA;
                be.temperature = Math.max(0, Math.min(MAX_TEMPERATURE, be.temperature));
            } else {
                // No black hole = everything cools toward 0
                be.targetTemperature = 0;
                double tempGap = be.targetTemperature - be.temperature;
                be.temperature += tempGap * THERMAL_INERTIA;
                be.temperature = Math.max(0, be.temperature);
            }
        }

        // ===== PHASE 5.7: CONTAINMENT FAILURE EVENTS =====
        // Above 7000 temp: random sudden stability drops. The higher the temp, the more likely.
        // These are the "oh no" moments that make you scramble.
        if (be.temperature > CONTAINMENT_FAILURE_THRESHOLD && !isMeltdown) {
            double failureRatio = (be.temperature - CONTAINMENT_FAILURE_THRESHOLD)
                    / (MAX_TEMPERATURE - CONTAINMENT_FAILURE_THRESHOLD);
            double chance = CONTAINMENT_FAILURE_CHANCE * failureRatio;
            if (level.getRandom().nextDouble() < chance) {
                be.stability -= CONTAINMENT_FAILURE_STABILITY_HIT;
                be.stability = Math.max(0.0, be.stability);
                // Sound cue for the player
                if (be.centerPos != null) {
                    level.playSound(null, be.centerPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.5f, 0.3f);
                }
            }
        }

        // ===== PHASE 6: STABILITY CALCULATION =====
        // Stability is now attacked from THREE directions:
        //   1. Mass position (distance from sweet spot, asymmetric)
        //   2. Rate of change (deviation from equilibrium)
        //   3. TEMPERATURE (the big one — exponential amplifier)
        // Shields are split between cooling (Phase 5.6) and stabilization (here).
        // This forces a choice: do you cool or stabilize? You need BOTH but shields are finite.
        {
            if (be.blackHoleEnergy < RADIATION_IGNITION_THRESHOLD) {
                be.tidalStressFactor = 1.0;
                be.netStabilityRate = 0;
            } else {
                // Tidal stress — oscillates above sweet spot
                double distFromSweet = (be.blackHoleEnergy - SWEET_SPOT_ENERGY) / SWEET_SPOT_ENERGY;
                double overshootRatio = Math.max(0, distFromSweet);
                double smoothstepVal = smoothstep(0.4, 0.8, overshootRatio);
                double sinVal = Math.sin(gameTime * 0.03);
                be.tidalStressFactor = 1.0 + 1.5 * sinVal * sinVal * smoothstepVal;

                // Mass position decay (sweet spot based, asymmetric)
                double massDecay;
                if (be.blackHoleEnergy <= SWEET_SPOT_ENERGY) {
                    double undershoot = (SWEET_SPOT_ENERGY - be.blackHoleEnergy) / SWEET_SPOT_ENERGY;
                    massDecay = UNDERSHOOT_DECAY_MULT * undershoot * undershoot;
                } else {
                    double overshoot = (be.blackHoleEnergy - SWEET_SPOT_ENERGY) / SWEET_SPOT_ENERGY;
                    massDecay = OVERSHOOT_DECAY_MULT * overshoot * overshoot;
                }

                // Rate-of-change penalty (equilibrium deviation) — now with slower convergence
                double deviation = Math.abs(be.blackHoleEnergy - be.equilibriumEnergy) / SWEET_SPOT_ENERGY;
                double deviationDecay = deviation * deviation * DEVIATION_DECAY_MULT;

                // Crafting strain
                double craftingStrain = be.crafting ? EXOTIC_MATTER_STABILITY_COST : 0.0;

                // Base decay from mass + deviation + crafting, amplified by tidal stress
                double baseDecay = (BASE_STABILITY_DECAY + massDecay + deviationDecay + craftingStrain)
                        * be.tidalStressFactor;

                // TEMPERATURE AMPLIFIER — this is the main danger source.
                // Below TEMP_SAFE_ZONE (1500): no extra decay — operating temperature.
                // Above safe zone: exponential decay kicks in hard.
                // This means temp is fine until it crosses the threshold, then it gets scary fast.
                double temperatureDecay = 0.0;
                if (be.temperature > TEMP_SAFE_ZONE) {
                    double excessTemp = be.temperature - TEMP_SAFE_ZONE;
                    temperatureDecay = TEMP_STABILITY_MULT * Math.pow(excessTemp, TEMP_STABILITY_EXPONENT);
                }

                double totalDecay = baseDecay + temperatureDecay;

                // Stability recovery — shields, but weaker and dropping off at high mass
                double overMassRatio = Math.max(0, be.blackHoleEnergy - SWEET_SPOT_ENERGY) / SWEET_SPOT_ENERGY;
                double shieldEffectiveness = 1.0 - overMassRatio * SHIELD_EFFECTIVENESS_DROPOFF;
                shieldEffectiveness = Math.max(0.1, shieldEffectiveness);
                double stabilityRecovery = be.totalShieldPower * SHIELD_STABILITY_EFFICIENCY * shieldEffectiveness
                        + (be.coreCount > 0 ? CORE_PASSIVE_STABILIZATION * be.coreCount : 0.0);

                be.netStabilityRate = stabilityRecovery - totalDecay;
                be.stability += be.netStabilityRate;
                be.stability = Math.max(0.0, Math.min(MAX_STABILITY, be.stability));
            }
            changed = true;
        }

        // ===== PHASE 6.5: MELTDOWN MASS DECAY =====
        // During meltdown, the black hole bleeds mass but never fully evaporates.
        // Equilibrium snaps to current mass so recovery is smooth (no residual deviation shock).
        if (isMeltdown) {
            be.blackHoleEnergy = Math.max(RADIATION_IGNITION_THRESHOLD, be.blackHoleEnergy - MELTDOWN_MASS_DECAY);
            be.equilibriumEnergy = be.blackHoleEnergy; // snap — no deviation shock on recovery
        }

        // ===== PHASE 7: STATE TRANSITIONS =====
        if (be.engineState == EngineState.NORMAL && be.stability <= 0.0) {
            be.enterMeltdown();
            return;
        }
        if (be.engineState == EngineState.MELTDOWN && be.stability >= RESTABILIZE_THRESHOLD) {
            be.engineState = EngineState.NORMAL;
            be.setStructureBlocksMeltdown(false);
            be.setChanged();
            be.syncToClient();
        }

        // ===== PHASE 8: VISUAL SCALE (energy-based, lerped for smooth transitions) =====
        float targetScale = (float) (0.1 + 0.9 * (be.blackHoleEnergy / MAX_ENERGY));
        be.blackHoleScale += (targetScale - be.blackHoleScale) * 0.02f;

        // ===== PHASE 9: GRAVITY =====
        be.applyGravity(level, isMeltdown);

        // ===== PHASE 10: EXOTIC MATTER PRODUCTION =====
        if (!isMeltdown) {
            be.tickExoticMatterProduction();
        } else {
            be.crafting = false;
            be.craftProgress = 0;
        }

        // ===== PHASE 11: CONSUME SHIELD RF =====
        be.consumeShieldRf();

        // ===== SYNC =====
        if (changed || gameTime % 10 == 0) {
            be.setChanged();
            be.syncToClient();
        }
    }

    // --- Physics Helper Methods ---

    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }

    private void scanForInjectorsWithRates() {
        if (centerPos == null || level == null) return;

        SingularityPattern.ensureLoaded();
        int shellRadius = SingularityPattern.getHeight() / 2;

        int count = 0;
        double totalRate = 0.0;
        for (Direction dir : Direction.values()) {
            for (int dist = shellRadius; dist <= shellRadius + 20; dist++) {
                BlockPos checkPos = centerPos.relative(dir, dist);
                BlockState checkState = level.getBlockState(checkPos);

                if (checkState.getBlock() instanceof PhotonicInjectorBlock) {
                    if (checkState.getValue(PhotonicInjectorBlock.ACTIVE)) {
                        Direction injectorFacing = checkState.getValue(PhotonicInjectorBlock.FACING);
                        if (injectorFacing == dir.getOpposite()) {
                            count++;
                            BlockEntity be = level.getBlockEntity(checkPos);
                            if (be instanceof PhotonicInjectorBlockEntity injector) {
                                totalRate += injector.getInjectionRate() / 100.0;
                            }
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
        totalPhotonRate = totalRate;
    }

    private void scanForShieldPorts() {
        if (level == null) return;
        double total = 0.0;
        for (int i = 0; i < portPositions.size(); i++) {
            if (i < portModes.size() && portModes.get(i) == PortMode.ENERGY_INPUT) {
                BlockEntity be = level.getBlockEntity(portPositions.get(i));
                if (be instanceof SingularityPortBlockEntity port) {
                    total += port.getShieldPowerRate();
                }
            }
        }
        totalShieldPower = total;
    }

    private void consumeShieldRf() {
        if (level == null) return;
        for (int i = 0; i < portPositions.size(); i++) {
            if (i < portModes.size() && portModes.get(i) == PortMode.ENERGY_INPUT) {
                BlockEntity be = level.getBlockEntity(portPositions.get(i));
                if (be instanceof SingularityPortBlockEntity port) {
                    port.consumeShieldRfFromAdjacent();
                }
            }
        }
    }

    private void pushRfToOutputPorts() {
        if (level == null) return;
        for (int i = 0; i < portPositions.size(); i++) {
            if (i < portModes.size() && portModes.get(i) == PortMode.ENERGY_OUTPUT) {
                BlockEntity be = level.getBlockEntity(portPositions.get(i));
                if (be instanceof SingularityPortBlockEntity port) {
                    port.pushRfToAdjacent(rfBuffer);
                }
            }
        }
    }

    private void applyGravity(Level level, boolean isMeltdown) {
        damageTick++;
        if (centerPos == null) return;

        double energyRatio = blackHoleEnergy / MAX_ENERGY;
        double gravityMult = 0.1 + 0.9 * energyRatio;
        if (isMeltdown) gravityMult *= MELTDOWN_GRAVITY_MULTIPLIER;

        double gravRadius = BASE_GRAVITY_RADIUS * gravityMult;
        double pullStr = BASE_PULL_STRENGTH * gravityMult;
        double maxPull = BASE_MAX_PULL * gravityMult;
        double killRadius = BASE_KILL_RADIUS * gravityMult;
        double damageRadius = BASE_DAMAGE_RADIUS * gravityMult;

        double cx = centerPos.getX() + 0.5;
        double cy = centerPos.getY() + 0.5;
        double cz = centerPos.getZ() + 0.5;

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
            } else if (distance < damageRadius && damageTick % 10 == 0) {
                entity.hurt(level.damageSources().generic(), DAMAGE_PER_TICK);
            }
        }

        // Wither effect during meltdown
        if (isMeltdown) {
            witherTick++;
            if (witherTick >= 40) {
                witherTick = 0;
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

    private void tickExoticMatterProduction() {
        double sweetSpotRatio = blackHoleEnergy / SWEET_SPOT_ENERGY;

        if (sweetSpotRatio < EXOTIC_MATTER_ENERGY_THRESHOLD
                || stability < MIN_STABILITY_TO_OPERATE
                || !canOutputExoticMatter()) {
            crafting = false;
            craftProgress = 0;
            return;
        }

        crafting = true;
        double craftSpeed = (sweetSpotRatio - EXOTIC_MATTER_ENERGY_THRESHOLD) * EXOTIC_MATTER_CRAFT_SPEED_MULT;
        exoticMatterCraftSpeed = craftSpeed;
        craftProgress += craftSpeed;

        if (craftProgress >= EXOTIC_MATTER_BASE_TIME) {
            produceExoticMatter();
            blackHoleEnergy = Math.max(0, blackHoleEnergy - EXOTIC_MATTER_ENERGY_COST);
            // Heat spike on production — each exotic matter causes a temperature surge
            targetTemperature = Math.min(MAX_TEMPERATURE, targetTemperature + EXOTIC_MATTER_HEAT_SPIKE);
            craftProgress = 0;
        }
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

    // --- Client Tick (sound) ---

    public static void clientTick(Level level, BlockPos pos, BlockState state,
                                   SingularityControllerBlockEntity be) {
        if (!be.formed) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        be.wardenSoundCooldown--;
        if (be.wardenSoundCooldown <= 0) {
            level.playLocalSound(x, y, z,
                    SoundEvents.WARDEN_AMBIENT, SoundSource.BLOCKS,
                    1.5f, 0.5f, false);
            be.wardenSoundCooldown = 60;
        }

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
        tag.putInt("RfBuffer", rfBuffer.getEnergyStored());
        tag.putDouble("BlackHoleEnergy", blackHoleEnergy);
        tag.putDouble("EquilibriumEnergy", equilibriumEnergy);
        tag.putDouble("Temperature", temperature);
        tag.putDouble("TargetTemperature", targetTemperature);
        tag.putDouble("CraftProgress", craftProgress);
        tag.putBoolean("Crafting", crafting);
        tag.putDouble("Stability", stability);
        tag.putDouble("SavedStability", savedStability);
        tag.putInt("Injectors", activeInjectors);
        tag.putInt("CoreCount", coreCount);
        tag.putBoolean("Formed", formed);
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

        // Backward compatibility: old worlds used "Energy", new uses "RfBuffer"
        if (tag.contains("RfBuffer")) {
            rfBuffer.setEnergy(tag.getInt("RfBuffer"));
        } else if (tag.contains("Energy")) {
            rfBuffer.setEnergy(tag.getInt("Energy"));
        }

        blackHoleEnergy = tag.contains("BlackHoleEnergy") ? tag.getDouble("BlackHoleEnergy") : 0.0;
        equilibriumEnergy = tag.contains("EquilibriumEnergy") ? tag.getDouble("EquilibriumEnergy") : blackHoleEnergy;
        temperature = tag.contains("Temperature") ? tag.getDouble("Temperature") : 0.0;
        targetTemperature = tag.contains("TargetTemperature") ? tag.getDouble("TargetTemperature") : 0.0;
        craftProgress = tag.contains("CraftProgress") ? tag.getDouble("CraftProgress") : 0.0;
        crafting = tag.getBoolean("Crafting");
        stability = tag.getDouble("Stability");
        savedStability = tag.contains("SavedStability") ? tag.getDouble("SavedStability") : STARTING_STABILITY;
        activeInjectors = tag.getInt("Injectors");
        coreCount = tag.getInt("CoreCount");
        formed = tag.getBoolean("Formed");
        engineState = tag.contains("EngineState") ? EngineState.fromOrdinal(tag.getInt("EngineState")) : EngineState.NORMAL;
        blackHoleScale = tag.contains("BlackHoleScale") ? tag.getFloat("BlackHoleScale") : 0.1f;

        // Backward compatibility: formed worlds without BlackHoleEnergy should start with energy
        if (formed && blackHoleEnergy <= 0 && engineState != EngineState.NEUTRALIZED) {
            blackHoleEnergy = STARTING_ENERGY;
            blackHoleScale = (float) (0.1 + 0.9 * (blackHoleEnergy / MAX_ENERGY));
        }

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
