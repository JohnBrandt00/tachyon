package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.CondenserControllerBlockEntity;
import com.setusertso.tachyon.block.entity.CondenserPortBlockEntity;
import com.setusertso.tachyon.block.entity.CondenserPortMode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
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

public class CondenserPortBlock extends Block implements EntityBlock {

    public static final EnumProperty<CondenserPortMode> MODE = EnumProperty.create("mode", CondenserPortMode.class);

    public CondenserPortBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(MODE, CondenserPortMode.ITEM_INPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CondenserPortBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (lvl, pos, st, be) -> CondenserPortBlockEntity.serverTick(lvl, pos, st, (CondenserPortBlockEntity) be);
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CondenserPortBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) {
                if (!level.isClientSide()) {
                    if (level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
                        // Redirect to controller GUI
                        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                            sp.openMenu(controller, masterPos);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof CondenserPortBlockEntity be) {
                BlockPos masterPos = be.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
                    controller.onNeighborChanged(pos);
                }
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CondenserPortBlockEntity be) {
                // Drop inventory
                for (int i = 0; i < be.getItems().getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                            be.getItems().getStackInSlot(i));
                }
                // Disassemble multiblock
                BlockPos masterPos = be.getMasterPos();
                if (masterPos != null && level.getBlockEntity(masterPos) instanceof CondenserControllerBlockEntity controller) {
                    controller.disassembleStructure();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
