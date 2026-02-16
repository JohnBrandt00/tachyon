package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.PhotonCompressorBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.PhotonCompressorMenu;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

public class PhotonCompressorBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {
    public static final int INPUT_PHOTON = 0;
    public static final int INPUT_GLOWSTONE = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int ENERGY_CAPACITY = 500_000;
    public static final int MAX_ENERGY_RECEIVE = 10_000;
    public static final int ENERGY_PER_TICK = 2_000;
    public static final int PROCESS_TIME = 100;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_PHOTON -> stack.is(ModItems.EXCITED_PHOTON.get());
                case INPUT_GLOWSTONE -> stack.is(Items.GLOWSTONE_DUST);
                case OUTPUT_SLOT -> false;
                default -> false;
            };
        }
    };

    private final IItemHandler topHandler = new RangedWrapper(items, INPUT_PHOTON, INPUT_PHOTON + 1);
    private final IItemHandler sideHandler = new RangedWrapper(items, INPUT_GLOWSTONE, INPUT_GLOWSTONE + 1);
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

    public PhotonCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PHOTON_COMPRESSOR.get(), pos, state);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, PhotonCompressorBlockEntity be) {
        boolean wasActive = state.getValue(PhotonCompressorBlock.ACTIVE);
        boolean isProcessing = false;

        if (be.canProcess()) {
            isProcessing = true;

            // Consume energy
            int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * be.getEnergyMultiplier()));
            be.energy.consumeEnergy(energyCost);

            // Increment progress (with speed upgrade)
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
            level.setBlock(pos, state.setValue(PhotonCompressorBlock.ACTIVE, isProcessing), Block.UPDATE_ALL);
        }
    }

    private boolean canProcess() {
        // Need energy
        int energyCost = Math.max(1, Math.round(ENERGY_PER_TICK * getEnergyMultiplier()));
        if (energy.getEnergyStored() < energyCost) return false;

        // Need Excited Photon
        ItemStack photon = items.getStackInSlot(INPUT_PHOTON);
        if (photon.isEmpty() || !photon.is(ModItems.EXCITED_PHOTON.get())) return false;

        // Need Glowstone Dust
        ItemStack glowstone = items.getStackInSlot(INPUT_GLOWSTONE);
        if (glowstone.isEmpty() || !glowstone.is(Items.GLOWSTONE_DUST)) return false;

        // Need output space
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (!output.isEmpty()) {
            if (!output.is(ModItems.CONDENSED_LIGHT.get())) return false;
            if (output.getCount() >= output.getMaxStackSize()) return false;
        }

        return true;
    }

    private void processItem() {
        // Consume inputs
        items.extractItem(INPUT_PHOTON, 1, false);
        items.extractItem(INPUT_GLOWSTONE, 1, false);

        // Determine output count (output upgrade chance to double)
        int outputCount = 1;
        float outputChance = getOutputChance();
        if (outputChance > 0 && level != null && level.getRandom().nextFloat() < outputChance) {
            outputCount = 2;
        }

        // Produce output
        ItemStack existing = items.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            ItemStack result = new ItemStack(ModItems.CONDENSED_LIGHT.get(), outputCount);
            items.setStackInSlot(OUTPUT_SLOT, result);
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
        return Component.translatable("menu.tachyon.photon_compressor");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PhotonCompressorMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
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
