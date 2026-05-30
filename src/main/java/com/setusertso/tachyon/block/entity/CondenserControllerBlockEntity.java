package com.setusertso.tachyon.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.setusertso.tachyon.ModItems;

import net.minecraft.world.item.Item;
import com.setusertso.tachyon.block.CondenserControllerBlock;
import com.setusertso.tachyon.block.CondenserPattern;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModFluids;
import com.setusertso.tachyon.menu.CondenserControllerMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

public class CondenserControllerBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {

    // Slots
    public static final int INPUT_SLOT = 0;
    public static final int SLOT_COUNT = 1;

    // Processing constants
    public static final int ENERGY_CAPACITY = 500_000;
    public static final int MAX_ENERGY_RECEIVE = 10_000;
    public static final int ENERGY_PER_TICK = 50;
    public static final int FLUID_CAPACITY = 16_000;
    public static final int FLUX_PER_SHARD = 250;
    public static final int PROCESS_TIME = 100;

    // Storage
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler upgradeHandler = IUpgradeable.createUpgradeHandler(this::setChanged, getAllowedUpgrades());

    private final CustomEnergyStorage energy = new CustomEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, 0);

    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.TACHYON_FLUX_SOURCE.get());
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    // Processing state
    private int progress = 0;
    private boolean formed = false;
    private List<BlockPos> structurePositions = new ArrayList<>();

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
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> energy.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public CondenserControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDENSER_CONTROLLER.get(), pos, state);
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

    @Override
    public ItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    @Override
    public Set<Item> getAllowedUpgrades() {
        return Set.of(ModItems.SPEED_UPGRADE.get(), ModItems.ENERGY_UPGRADE.get(), ModItems.OUTPUT_UPGRADE.get());
    }

    // --- Structure Management ---

    public void tryFormStructure() {
        if (level == null || level.isClientSide()) return;
        Direction facing = getBlockState().getValue(CondenserControllerBlock.FACING);

        List<BlockPos> positions = CondenserPattern.validate(level, worldPosition, facing);
        if (positions != null) {
            formed = true;
            structurePositions = positions;

            // Set master pos on all slave block entities
            for (BlockPos sPos : structurePositions) {
                BlockEntity be = level.getBlockEntity(sPos);
                if (be instanceof CondenserCasingBlockEntity casing) {
                    casing.setMasterPos(worldPosition);
                } else if (be instanceof CondenserPortBlockEntity port) {
                    port.setMasterPos(worldPosition);
                }
            }

            // Update blockstate
            level.setBlock(worldPosition, getBlockState().setValue(CondenserControllerBlock.FORMED, true),
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
            if (be instanceof CondenserCasingBlockEntity casing) {
                casing.setMasterPos(null);
            } else if (be instanceof CondenserPortBlockEntity port) {
                port.setMasterPos(null);
            }
        }

        formed = false;
        structurePositions.clear();
        progress = 0;

        // Update blockstate
        BlockState state = getBlockState();
        if (state.getValue(CondenserControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(CondenserControllerBlock.FORMED, false),
                    Block.UPDATE_ALL);
        }
        setChanged();
        syncToClient();
    }

    public void onNeighborChanged(BlockPos changedPos) {
        if (level == null || level.isClientSide() || !formed) return;
        Direction facing = getBlockState().getValue(CondenserControllerBlock.FACING);
        if (CondenserPattern.validate(level, worldPosition, facing) == null) {
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
                                   CondenserControllerBlockEntity be) {
        if (!be.formed) return;

        boolean changed = false;
        boolean wasProcessing = be.progress > 0;

        if (be.canProcess()) {
            float speedMultiplier = be.getSpeedMultiplier();
            int progressIncrement = Math.max(1, Math.round(speedMultiplier));

            int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * be.getEnergyMultiplier()));
            be.energy.consumeEnergy(energyCost);
            be.progress += progressIncrement;
            changed = true;

            if (be.progress >= PROCESS_TIME) {
                be.processItem();
                be.progress = 0;
            }
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                changed = true;
            }
        }

        boolean isProcessing = be.progress > 0;

        if (changed) {
            be.setChanged();
            if (wasProcessing != isProcessing || (isProcessing && level.getGameTime() % 10 == 0)) {
                be.syncToClient();
            }
        }
    }

    private boolean canProcess() {
        int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * getEnergyMultiplier()));
        if (energy.getEnergyStored() < energyCost) return false;

        var input = items.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty() || !input.is(ModItems.TACHYON_SHARD.get())) return false;

        // Check if tank has space for output
        FluidStack output = new FluidStack(ModFluids.TACHYON_FLUX_SOURCE.get(), FLUX_PER_SHARD);
        int filled = fluidTank.fill(output, IFluidHandler.FluidAction.SIMULATE);
        return filled > 0;
    }

    private void processItem() {
        var input = items.getStackInSlot(INPUT_SLOT);
        input.shrink(1);

        // Base output + chance for bonus from output upgrades
        int outputAmount = FLUX_PER_SHARD;
        if (level != null && level.getRandom().nextFloat() < getOutputChance()) {
            outputAmount += FLUX_PER_SHARD; // Double output on bonus
        }
        FluidStack output = new FluidStack(ModFluids.TACHYON_FLUX_SOURCE.get(), outputAmount);
        fluidTank.fill(output, IFluidHandler.FluidAction.EXECUTE);
    }

    // --- Menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.condenser_controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CondenserControllerMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.put("Upgrades", upgradeHandler.serializeNBT(registries));
        tag.put("Energy", energy.serializeNBT(registries));
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putBoolean("Formed", formed);

        if (!structurePositions.isEmpty()) {
            int[] posData = new int[structurePositions.size() * 3];
            for (int i = 0; i < structurePositions.size(); i++) {
                BlockPos p = structurePositions.get(i);
                posData[i * 3] = p.getX();
                posData[i * 3 + 1] = p.getY();
                posData[i * 3 + 2] = p.getZ();
            }
            tag.putIntArray("StructurePositions", posData);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) items.deserializeNBT(registries, tag.getCompound("Items"));
        if (tag.contains("Upgrades")) upgradeHandler.deserializeNBT(registries, tag.getCompound("Upgrades"));
        if (tag.contains("Energy")) energy.deserializeNBT(registries, tag.get("Energy"));
        if (tag.contains("FluidTank")) fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        progress = tag.getInt("Progress");
        formed = tag.getBoolean("Formed");

        if (tag.contains("StructurePositions")) {
            int[] posData = tag.getIntArray("StructurePositions");
            structurePositions.clear();
            for (int i = 0; i < posData.length; i += 3) {
                structurePositions.add(new BlockPos(posData[i], posData[i + 1], posData[i + 2]));
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

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
