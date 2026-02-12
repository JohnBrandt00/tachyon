package com.setusertso.tachyon.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorPattern;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModTags;
import com.setusertso.tachyon.menu.AcceleratorControllerMenu;

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

public class AcceleratorControllerBlockEntity extends BlockEntity implements MenuProvider {

    // Slots
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    // Processing constants
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int MAX_ENERGY_RECEIVE = 10_000;
    public static final int ENERGY_PER_TICK = 500;
    public static final int FLUID_CAPACITY = 16_000;
    public static final int FLUID_PER_CRAFT = 1_000;
    public static final int PROCESS_TIME = 200;

    // Storage
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

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
    }

    // --- Processing ---

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                   AcceleratorControllerBlockEntity be) {
        if (!be.formed) return;

        boolean changed = false;

        if (be.canProcess()) {
            be.energy.consumeEnergy(ENERGY_PER_TICK);
            be.progress++;
            changed = true;

            if (be.progress >= PROCESS_TIME) {
                be.processItem();
                be.progress = 0;
            }
        } else if (be.progress > 0) {
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.setChanged();
        }
    }

    private boolean canProcess() {
        // Need energy
        if (energy.getEnergyStored() < ENERGY_PER_TICK) return false;
        // Need water
        if (fluidTank.getFluidAmount() < FLUID_PER_CRAFT) return false;
        // Need input item (thorium ingot)
        if (items.getStackInSlot(INPUT_SLOT).isEmpty()) return false;
        if (!items.getStackInSlot(INPUT_SLOT).is(ModTags.Items.INGOTS_THORIUM)) return false;
        // Need space for output
        var output = items.getStackInSlot(OUTPUT_SLOT);
        if (!output.isEmpty()) {
            if (!output.is(ModItems.TACHYON_SHARD.get())) return false;
            if (output.getCount() >= output.getMaxStackSize()) return false;
        }
        return true;
    }

    private void processItem() {
        // Consume input
        items.extractItem(INPUT_SLOT, 1, false);
        // Consume water
        fluidTank.drain(FLUID_PER_CRAFT, IFluidHandler.FluidAction.EXECUTE);
        // Produce output
        var existing = items.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, ModItems.TACHYON_SHARD.get().getDefaultInstance());
        } else {
            existing.grow(1);
        }
    }

    // --- Menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.accelerator_controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AcceleratorControllerMenu(containerId, playerInventory, items, dataAccess);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Fluid", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
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
        energy.setEnergy(tag.getInt("Energy"));
        fluidTank.readFromNBT(registries, tag.getCompound("Fluid"));
        progress = tag.getInt("Progress");
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
}
