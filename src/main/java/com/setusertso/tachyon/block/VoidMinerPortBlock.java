package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.VoidMinerPortBlockEntity;
import com.setusertso.tachyon.block.entity.VoidMinerControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VoidMinerPortBlock extends Block implements EntityBlock {
    public static final EnumProperty<VoidMinerPortMode> MODE = EnumProperty.create("mode", VoidMinerPortMode.class);

    public VoidMinerPortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MODE, VoidMinerPortMode.ITEM_OUTPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidMinerPortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof VoidMinerPortBlockEntity portBE) {
                VoidMinerPortBlockEntity.serverTick(lvl, pos, st, portBE);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof VoidMinerPortBlockEntity portBE) {
            BlockPos masterPos = portBE.getMasterPos();

            // Sneak-click opens controller GUI when formed
            if (player.isShiftKeyDown() && masterPos != null) {
                if (level.getBlockEntity(masterPos) instanceof VoidMinerControllerBlockEntity controller) {
                    player.openMenu(controller, masterPos);
                    return InteractionResult.SUCCESS;
                }
                // Master is gone — clear stale reference
                portBE.setMasterPos(null);
            }

            // Normal click always cycles mode
            VoidMinerPortMode currentMode = state.getValue(MODE);
            VoidMinerPortMode nextMode = currentMode.next();
            level.setBlock(pos, state.setValue(MODE, nextMode), 3);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Port mode: " + nextMode.getSerializedName()), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof VoidMinerPortBlockEntity portBE) {
                BlockPos masterPos = portBE.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof VoidMinerControllerBlockEntity controller) {
                    // Disassemble structure
                    controller.disassembleStructure();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof VoidMinerPortBlockEntity portBE) {
            BlockPos masterPos = portBE.getMasterPos();
            if (masterPos != null && level.getBlockEntity(masterPos) instanceof VoidMinerControllerBlockEntity controller) {
                // Notify controller of neighbor change
                controller.onNeighborChanged(pos);
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    public enum VoidMinerPortMode implements StringRepresentable {
        ITEM_OUTPUT("item_output"),
        ENERGY_INPUT("energy_input"),
        CATALYST_INPUT("catalyst_input");

        private final String name;

        VoidMinerPortMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public VoidMinerPortMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        public static VoidMinerPortMode fromOrdinal(int ordinal) {
            VoidMinerPortMode[] modes = values();
            if (ordinal < 0 || ordinal >= modes.length) {
                return ITEM_OUTPUT;
            }
            return modes[ordinal];
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
