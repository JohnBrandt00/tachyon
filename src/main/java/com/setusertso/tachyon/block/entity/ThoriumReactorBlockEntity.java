package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.ThoriumReactorBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.ThoriumReactorMenu;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ThoriumReactorBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {
    public static final int FUEL_SLOT = 0;
    public static final int SLOT_COUNT = 1;
    public static final int BURN_TIME_PER_INGOT = 6000; // 5 minutes
    public static final int RF_PER_TICK = 100;
    public static final int MAX_ENERGY = 100_000;
    public static final int MAX_EXTRACT = 100;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == FUEL_SLOT && stack.is(ModItems.THORIUM_INGOT.get());
        }
    };

    private final ItemStackHandler upgradeHandler = IUpgradeable.createUpgradeHandler(this::setChanged);

    private final CustomEnergyStorage energy = new CustomEnergyStorage(MAX_ENERGY, 0, MAX_EXTRACT);

    private int burnTime = 0;
    private int maxBurnTime = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public ThoriumReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THORIUM_REACTOR.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public CustomEnergyStorage getEnergy() {
        return energy;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    @Override
    public ItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThoriumReactorBlockEntity be) {
        boolean wasBurning = be.burnTime > 0;

        // 1. Generate RF while burning (speed upgrade increases RF/t)
        if (be.burnTime > 0) {
            be.burnTime--;
            if (be.energy.getEnergyStored() < be.energy.getMaxEnergyStored()) {
                int rfPerTick = Math.round(RF_PER_TICK * be.getSpeedMultiplier());
                be.energy.addEnergy(rfPerTick);
            }
            be.setChanged();
        }

        // 2. Try consume new fuel if not burning and buffer not full
        // Energy upgrade extends burn duration
        if (be.burnTime == 0 && be.energy.getEnergyStored() < be.energy.getMaxEnergyStored()) {
            ItemStack fuel = be.items.getStackInSlot(FUEL_SLOT);
            if (!fuel.isEmpty() && fuel.is(ModItems.THORIUM_INGOT.get())) {
                int burnDuration = Math.round(BURN_TIME_PER_INGOT / be.getEnergyMultiplier());
                be.burnTime = burnDuration;
                be.maxBurnTime = burnDuration;
                fuel.shrink(1);
                be.setChanged();
            }
        }

        // 3. Push energy to neighbors
        if (be.energy.getEnergyStored() > 0) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor != null) {
                    var cap = level.getCapability(
                            net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                            neighborPos, dir.getOpposite());
                    if (cap != null && cap.canReceive()) {
                        int pushed = cap.receiveEnergy(
                                Math.min(MAX_EXTRACT, be.energy.getEnergyStored()), false);
                        if (pushed > 0) {
                            be.energy.consumeEnergy(pushed);
                            be.setChanged();
                        }
                    }
                    if (be.energy.getEnergyStored() <= 0) break;
                }
            }
        }

        // 4. Update blockstate if burning status changed
        boolean nowBurning = be.burnTime > 0;
        if (wasBurning != nowBurning) {
            level.setBlock(pos, state.setValue(ThoriumReactorBlock.BURNING, nowBurning), Block.UPDATE_ALL);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.thorium_reactor");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ThoriumReactorMenu(containerId, playerInventory, this, upgradeHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.put("Upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("Upgrades")) {
            upgradeHandler.deserializeNBT(registries, tag.getCompound("Upgrades"));
        }
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        energy.setEnergy(tag.getInt("Energy"));
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
