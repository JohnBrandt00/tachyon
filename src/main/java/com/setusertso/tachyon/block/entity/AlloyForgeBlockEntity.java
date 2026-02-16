package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.AlloyForgeBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.AlloyForgeMenu;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import java.util.List;

public class AlloyForgeBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {
    public static final int INPUT_1 = 0;
    public static final int INPUT_2 = 1;
    public static final int OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int ENERGY_CAPACITY = 250_000;
    public static final int MAX_ENERGY_RECEIVE = 10_000;
    public static final int ENERGY_PER_TICK = 1000;
    public static final int PROCESS_TIME = 150;

    private record AlloyRecipe(Item input1, Item input2, Item output) {}

    private static final List<AlloyRecipe> RECIPES = List.of(
            new AlloyRecipe(ModItems.TITANIUM_INGOT.get(), ModItems.TUNGSTEN_INGOT.get(), ModItems.TACHYON_ALLOY_INGOT.get()),
            new AlloyRecipe(ModItems.LITHIUM_INGOT.get(), ModItems.THORIUM_INGOT.get(), ModItems.REACTOR_PLATING.get()),
            new AlloyRecipe(ModItems.TACHYON_ALLOY_INGOT.get(), ModItems.EXOTIC_MATTER.get(), ModItems.EXOTIC_SHARD.get()),
            new AlloyRecipe(Items.IRON_INGOT, ModItems.TACHYON_SHARD.get(), ModItems.ENERGY_CRYSTAL.get()),
            new AlloyRecipe(ModItems.TACHYON_ALLOY_INGOT.get(), ModItems.GRAVITON_CRYSTAL.get(), ModItems.QUANTUM_PROCESSOR.get())
    );

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_1, INPUT_2 -> true;
                case OUTPUT -> false;
                default -> false;
            };
        }
    };

    private final IItemHandler topHandler = new RangedWrapper(items, INPUT_1, INPUT_1 + 1);
    private final IItemHandler sideHandler = new RangedWrapper(items, INPUT_2, INPUT_2 + 1);
    private final IItemHandler bottomHandler = new RangedWrapper(items, OUTPUT, OUTPUT + 1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }
    };

    private final ItemStackHandler upgradeHandler = IUpgradeable.createUpgradeHandler(this::setChanged);
    private final CustomEnergyStorage energy = new CustomEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, 0);

    private int progress = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> PROCESS_TIME;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
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
            return 4;
        }
    };

    public AlloyForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_FORGE.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public IItemHandler getSidedItemHandler(Direction side) {
        if (side == null) return items;
        return switch (side) {
            case DOWN -> bottomHandler;
            case UP -> topHandler;
            default -> sideHandler;
        };
    }

    @Override
    public ItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    public CustomEnergyStorage getEnergyStorage() {
        return energy;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    private AlloyRecipe findRecipe() {
        ItemStack slot1 = items.getStackInSlot(INPUT_1);
        ItemStack slot2 = items.getStackInSlot(INPUT_2);
        if (slot1.isEmpty() || slot2.isEmpty()) return null;

        for (AlloyRecipe recipe : RECIPES) {
            if (slot1.is(recipe.input1) && slot2.is(recipe.input2)) return recipe;
            if (slot1.is(recipe.input2) && slot2.is(recipe.input1)) return recipe;
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlloyForgeBlockEntity be) {
        boolean wasActive = state.getValue(AlloyForgeBlock.ACTIVE);
        boolean isProcessing = false;

        if (be.canProcess()) {
            isProcessing = true;

            int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * be.getEnergyMultiplier()));
            be.energy.consumeEnergy(energyCost);

            float speedMult = be.getSpeedMultiplier();
            int progressIncrement = Math.max(1, Math.round(speedMult));
            be.progress += progressIncrement;

            if (be.progress >= PROCESS_TIME) {
                be.processItem();
                be.progress = 0;
            }

            be.setChanged();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.setChanged();
            }
        }

        if (wasActive != isProcessing) {
            level.setBlock(pos, state.setValue(AlloyForgeBlock.ACTIVE, isProcessing), Block.UPDATE_ALL);
        }
    }

    private boolean canProcess() {
        int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * getEnergyMultiplier()));
        if (energy.getEnergyStored() < energyCost) return false;

        AlloyRecipe recipe = findRecipe();
        if (recipe == null) return false;

        ItemStack output = items.getStackInSlot(OUTPUT);
        if (!output.isEmpty()) {
            if (!output.is(recipe.output)) return false;
            if (output.getCount() >= output.getMaxStackSize()) return false;
        }

        return true;
    }

    private void processItem() {
        AlloyRecipe recipe = findRecipe();
        if (recipe == null) return;

        items.extractItem(INPUT_1, 1, false);
        items.extractItem(INPUT_2, 1, false);

        int outputCount = 1;
        float outputChance = getOutputChance();
        if (outputChance > 0 && level != null && level.getRandom().nextFloat() < outputChance) {
            outputCount = 2;
        }

        ItemStack existing = items.getStackInSlot(OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(OUTPUT, new ItemStack(recipe.output, outputCount));
        } else {
            existing.grow(outputCount);
        }
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.alloy_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AlloyForgeMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.put("Upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("Upgrades")) {
            upgradeHandler.deserializeNBT(registries, tag.getCompound("Upgrades"));
        }
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
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
