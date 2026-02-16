package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PhotonicInjectorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CONSUME_INTERVAL = 2000; // ticks per condensed light consumed

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.CONDENSED_LIGHT.get()) || stack.is(ModItems.ANTIMATTER_NEUTRALIZER.get());
        }
    };

    private int burnTime = 0;
    private boolean active = false;
    private int injectionRate = 100; // hundredths: 100 = 1.00/t, range 1-1000 (0.01 to 10.00)

    public PhotonicInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PHOTONIC_INJECTOR.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isActive() {
        return active;
    }

    public int getInjectionRate() {
        return injectionRate;
    }

    public void setInjectionRate(int rate) {
        this.injectionRate = Math.max(1, Math.min(1000, rate));
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PhotonicInjectorBlockEntity be) {
        boolean wasActive = be.active;

        if (be.burnTime > 0) {
            be.burnTime -= be.injectionRate;
            if (be.burnTime < 0) be.burnTime = 0;
            be.active = true;
        } else {
            ItemStack fuel = be.items.getStackInSlot(0);
            if (!fuel.isEmpty()) {
                if (fuel.is(ModItems.ANTIMATTER_NEUTRALIZER.get())) {
                    // Consume neutralizer and neutralize nearby controller
                    be.items.extractItem(0, 1, false);
                    be.neutralizeNearbyController(level, pos, state);
                    be.active = false;
                } else if (fuel.is(ModItems.CONDENSED_LIGHT.get())) {
                    be.items.extractItem(0, 1, false);
                    be.burnTime = CONSUME_INTERVAL;
                    be.active = true;
                } else {
                    be.active = false;
                }
            } else {
                be.active = false;
            }
        }

        if (wasActive != be.active) {
            level.setBlock(pos, state.setValue(PhotonicInjectorBlock.ACTIVE, be.active), Block.UPDATE_ALL);
            be.setChanged();
            be.syncToClient();
        }
    }

    /**
     * Search along the injector's facing direction to find a structure block,
     * then get its master controller and call neutralize().
     */
    private void neutralizeNearbyController(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(PhotonicInjectorBlock.FACING);

        for (int dist = 1; dist <= 30; dist++) {
            BlockPos checkPos = pos.relative(facing, dist);
            BlockEntity be = level.getBlockEntity(checkPos);

            if (be instanceof SingularityControllerBlockEntity controller) {
                controller.neutralize();
                return;
            } else if (be instanceof SingularityCasingBlockEntity casing) {
                BlockPos masterPos = casing.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
                    controller.neutralize();
                    return;
                }
            } else if (be instanceof SingularityPortBlockEntity port) {
                BlockPos masterPos = port.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
                    controller.neutralize();
                    return;
                }
            } else if (be instanceof ExoticMatterCoreBlockEntity core) {
                BlockPos masterPos = core.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
                    controller.neutralize();
                    return;
                }
            }

            BlockState checkState = level.getBlockState(checkPos);
            if (!checkState.isAir() && be == null) {
                break;
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.photonic_injector");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PhotonicInjectorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("BurnTime", burnTime);
        tag.putBoolean("Active", active);
        tag.putInt("InjectionRate", injectionRate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        burnTime = tag.getInt("BurnTime");
        active = tag.getBoolean("Active");
        injectionRate = tag.contains("InjectionRate") ? Math.max(1, Math.min(1000, tag.getInt("InjectionRate"))) : 100;
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
