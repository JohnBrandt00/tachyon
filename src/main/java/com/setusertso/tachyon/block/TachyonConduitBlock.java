package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.TachyonConduitBlockEntity;
import com.setusertso.tachyon.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

import org.jetbrains.annotations.Nullable;

public class TachyonConduitBlock extends Block implements EntityBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape[] ARMS = {
            Block.box(5, 0, 5, 11, 5, 11),   // DOWN  (0)
            Block.box(5, 11, 5, 11, 16, 11),  // UP    (1)
            Block.box(5, 5, 0, 11, 11, 5),    // NORTH (2)
            Block.box(5, 5, 11, 11, 11, 16),  // SOUTH (3)
            Block.box(0, 5, 5, 5, 11, 11),    // WEST  (4)
            Block.box(11, 5, 5, 16, 11, 11),  // EAST  (5)
    };

    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[64];

    static {
        for (int i = 0; i < 64; i++) {
            VoxelShape shape = CORE;
            for (int dir = 0; dir < 6; dir++) {
                if ((i & (1 << dir)) != 0) {
                    shape = Shapes.or(shape, ARMS[dir]);
                }
            }
            SHAPE_CACHE[i] = shape;
        }
    }

    public TachyonConduitBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int index = 0;
        if (state.getValue(DOWN)) index |= 1;
        if (state.getValue(UP)) index |= 2;
        if (state.getValue(NORTH)) index |= 4;
        if (state.getValue(SOUTH)) index |= 8;
        if (state.getValue(WEST)) index |= 16;
        if (state.getValue(EAST)) index |= 32;
        return SHAPE_CACHE[index];
    }

    private boolean shouldConnect(Level level, BlockPos pos, Direction dir) {
        BlockPos neighbor = pos.relative(dir);
        BlockState neighborState = level.getBlockState(neighbor);
        if (neighborState.getBlock() instanceof TachyonConduitBlock) return true;
        if (neighborState.getBlock() instanceof TachyonRelayBlock) return true;
        Direction opposite = dir.getOpposite();
        if (level.getCapability(Capabilities.EnergyStorage.BLOCK, neighbor, opposite) != null) return true;
        if (level.getCapability(Capabilities.ItemHandler.BLOCK, neighbor, opposite) != null) return true;
        if (level.getCapability(Capabilities.FluidHandler.BLOCK, neighbor, opposite) != null) return true;
        return false;
    }

    private BlockState updateConnections(Level level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, shouldConnect(level, pos, Direction.NORTH))
                .setValue(SOUTH, shouldConnect(level, pos, Direction.SOUTH))
                .setValue(EAST, shouldConnect(level, pos, Direction.EAST))
                .setValue(WEST, shouldConnect(level, pos, Direction.WEST))
                .setValue(UP, shouldConnect(level, pos, Direction.UP))
                .setValue(DOWN, shouldConnect(level, pos, Direction.DOWN));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            BlockState updated = updateConnections(level, pos, state);
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            BlockState updated = updateConnections(level, pos, state);
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor levelAccessor, BlockPos pos, BlockPos neighborPos) {
        if (levelAccessor instanceof Level level && !level.isClientSide) {
            boolean connected = shouldConnect(level, pos, direction);
            BooleanProperty prop = getPropertyForDirection(direction);
            if (prop != null && state.getValue(prop) != connected) {
                return state.setValue(prop, connected);
            }
        }
        return state;
    }

    private static BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TachyonConduitBlockEntity conduit) {
                conduit.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TachyonConduitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.TACHYON_CONDUIT.get()
                ? (lvl, pos, st, be) -> TachyonConduitBlockEntity.serverTick(lvl, pos, st, (TachyonConduitBlockEntity) be)
                : null;
    }
}
