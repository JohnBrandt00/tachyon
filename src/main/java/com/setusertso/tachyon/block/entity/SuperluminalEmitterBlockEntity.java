package com.setusertso.tachyon.block.entity;

import java.util.Set;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.SuperluminalEmitterBlock;

import net.minecraft.world.item.Item;
import com.setusertso.tachyon.block.TachyonLightGeneratorBlock;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.SuperluminalEmitterMenu;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SuperluminalEmitterBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {
    public static final int FUEL_SLOT = 0;
    public static final int SLOT_COUNT = 1;
    public static final int SHARD_BURN_TIME = 1600;
    private static final int BASE_SCAN_RADIUS = 16;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == FUEL_SLOT && stack.is(ModItems.TACHYON_SHARD.get());
        }
    };

    private final ItemStackHandler upgradeHandler = IUpgradeable.createUpgradeHandler(this::setChanged, getAllowedUpgrades());

    private int litTime = 0;
    private int litDuration = 0;
    private int scanTickCounter = 0;
    private boolean redstonePowered = false;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> litTime;
                case 1 -> litDuration;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public SuperluminalEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPERLUMINAL_EMITTER.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    @Override
    public ItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    @Override
    public Set<Item> getAllowedUpgrades() {
        return Set.of(ModItems.SPEED_UPGRADE.get(), ModItems.ENERGY_UPGRADE.get());
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public boolean isActive() {
        return litTime > 0 && !redstonePowered;
    }

    public int getScanRadius() {
        // Speed upgrade increases scan radius: 16 -> 20 -> 24 -> 28 -> 32
        return BASE_SCAN_RADIUS + 4 * countUpgrade(ModItems.SPEED_UPGRADE.get());
    }

    public void setRedstonePowered(boolean powered) {
        if (this.redstonePowered != powered) {
            boolean wasActive = isActive();
            this.redstonePowered = powered;
            boolean nowActive = isActive();
            if (wasActive && !nowActive) {
                deactivateAllGenerators();
            } else if (!wasActive && nowActive && level != null) {
                scanAndActivateGenerators();
            }
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SuperluminalEmitterBlockEntity be) {
        boolean wasActive = be.isActive();

        // 1. Decrement fuel timer
        if (be.litTime > 0) {
            be.litTime--;
        }

        // 2. Try consume new fuel (energy upgrade extends burn duration)
        if (be.litTime == 0 && !be.redstonePowered) {
            ItemStack fuel = be.items.getStackInSlot(FUEL_SLOT);
            if (!fuel.isEmpty() && fuel.is(ModItems.TACHYON_SHARD.get())) {
                int burnDuration = Math.round(SHARD_BURN_TIME / be.getEnergyMultiplier());
                be.litTime = burnDuration;
                be.litDuration = burnDuration;
                fuel.shrink(1);
                be.setChanged();
            }
        }

        // 3. Update block state if changed
        boolean nowActive = be.isActive();
        if (wasActive != nowActive) {
            level.setBlock(pos, state.setValue(SuperluminalEmitterBlock.ACTIVE, nowActive), Block.UPDATE_ALL);
            if (!nowActive) {
                be.deactivateAllGenerators();
                be.scanTickCounter = 0;
            }
            be.setChanged();
        }

        // 4. Periodic scan when active
        if (nowActive) {
            be.scanTickCounter++;
            if (be.scanTickCounter >= 20) {
                be.scanTickCounter = 0;
                be.scanAndActivateGenerators();
            }
        }
    }

    private void scanAndActivateGenerators() {
        if (level == null) return;
        long gameTime = level.getGameTime();
        BlockPos center = worldPosition;
        int radius = getScanRadius();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof TachyonLightGeneratorBlock) {
                        if (!checkState.getValue(TachyonLightGeneratorBlock.ACTIVE)) {
                            level.setBlock(checkPos, checkState.setValue(TachyonLightGeneratorBlock.ACTIVE, true), Block.UPDATE_ALL);
                        }
                        if (level.getBlockEntity(checkPos) instanceof TachyonLightGeneratorBlockEntity genBe) {
                            genBe.setLastPoweredTick(gameTime);
                        }
                    }
                }
            }
        }
    }

    public void deactivateAllGenerators() {
        if (level == null) return;
        BlockPos center = worldPosition;
        int radius = getScanRadius();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof TachyonLightGeneratorBlock
                            && checkState.getValue(TachyonLightGeneratorBlock.ACTIVE)) {
                        level.setBlock(checkPos, checkState.setValue(TachyonLightGeneratorBlock.ACTIVE, false), Block.UPDATE_ALL);
                    }
                }
            }
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
        return Component.translatable("menu.tachyon.superluminal_emitter");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SuperluminalEmitterMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.put("Upgrades", upgradeHandler.serializeNBT(registries));
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putBoolean("RedstonePowered", redstonePowered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("Upgrades")) {
            upgradeHandler.deserializeNBT(registries, tag.getCompound("Upgrades"));
        }
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        redstonePowered = tag.getBoolean("RedstonePowered");
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
