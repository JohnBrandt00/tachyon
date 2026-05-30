package com.setusertso.tachyon.block.entity;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.menu.SingularityPortMenu;

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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SingularityPortBlockEntity extends BlockEntity implements MenuProvider {
    private BlockPos masterPos = null;
    private PortMode mode = PortMode.ITEM_INPUT;
    private int shieldPowerRate = 0; // RF/tick consumed for stability (0-10000)

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public SingularityPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_PORT.get(), pos, state);
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public PortMode getMode() {
        return mode;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getShieldPowerRate() {
        return shieldPowerRate;
    }

    public void setShieldPowerRate(int rate) {
        this.shieldPowerRate = Math.max(0, Math.min(100000, rate));
        setChanged();
    }

    public void cycleMode() {
        // Cycle: ITEM_INPUT -> ITEM_OUTPUT -> ENERGY_INPUT -> ENERGY_OUTPUT -> ITEM_INPUT
        this.mode = switch (this.mode) {
            case ITEM_INPUT -> PortMode.ITEM_OUTPUT;
            case ITEM_OUTPUT -> PortMode.ENERGY_INPUT;
            case ENERGY_INPUT -> PortMode.ENERGY_OUTPUT;
            case ENERGY_OUTPUT -> PortMode.ITEM_INPUT;
            default -> PortMode.ITEM_INPUT;
        };
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            BlockState newState = getBlockState().setValue(
                    com.setusertso.tachyon.block.SingularityPortBlock.MODE, mode);
            level.setBlock(worldPosition, newState, Block.UPDATE_ALL);

            // Notify controller to update its cached port mode for rendering
            if (masterPos != null && level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
                controller.updatePortMode(worldPosition, mode);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.getValue(com.setusertso.tachyon.block.SingularityPortBlock.MODE) != mode) {
                level.setBlock(worldPosition, state.setValue(
                        com.setusertso.tachyon.block.SingularityPortBlock.MODE, mode),
                        Block.UPDATE_CLIENTS);
            }
        }
    }

    // --- Server Tick: Transfer items between port slot and controller ---

    public static void serverTick(Level level, BlockPos pos, BlockState state, SingularityPortBlockEntity be) {
        if (be.masterPos == null) return;
        if (!(level.getBlockEntity(be.masterPos) instanceof SingularityControllerBlockEntity controller)) return;

        switch (be.mode) {
            case ITEM_OUTPUT -> {
                ItemStack portStack = be.items.getStackInSlot(0);
                if (portStack.isEmpty() || (portStack.is(ModItems.EXOTIC_MATTER.get())
                        && portStack.getCount() < portStack.getMaxStackSize())) {
                    ItemStack extracted = controller.getItemHandler().extractItem(
                            SingularityControllerBlockEntity.OUTPUT_SLOT, 1, false);
                    if (!extracted.isEmpty()) {
                        if (portStack.isEmpty()) {
                            be.items.setStackInSlot(0, extracted);
                        } else {
                            portStack.grow(extracted.getCount());
                        }
                    }
                }
            }
            default -> {
                // ITEM_INPUT, ENERGY_INPUT, ENERGY_OUTPUT: no item transfer
            }
        }
    }

    // --- Shield RF consumption: pull RF from adjacent blocks ---

    public int consumeShieldRfFromAdjacent() {
        if (mode != PortMode.ENERGY_INPUT || shieldPowerRate <= 0) return 0;
        if (level == null || level.isClientSide()) return 0;

        int totalConsumed = 0;
        int remaining = shieldPowerRate;

        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos adjPos = worldPosition.relative(dir);
            IEnergyStorage adjacent = level.getCapability(Capabilities.EnergyStorage.BLOCK, adjPos, dir.getOpposite());
            if (adjacent != null && adjacent.canExtract()) {
                int extracted = adjacent.extractEnergy(remaining, false);
                totalConsumed += extracted;
                remaining -= extracted;
            }
        }

        return totalConsumed;
    }

    // --- RF output: push RF from controller's buffer to adjacent blocks ---

    public void pushRfToAdjacent(CustomEnergyStorage source) {
        if (mode != PortMode.ENERGY_OUTPUT) return;
        if (level == null || level.isClientSide()) return;

        int available = source.getEnergyStored();
        if (available <= 0) return;

        for (Direction dir : Direction.values()) {
            if (available <= 0) break;
            BlockPos adjPos = worldPosition.relative(dir);
            IEnergyStorage adjacent = level.getCapability(Capabilities.EnergyStorage.BLOCK, adjPos, dir.getOpposite());
            if (adjacent != null && adjacent.canReceive()) {
                int maxPush = Math.min(available, source.getMaxEnergyStored() / 20); // up to 5% of buffer per tick per face
                int accepted = adjacent.receiveEnergy(maxPush, false);
                source.consumeEnergy(accepted);
                available -= accepted;
            }
        }
    }

    // --- Capability delegation (for pipes/automation) ---

    public IItemHandler getItemCapHandler() {
        if (masterPos == null || level == null) return null;
        if (level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
            return switch (mode) {
                case ITEM_OUTPUT -> new OutputOnlyItemHandler(controller.getItemHandler(), SingularityControllerBlockEntity.OUTPUT_SLOT);
                default -> null;
            };
        }
        return null;
    }

    public IEnergyStorage getEnergyHandler() {
        if (masterPos == null || level == null) return null;
        if (level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
            return switch (mode) {
                case ENERGY_OUTPUT -> controller.getRfBuffer();
                default -> null;
            };
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.singularity_port");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SingularityPortMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) {
            tag.putInt("MasterX", masterPos.getX());
            tag.putInt("MasterY", masterPos.getY());
            tag.putInt("MasterZ", masterPos.getZ());
        }
        tag.putInt("Mode", mode.ordinal());
        tag.put("PortItems", items.serializeNBT(registries));
        tag.putInt("ShieldPowerRate", shieldPowerRate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterX")) {
            masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        } else {
            masterPos = null;
        }
        mode = PortMode.fromOrdinal(tag.getInt("Mode"));
        if (tag.contains("PortItems")) {
            items.deserializeNBT(registries, tag.getCompound("PortItems"));
        }
        shieldPowerRate = tag.contains("ShieldPowerRate") ? Math.max(0, Math.min(100000, tag.getInt("ShieldPowerRate"))) : 0;
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
