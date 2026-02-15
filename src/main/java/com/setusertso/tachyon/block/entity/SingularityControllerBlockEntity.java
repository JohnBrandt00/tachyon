package com.setusertso.tachyon.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.ExoticMatterCoreBlock;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.block.SingularityControllerBlock;
import com.setusertso.tachyon.block.SingularityPattern;
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
import net.minecraft.world.entity.Entity;
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

    // Gravity constants — matches the containment field radius
    private static final double GRAVITY_RADIUS = 16.0;
    private static final double PULL_STRENGTH = 0.06;
    private static final double MAX_PULL = 0.6;
    private static final double KILL_RADIUS = 1.5;
    private static final double DAMAGE_RADIUS = 4.5;
    private static final float DAMAGE_PER_TICK = 2.0f;
    private static final float KILL_DAMAGE = 20.0f;

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
    private int activeInjectors = 0;
    private int coreCount = 0;
    private boolean formed = false;
    private boolean processing = false;
    private List<BlockPos> structurePositions = new ArrayList<>();
    private List<BlockPos> corePositions = new ArrayList<>();
    private BlockPos centerPos = null;

    // Tick counters
    private int injectorScanTick = 0;
    private int damageTick = 0;

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
            }
        }

        @Override
        public int getCount() {
            return 10;
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

    // --- Structure Management ---

    public void tryFormStructure() {
        if (level == null || level.isClientSide()) return;

        SingularityPattern.ensureLoaded();
        SingularityPattern.ValidationResult result = SingularityPattern.validate(level, worldPosition);

        if (result.valid()) {
            formed = true;
            structurePositions = result.structurePositions();
            corePositions = result.corePositions();
            coreCount = corePositions.size();
            centerPos = SingularityPattern.getCenterPosition(worldPosition);
            stability = STARTING_STABILITY;

            // Set master pos on all slave block entities
            for (BlockPos sPos : structurePositions) {
                BlockEntity be = level.getBlockEntity(sPos);
                if (be instanceof SingularityCasingBlockEntity casing) {
                    casing.setMasterPos(worldPosition);
                } else if (be instanceof SingularityPortBlockEntity port) {
                    port.setMasterPos(worldPosition);
                } else if (be instanceof ExoticMatterCoreBlockEntity core) {
                    core.setMasterPos(worldPosition);
                }
            }

            // Update blockstate
            level.setBlock(worldPosition, getBlockState().setValue(SingularityControllerBlock.FORMED, true),
                    Block.UPDATE_ALL);
            setChanged();
            syncToClient();
        }
    }

    public void disassembleStructure() {
        if (level == null || level.isClientSide()) return;
        if (!formed) return;

        // Clear master pos on all slave block entities
        for (BlockPos sPos : structurePositions) {
            BlockEntity be = level.getBlockEntity(sPos);
            if (be instanceof SingularityCasingBlockEntity casing) {
                casing.setMasterPos(null);
            } else if (be instanceof SingularityPortBlockEntity port) {
                port.setMasterPos(null);
            } else if (be instanceof ExoticMatterCoreBlockEntity core) {
                core.setMasterPos(null);
            }
        }

        formed = false;
        structurePositions.clear();
        corePositions.clear();
        coreCount = 0;
        centerPos = null;
        progress = 0;
        stability = 0.0;
        activeInjectors = 0;
        processing = false;

        // Update blockstate
        BlockState state = getBlockState();
        if (state.getValue(SingularityControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(SingularityControllerBlock.FORMED, false),
                    Block.UPDATE_ALL);
        }
        setChanged();
        syncToClient();
    }

    public void onNeighborChanged(BlockPos changedPos) {
        if (level == null || level.isClientSide() || !formed) return;
        SingularityPattern.ensureLoaded();
        SingularityPattern.ValidationResult result = SingularityPattern.validate(level, worldPosition);
        if (!result.valid()) {
            disassembleStructure();
        }
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

        // --- Gravitational pull ---
        be.damageTick++;
        if (be.centerPos != null) {
            double cx = be.centerPos.getX() + 0.5;
            double cy = be.centerPos.getY() + 0.5;
            double cz = be.centerPos.getZ() + 0.5;

            AABB pullArea = new AABB(
                    cx - GRAVITY_RADIUS, cy - GRAVITY_RADIUS, cz - GRAVITY_RADIUS,
                    cx + GRAVITY_RADIUS, cy + GRAVITY_RADIUS, cz + GRAVITY_RADIUS);

            for (Entity entity : level.getEntitiesOfClass(Entity.class, pullArea)) {
                double dx = cx - entity.getX();
                double dy = cy - (entity.getY() + entity.getBbHeight() * 0.5);
                double dz = cz - entity.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > GRAVITY_RADIUS || distance < 0.1) continue;
                if (entity instanceof Player player && player.isCreative()) continue;

                double nx = dx / distance;
                double ny = dy / distance;
                double nz = dz / distance;

                double strength = Math.min(MAX_PULL, PULL_STRENGTH / (distance * 0.15));
                Vec3 motion = entity.getDeltaMovement();
                entity.setDeltaMovement(motion.x + nx * strength, motion.y + ny * strength, motion.z + nz * strength);
                entity.hurtMarked = true;

                if (distance < KILL_RADIUS) {
                    if (entity instanceof ItemEntity) {
                        entity.discard();
                    } else {
                        entity.hurt(level.damageSources().generic(), KILL_DAMAGE);
                    }
                } else if (distance < DAMAGE_RADIUS && be.damageTick % 10 == 0) {
                    entity.hurt(level.damageSources().generic(), DAMAGE_PER_TICK);
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

        // Safe shutdown at 0 stability
        if (be.stability <= 0.0) {
            be.disassembleStructure();
            return;
        }

        // Process if conditions met
        if (be.stability >= MIN_STABILITY_TO_OPERATE
                && be.energy.getEnergyStored() >= ENERGY_PER_TICK
                && be.condensedLightBuffer > 0
                && be.canOutputExoticMatter()) {

            be.energy.consumeEnergy(ENERGY_PER_TICK);
            be.progress++;
            be.processing = true;

            // Consume light from buffer periodically
            if (be.progress % LIGHT_CONSUME_INTERVAL == 0) {
                be.condensedLightBuffer--;
            }

            // Refill light buffer from input slot
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
            // Try to refill light buffer even when not processing
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

        int count = 0;
        for (Direction dir : Direction.values()) {
            for (int dist = 6; dist <= 16; dist++) {
                BlockPos checkPos = centerPos.relative(dir, dist);
                BlockState checkState = level.getBlockState(checkPos);

                if (checkState.getBlock() instanceof PhotonicInjectorBlock) {
                    if (checkState.getValue(PhotonicInjectorBlock.ACTIVE)) {
                        // Check facing aims toward center
                        Direction injectorFacing = checkState.getValue(PhotonicInjectorBlock.FACING);
                        if (injectorFacing == dir.getOpposite()) {
                            count++;
                        }
                    }
                    break;
                }

                // Stop if we hit a solid non-air block that isn't part of our structure
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
        return new SingularityControllerMenu(containerId, playerInventory, items, dataAccess);
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
        tag.putInt("Injectors", activeInjectors);
        tag.putInt("CoreCount", coreCount);
        tag.putBoolean("Formed", formed);
        tag.putBoolean("Processing", processing);

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
        activeInjectors = tag.getInt("Injectors");
        coreCount = tag.getInt("CoreCount");
        formed = tag.getBoolean("Formed");
        processing = tag.getBoolean("Processing");

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
