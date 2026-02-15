package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.SingularityCasingBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SingularityCasingBlock extends Block implements EntityBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty MELTDOWN = BooleanProperty.create("meltdown");

    public SingularityCasingBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(MELTDOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED, MELTDOWN);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FORMED) ? Shapes.empty() : super.getVisualShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FORMED) ? Shapes.empty() : super.getOcclusionShape(state, level, pos);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(FORMED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        if (!state.getValue(FORMED)) return RenderShape.MODEL;
        // When formed: invisible unless in meltdown (then reappear)
        return state.getValue(MELTDOWN) ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        // Unbreakable in survival when formed
        if (state.getValue(FORMED)) return 0.0f;
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SingularityCasingBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SingularityCasingBlockEntity be) {
            BlockPos masterPos = be.getMasterPos();
            if (masterPos != null) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    if (level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
                        sp.openMenu(controller, masterPos);
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
        // Structure is permanent — no neighbor validation needed
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Only trigger disassembly if NOT formed (structure is permanent when formed)
            if (!state.getValue(FORMED)) {
                if (level.getBlockEntity(pos) instanceof SingularityCasingBlockEntity be) {
                    BlockPos masterPos = be.getMasterPos();
                    if (masterPos != null && level.getBlockEntity(masterPos) instanceof SingularityControllerBlockEntity controller) {
                        controller.disassembleStructure();
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
