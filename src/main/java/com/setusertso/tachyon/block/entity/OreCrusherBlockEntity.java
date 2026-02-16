package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.OreCrusherBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.OreCrusherMenu;

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import java.util.List;

public class OreCrusherBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    public static final int ENERGY_CAPACITY = 50_000;
    public static final int MAX_ENERGY_RECEIVE = 10_000;
    public static final int ENERGY_PER_TICK = 200;
    public static final int PROCESS_TIME = 80;

    private record CrusherRecipe(Item input, Item output, int count) {}

    private static final List<CrusherRecipe> RECIPES = List.of(
            new CrusherRecipe(ModItems.RAW_TITANIUM.get(), ModItems.CRUSHED_TITANIUM.get(), 2),
            new CrusherRecipe(ModItems.RAW_TUNGSTEN.get(), ModItems.CRUSHED_TUNGSTEN.get(), 2),
            new CrusherRecipe(ModItems.RAW_LITHIUM.get(), ModItems.CRUSHED_LITHIUM.get(), 2),
            new CrusherRecipe(ModItems.RAW_THORIUM.get(), ModItems.CRUSHED_THORIUM.get(), 2),
            new CrusherRecipe(Items.RAW_IRON, ModItems.CRUSHED_IRON.get(), 2),
            new CrusherRecipe(Items.RAW_GOLD, ModItems.CRUSHED_GOLD.get(), 2),
            new CrusherRecipe(Items.RAW_COPPER, ModItems.CRUSHED_COPPER.get(), 2),
            new CrusherRecipe(Items.COBBLESTONE, Items.SAND, 1),
            new CrusherRecipe(Items.GRAVEL, Items.FLINT, 1),
            new CrusherRecipe(Items.BLAZE_ROD, Items.BLAZE_POWDER, 4)
    );

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == INPUT_SLOT) return findRecipe(stack) != null;
            return false;
        }
    };

    private final IItemHandler topHandler = new RangedWrapper(items, INPUT_SLOT, INPUT_SLOT + 1);
    private final IItemHandler bottomHandler = new RangedWrapper(items, OUTPUT_SLOT, OUTPUT_SLOT + 1) {
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

    public OreCrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORE_CRUSHER.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public IItemHandler getSidedItemHandler(Direction side) {
        if (side == null) return items;
        return switch (side) {
            case DOWN -> bottomHandler;
            default -> topHandler;
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

    private static CrusherRecipe findRecipe(ItemStack input) {
        for (CrusherRecipe recipe : RECIPES) {
            if (input.is(recipe.input)) return recipe;
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OreCrusherBlockEntity be) {
        boolean wasActive = state.getValue(OreCrusherBlock.ACTIVE);
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
            level.setBlock(pos, state.setValue(OreCrusherBlock.ACTIVE, isProcessing), Block.UPDATE_ALL);
        }
    }

    private boolean canProcess() {
        int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * getEnergyMultiplier()));
        if (energy.getEnergyStored() < energyCost) return false;

        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return false;

        CrusherRecipe recipe = findRecipe(input);
        if (recipe == null) return false;

        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!output.is(recipe.output)) return false;
        return output.getCount() + recipe.count <= output.getMaxStackSize();
    }

    private void processItem() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        CrusherRecipe recipe = findRecipe(input);
        if (recipe == null) return;

        items.extractItem(INPUT_SLOT, 1, false);

        int outputCount = recipe.count;
        float outputChance = getOutputChance();
        if (outputChance > 0 && level != null && level.getRandom().nextFloat() < outputChance) {
            outputCount += 1;
        }

        ItemStack existing = items.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, new ItemStack(recipe.output, outputCount));
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
        return Component.translatable("menu.tachyon.ore_crusher");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OreCrusherMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
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
