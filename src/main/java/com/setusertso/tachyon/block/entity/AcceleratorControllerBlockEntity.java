package com.setusertso.tachyon.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorPattern;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModTags;
import com.setusertso.tachyon.menu.AcceleratorControllerMenu;

import net.minecraft.world.item.Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public class AcceleratorControllerBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {

    // Slots
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    // Processing constants
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int MAX_ENERGY_RECEIVE = 10_000;
    public static final int ENERGY_PER_TICK = 500;
    public static final int FLUID_CAPACITY = 16_000;
    public static final int FLUID_PER_CRAFT_THORIUM = 1_000;
    public static final int FLUID_PER_CRAFT_ENDERPEARL = 100;
    public static final int FLUID_PER_CRAFT_PHOTON = 200;
    public static final int PROCESS_TIME = 200;

    // Windup mechanic constants
    public static final int MAX_MOMENTUM = 5000; // 5 items * 1000 each
    public static final int MOMENTUM_PER_ITEM = 1000;
    public static final int MOMENTUM_DECAY_RATE = 2; // Momentum lost per tick when idle
    public static final float MIN_SPEED_MULTIPLIER = 0.5f; // 50% speed at 0 momentum
    public static final float MAX_SPEED_MULTIPLIER = 2.0f; // 200% speed at max momentum

    // Storage
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler upgradeHandler = IUpgradeable.createUpgradeHandler(this::setChanged);

    private final CustomEnergyStorage energy = new CustomEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, 0);

    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(com.setusertso.tachyon.init.ModFluids.HELIUM_SOURCE.get());
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    // Processing state
    private int progress = 0;
    private int momentum = 0; // Windup mechanic - increases with continuous use
    private boolean formed = false;
    private List<BlockPos> structurePositions = new ArrayList<>();
    private List<BlockPos> ringPath = new ArrayList<>();

    // Container data for syncing to client GUI
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> PROCESS_TIME;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> fluidTank.getFluidAmount();
                case 5 -> fluidTank.getCapacity();
                case 6 -> momentum;
                case 7 -> MAX_MOMENTUM;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> energy.setEnergy(value);
                case 6 -> momentum = value;
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public AcceleratorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ACCELERATOR_CONTROLLER.get(), pos, state);
    }

    // --- Accessors ---

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public boolean isFormed() {
        return formed;
    }

    public List<BlockPos> getRingPath() {
        return ringPath;
    }

    public int getProgress() {
        return progress;
    }

    public int getMomentum() {
        return momentum;
    }

    @Override
    public ItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    public float getSpeedMultiplier() {
        // Interpolate between min and max speed based on momentum
        float momentumRatio = (float) momentum / MAX_MOMENTUM;
        float baseSpeed = MIN_SPEED_MULTIPLIER + (MAX_SPEED_MULTIPLIER - MIN_SPEED_MULTIPLIER) * momentumRatio;
        return baseSpeed * IUpgradeable.super.getSpeedMultiplier();
    }

    // --- Structure Management ---

    public void tryFormStructure() {
        if (level == null || level.isClientSide()) return;
        Direction facing = getBlockState().getValue(AcceleratorControllerBlock.FACING);

        if (AcceleratorPattern.validate(level, worldPosition, facing)) {
            formed = true;
            structurePositions = AcceleratorPattern.getStructurePositions(worldPosition, facing);
            ringPath = AcceleratorPattern.computeRingPath(worldPosition, facing, 2);

            // Set master pos on all slave block entities
            for (BlockPos sPos : structurePositions) {
                BlockEntity be = level.getBlockEntity(sPos);
                if (be instanceof AcceleratorCasingBlockEntity casing) {
                    casing.setMasterPos(worldPosition);
                } else if (be instanceof AcceleratorPortBlockEntity port) {
                    port.setMasterPos(worldPosition);
                }
            }

            // Update blockstate
            level.setBlock(worldPosition, getBlockState().setValue(AcceleratorControllerBlock.FORMED, true),
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
            if (be instanceof AcceleratorCasingBlockEntity casing) {
                casing.setMasterPos(null);
            } else if (be instanceof AcceleratorPortBlockEntity port) {
                port.setMasterPos(null);
            }
        }

        formed = false;
        structurePositions.clear();
        ringPath.clear();
        progress = 0;

        // Update blockstate
        BlockState state = getBlockState();
        if (state.getValue(AcceleratorControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(AcceleratorControllerBlock.FORMED, false),
                    Block.UPDATE_ALL);
        }
        setChanged();
        syncToClient();
    }

    public void onNeighborChanged(BlockPos changedPos) {
        if (level == null || level.isClientSide() || !formed) return;
        Direction facing = getBlockState().getValue(AcceleratorControllerBlock.FACING);
        if (!AcceleratorPattern.validate(level, worldPosition, facing)) {
            disassembleStructure();
        }
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
                    worldPosition.getZ(), items.getStackInSlot(i));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
                    worldPosition.getZ(), upgradeHandler.getStackInSlot(i));
        }
    }

    // --- Processing ---

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                   AcceleratorControllerBlockEntity be) {
        if (!be.formed) return;

        boolean changed = false;
        boolean wasProcessing = be.progress > 0;

        if (be.canProcess()) {
            // Apply speed multiplier to processing (momentum + upgrades)
            float speedMultiplier = be.getSpeedMultiplier();
            int progressIncrement = Math.max(1, Math.round(speedMultiplier));

            // Apply energy upgrade multiplier
            int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * be.getEnergyMultiplier()));
            be.energy.consumeEnergy(energyCost);
            be.progress += progressIncrement;
            changed = true;

            if (be.progress >= PROCESS_TIME) {
                be.processItem();
                be.progress = 0;

                // Increase momentum when item completes
                be.momentum = Math.min(MAX_MOMENTUM, be.momentum + MOMENTUM_PER_ITEM);
            }
        } else {
            // Decay momentum when idle
            if (be.momentum > 0) {
                be.momentum = Math.max(0, be.momentum - MOMENTUM_DECAY_RATE);
                changed = true;
            }

            if (be.progress > 0) {
                be.progress = 0;
                changed = true;
            }
        }

        boolean isProcessing = be.progress > 0;

        if (changed) {
            be.setChanged();
            // Sync to client when processing state changes or every 10 ticks while processing
            if (wasProcessing != isProcessing || (isProcessing && level.getGameTime() % 10 == 0)) {
                be.syncToClient();
            }
        }
    }

    private boolean canProcess() {
        // Need energy (accounting for energy upgrade)
        int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * getEnergyMultiplier()));
        if (energy.getEnergyStored() < energyCost) return false;

        // Check input item
        var input = items.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return false;

        // Determine recipe and fluid requirement
        boolean isThorium = input.is(ModTags.Items.INGOTS_THORIUM);
        boolean isEnderPearl = input.is(Items.ENDER_PEARL);
        boolean isRawPhoton = input.is(ModItems.RAW_PHOTON.get());

        if (!isThorium && !isEnderPearl && !isRawPhoton) return false;

        // Check fluid requirement based on input
        int requiredFluid = isThorium ? FLUID_PER_CRAFT_THORIUM
                : isRawPhoton ? FLUID_PER_CRAFT_PHOTON
                : FLUID_PER_CRAFT_ENDERPEARL;
        if (fluidTank.getFluidAmount() < requiredFluid) return false;

        // Need space for output (Tachyon Shard or Excited Photon)
        var output = items.getStackInSlot(OUTPUT_SLOT);
        var expectedOutput = isRawPhoton ? ModItems.EXCITED_PHOTON.get() : ModItems.TACHYON_SHARD.get();
        if (!output.isEmpty()) {
            if (!output.is(expectedOutput)) return false;
            if (output.getCount() >= output.getMaxStackSize()) return false;
        }
        return true;
    }

    private void processItem() {
        var input = items.getStackInSlot(INPUT_SLOT);

        boolean isRawPhoton = input.is(ModItems.RAW_PHOTON.get());

        // Determine fluid consumption based on input
        int fluidToConsume = input.is(ModTags.Items.INGOTS_THORIUM)
            ? FLUID_PER_CRAFT_THORIUM
            : isRawPhoton ? FLUID_PER_CRAFT_PHOTON
            : FLUID_PER_CRAFT_ENDERPEARL;

        // Consume input
        items.extractItem(INPUT_SLOT, 1, false);
        // Consume helium
        fluidTank.drain(fluidToConsume, IFluidHandler.FluidAction.EXECUTE);

        // Determine output item
        var outputItem = isRawPhoton ? ModItems.EXCITED_PHOTON.get() : ModItems.TACHYON_SHARD.get();

        // Determine output count (output upgrade gives chance to double)
        int outputCount = 1;
        float outputChance = getOutputChance();
        if (outputChance > 0 && level != null && level.getRandom().nextFloat() < outputChance) {
            outputCount = 2;
        }

        // Produce output
        var existing = items.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            var stack = outputItem.getDefaultInstance();
            stack.setCount(outputCount);
            items.setStackInSlot(OUTPUT_SLOT, stack);
        } else {
            existing.grow(outputCount);
        }
    }

    // --- Menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.accelerator_controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AcceleratorControllerMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.put("Upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Fluid", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putInt("Momentum", momentum);
        tag.putBoolean("Formed", formed);

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

            ListTag pathList = new ListTag();
            for (BlockPos rPos : ringPath) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", rPos.getX());
                posTag.putInt("Y", rPos.getY());
                posTag.putInt("Z", rPos.getZ());
                pathList.add(posTag);
            }
            tag.put("RingPath", pathList);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("Upgrades")) {
            upgradeHandler.deserializeNBT(registries, tag.getCompound("Upgrades"));
        }
        energy.setEnergy(tag.getInt("Energy"));
        fluidTank.readFromNBT(registries, tag.getCompound("Fluid"));
        progress = tag.getInt("Progress");
        momentum = tag.getInt("Momentum");
        formed = tag.getBoolean("Formed");

        structurePositions.clear();
        if (tag.contains("StructurePositions")) {
            ListTag posList = tag.getList("StructurePositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < posList.size(); i++) {
                CompoundTag posTag = posList.getCompound(i);
                structurePositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        ringPath.clear();
        if (tag.contains("RingPath")) {
            ListTag pathList = tag.getList("RingPath", Tag.TAG_COMPOUND);
            for (int i = 0; i < pathList.size(); i++) {
                CompoundTag posTag = pathList.getCompound(i);
                ringPath.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
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
