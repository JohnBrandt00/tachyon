package com.setusertso.tachyon.block.entity;

import java.util.Set;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.SolarCollectorBlock;

import net.minecraft.world.item.Item;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.SolarCollectorMenu;

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

public class SolarCollectorBlockEntity extends BlockEntity implements MenuProvider, IUpgradeable {
    public static final int OUTPUT_SLOT = 0;
    public static final int SLOT_COUNT = 1;
    public static final int MAX_BUFFER = 64;
    private static final float BASE_PRODUCTION_RATE = 1.0f / 200.0f; // 1 photon per 200 ticks at peak

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false; // Output only
        }

        @Override
        public int getSlotLimit(int slot) {
            return MAX_BUFFER;
        }
    };

    private final ItemStackHandler upgradeHandler = IUpgradeable.createUpgradeHandler(this::setChanged, getAllowedUpgrades());

    private float productionAccumulator = 0.0f;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) (productionAccumulator * 200); // scaled for display
                case 1 -> getBlockState().getValue(SolarCollectorBlock.ACTIVE) ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) productionAccumulator = value / 200.0f;
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public SolarCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_COLLECTOR.get(), pos, state);
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
        return Set.of(ModItems.SPEED_UPGRADE.get());
    }

    @Override
    public float getSpeedMultiplier() {
        // Steeper curve for solar collector: 32 upgrades = 17x speed
        return 1.0f + 0.5f * countUpgrade(ModItems.SPEED_UPGRADE.get());
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarCollectorBlockEntity be) {
        boolean wasActive = state.getValue(SolarCollectorBlock.ACTIVE);
        boolean canProduce = false;

        // Check sky access
        if (level.canSeeSky(pos.above())) {
            long dayTime = level.getDayTime() % 24000;

            // Daytime is 0-12000, night is 12000-24000
            if (dayTime < 12000) {
                // Weather multiplier
                float weatherMult;
                if (level.isThundering()) {
                    weatherMult = 0.0f;
                } else if (level.isRaining()) {
                    weatherMult = 0.3f;
                } else {
                    weatherMult = 1.0f;
                }

                if (weatherMult > 0) {
                    // Time-of-day sine curve (peaks at noon = 6000)
                    float dayProgress = dayTime / 12000.0f;
                    float timeMult = (float) Math.sin(Math.PI * dayProgress);

                    // Speed upgrade increases production rate
                    float speedMult = be.getSpeedMultiplier();

                    float production = BASE_PRODUCTION_RATE * weatherMult * timeMult * speedMult;

                    // Check if output buffer has space
                    ItemStack existing = be.items.getStackInSlot(OUTPUT_SLOT);
                    if (existing.isEmpty() || (existing.is(ModItems.RAW_PHOTON.get()) && existing.getCount() < MAX_BUFFER)) {
                        be.productionAccumulator += production;
                        canProduce = true;

                        // Produce items when accumulator reaches 1
                        while (be.productionAccumulator >= 1.0f) {
                            be.productionAccumulator -= 1.0f;
                            ItemStack current = be.items.getStackInSlot(OUTPUT_SLOT);
                            if (current.isEmpty()) {
                                be.items.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.RAW_PHOTON.get(), 1));
                            } else if (current.getCount() < MAX_BUFFER) {
                                current.grow(1);
                            } else {
                                be.productionAccumulator = 0; // Buffer full
                                break;
                            }
                        }
                        be.setChanged();
                    }
                }
            }
        }

        // Update block state
        if (wasActive != canProduce) {
            level.setBlock(pos, state.setValue(SolarCollectorBlock.ACTIVE, canProduce), Block.UPDATE_ALL);
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
        return Component.translatable("menu.tachyon.solar_collector");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SolarCollectorMenu(containerId, playerInventory, items, upgradeHandler, dataAccess);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.put("Upgrades", upgradeHandler.serializeNBT(registries));
        tag.putFloat("ProductionAccumulator", productionAccumulator);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("Upgrades")) {
            upgradeHandler.deserializeNBT(registries, tag.getCompound("Upgrades"));
        }
        productionAccumulator = tag.getFloat("ProductionAccumulator");
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
