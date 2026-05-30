package com.setusertso.tachyon.item;

import com.setusertso.tachyon.block.TachyonConduitBlock;
import com.setusertso.tachyon.block.entity.AcceleratorPortBlockEntity;
import com.setusertso.tachyon.block.entity.CondenserPortBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityPortBlockEntity;
import com.setusertso.tachyon.block.entity.TachyonConduitBlockEntity;
import com.setusertso.tachyon.block.entity.TachyonRelayBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.setusertso.tachyon.block.entity.TachyonConduitBlockEntity.*;

public class TachyonWrenchItem extends Item {

    private static final String[] TYPE_NAMES = {"Energy", "Item", "Fluid"};
    private static final String[] TYPE_COLORS = {"§e", "§a", "§b"};

    public TachyonWrenchItem(Properties properties) {
        super(properties);
    }

    /** Get the wrench's active conduit resource type (0=energy, 1=item, 2=fluid) */
    private int getWrenchMode(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            return data.copyTag().getInt("WrenchMode");
        }
        return 0; // default: energy
    }

    /** Set the wrench's active conduit resource type */
    private void setWrenchMode(ItemStack stack, int mode) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        tag.putInt("WrenchMode", mode);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Sneak + right-click air: cycle wrench mode */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            int mode = (getWrenchMode(stack) + 1) % 3;
            setWrenchMode(stack, mode);
            player.sendSystemMessage(Component.literal(
                    "§7Wrench mode: " + TYPE_COLORS[mode] + TYPE_NAMES[mode]));
            level.playSound(null, player.blockPosition(), SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.PLAYERS, 0.6f, 1.4f);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        var blockEntity = level.getBlockEntity(pos);

        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            // Shift + right-click on conduit/relay: cycle mode for the wrench's active type
            if (blockEntity instanceof TachyonConduitBlockEntity conduit) {
                Direction face = getConduitSideFromHit(context, state);
                int type = getWrenchMode(stack);
                conduit.cycleSideConfig(face, type);
                var newMode = conduit.getSideConfig(face, type);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal(
                            "§d" + face.getName() + " " + TYPE_COLORS[type] + TYPE_NAMES[type] + "§7: " + newMode.displayName()));
                }
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.SUCCESS;
            } else if (blockEntity instanceof TachyonRelayBlockEntity relay) {
                Direction face = context.getClickedFace();
                int type = getWrenchMode(stack);
                relay.cycleSideConfig(face, type);
                var newMode = relay.getSideConfig(face, type);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal(
                            "§d" + face.getName() + " " + TYPE_COLORS[type] + TYPE_NAMES[type] + "§7: " + newMode.displayName()));
                }
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.SUCCESS;
            }

            // Check if a conduit/relay is behind the clicked face (for configuring through neighbor blocks)
            Direction clickedFace = context.getClickedFace();
            BlockPos behindPos = pos.relative(clickedFace);
            var behindBe = level.getBlockEntity(behindPos);
            if (behindBe instanceof TachyonConduitBlockEntity conduitBehind) {
                Direction sideToConfig = clickedFace.getOpposite();
                int type = getWrenchMode(stack);
                conduitBehind.cycleSideConfig(sideToConfig, type);
                var newMode = conduitBehind.getSideConfig(sideToConfig, type);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal(
                            "§d" + sideToConfig.getName() + " " + TYPE_COLORS[type] + TYPE_NAMES[type] + "§7: " + newMode.displayName()));
                }
                level.playSound(null, behindPos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.SUCCESS;
            } else if (behindBe instanceof TachyonRelayBlockEntity relayBehind) {
                Direction sideToConfig = clickedFace.getOpposite();
                int type = getWrenchMode(stack);
                relayBehind.cycleSideConfig(sideToConfig, type);
                var newMode = relayBehind.getSideConfig(sideToConfig, type);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal(
                            "§d" + sideToConfig.getName() + " " + TYPE_COLORS[type] + TYPE_NAMES[type] + "§7: " + newMode.displayName()));
                }
                level.playSound(null, behindPos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.SUCCESS;
            }

            // Shift + right-click other block: dismantle
            String blockName = state.getBlock().getClass().getSimpleName();
            if (blockName.contains("Controller") || blockName.contains("Accelerator")) {
                return InteractionResult.FAIL;
            }

            Block.dropResources(state, level, pos, level.getBlockEntity(pos));
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // Normal right-click on ports
        if (blockEntity instanceof AcceleratorPortBlockEntity port) {
            port.cycleMode();
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                        Component.literal("Mode: " + port.getMode().getSerializedName()));
            }
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof SingularityPortBlockEntity port) {
            port.cycleMode();
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                        Component.literal("Mode: " + port.getMode().getSerializedName()));
            }
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof CondenserPortBlockEntity port) {
            port.cycleMode();
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                        Component.literal("Mode: " + port.getMode().getSerializedName()));
            }
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof TachyonConduitBlockEntity conduit) {
            // Normal click: show all configs for the detected side
            Direction face = getConduitSideFromHit(context, state);
            conduit.markWrenchInteraction();
            sendConduitStatus(conduit, face, stack, context);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof TachyonRelayBlockEntity relay) {
            Direction face = context.getClickedFace();
            sendRelayStatus(relay, face, stack, context);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // Check if a conduit/relay is behind the clicked face (for viewing through neighbor blocks)
        {
            Direction clickedFace = context.getClickedFace();
            BlockPos behindPos = pos.relative(clickedFace);
            var behindBe = level.getBlockEntity(behindPos);
            if (behindBe instanceof TachyonConduitBlockEntity conduitBehind) {
                Direction sideToShow = clickedFace.getOpposite();
                conduitBehind.markWrenchInteraction();
                sendConduitStatus(conduitBehind, sideToShow, stack, context);
                level.playSound(null, behindPos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.SUCCESS;
            } else if (behindBe instanceof TachyonRelayBlockEntity relayBehind) {
                Direction sideToShow = clickedFace.getOpposite();
                sendRelayStatus(relayBehind, sideToShow, stack, context);
                level.playSound(null, behindPos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.SUCCESS;
            }
        }

        // Normal right-click: rotate
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
            Direction current = state.getValue(facingProp);
            Direction rotated = current.getClockWise();
            level.setBlock(pos, state.setValue(facingProp, rotated), 3);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.0f);
            return InteractionResult.SUCCESS;
        } else if (state.hasProperty(DirectionalBlock.FACING)) {
            DirectionProperty facingProp = DirectionalBlock.FACING;
            Direction current = state.getValue(facingProp);
            Direction[] order = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
            int idx = 0;
            for (int i = 0; i < order.length; i++) {
                if (order[i] == current) { idx = i; break; }
            }
            Direction next = order[(idx + 1) % order.length];
            level.setBlock(pos, state.setValue(facingProp, next), 3);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.0f);
            return InteractionResult.SUCCESS;
        } else {
            BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);
            if (rotated != state) {
                level.setBlock(pos, rotated, 3);
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int mode = getWrenchMode(stack);
        tooltipComponents.add(Component.literal("§7Mode: " + TYPE_COLORS[mode] + TYPE_NAMES[mode]));
        tooltipComponents.add(Component.literal("§8Sneak+right-click air to change mode"));
    }

    /**
     * Determines which conduit side the player clicked based on the hit position.
     * When clicking on a face, checks whether the click is over an arm or the center core.
     * The core spans ~0.3125 to ~0.6875 on each axis (pixels 5-11 of 16).
     * If the click falls outside the core on one of the non-face axes, it's over an arm.
     */
    private Direction getConduitSideFromHit(UseOnContext context, BlockState state) {
        Vec3 hitPos = context.getClickLocation();
        BlockPos blockPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // Position relative to block, 0-1 range
        double lx = hitPos.x - blockPos.getX();
        double ly = hitPos.y - blockPos.getY();
        double lz = hitPos.z - blockPos.getZ();

        if (!(state.getBlock() instanceof TachyonConduitBlock)) {
            return clickedFace;
        }

        // Core bounds: pixels 5-11 of 16 = 0.3125 to 0.6875
        double coreMin = 5.0 / 16.0;
        double coreMax = 11.0 / 16.0;

        // Determine which axes are "across the face" (the two axes perpendicular to the clicked face)
        // If the click position on those axes is outside the core, we're over an arm
        Direction armDirection = null;
        Direction.Axis faceAxis = clickedFace.getAxis();

        // Check the two non-face axes for being outside the core
        if (faceAxis != Direction.Axis.X) {
            if (lx < coreMin && isConnected(state, Direction.WEST)) armDirection = Direction.WEST;
            else if (lx > coreMax && isConnected(state, Direction.EAST)) armDirection = Direction.EAST;
        }
        if (faceAxis != Direction.Axis.Y) {
            if (ly < coreMin && isConnected(state, Direction.DOWN)) {
                if (armDirection == null) armDirection = Direction.DOWN;
            } else if (ly > coreMax && isConnected(state, Direction.UP)) {
                if (armDirection == null) armDirection = Direction.UP;
            }
        }
        if (faceAxis != Direction.Axis.Z) {
            if (lz < coreMin && isConnected(state, Direction.NORTH)) {
                if (armDirection == null) armDirection = Direction.NORTH;
            } else if (lz > coreMax && isConnected(state, Direction.SOUTH)) {
                if (armDirection == null) armDirection = Direction.SOUTH;
            }
        }

        if (armDirection != null) {
            return armDirection;
        }

        // Click is over the core area — use the clicked face if it's connected
        if (isConnected(state, clickedFace)) {
            return clickedFace;
        }

        // Fallback: find the nearest connected side
        double dx = lx - 0.5;
        double dy = ly - 0.5;
        double dz = lz - 0.5;
        Direction nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            if (!isConnected(state, dir)) continue;
            double dist = Math.abs(dx - dir.getStepX() * 0.5)
                    + Math.abs(dy - dir.getStepY() * 0.5)
                    + Math.abs(dz - dir.getStepZ() * 0.5);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = dir;
            }
        }
        return nearest != null ? nearest : clickedFace;
    }

    private static boolean isConnected(BlockState state, Direction dir) {
        var prop = switch (dir) {
            case NORTH -> TachyonConduitBlock.NORTH;
            case SOUTH -> TachyonConduitBlock.SOUTH;
            case EAST -> TachyonConduitBlock.EAST;
            case WEST -> TachyonConduitBlock.WEST;
            case UP -> TachyonConduitBlock.UP;
            case DOWN -> TachyonConduitBlock.DOWN;
        };
        return state.getValue(prop);
    }

    private void sendConduitStatus(TachyonConduitBlockEntity conduit, Direction face, ItemStack stack, UseOnContext context) {
        if (context.getPlayer() == null) return;
        int mode = getWrenchMode(stack);
        var player = context.getPlayer();
        player.sendSystemMessage(Component.literal(
                "§d" + face.getName() + " §7— " +
                "§eEnergy§7: " + conduit.getSideConfig(face, TYPE_ENERGY).displayName() + "  " +
                "§aItem§7: " + conduit.getSideConfig(face, TYPE_ITEM).displayName() + "  " +
                "§bFluid§7: " + conduit.getSideConfig(face, TYPE_FLUID).displayName()
        ));
        player.sendSystemMessage(Component.literal(
                "§8Wrench mode: " + TYPE_COLORS[mode] + TYPE_NAMES[mode] + " §8| Sneak+click to cycle"));
    }

    private void sendRelayStatus(TachyonRelayBlockEntity relay, Direction face, ItemStack stack, UseOnContext context) {
        if (context.getPlayer() == null) return;
        int mode = getWrenchMode(stack);
        var player = context.getPlayer();
        player.sendSystemMessage(Component.literal(
                "§d" + face.getName() + " §7— " +
                "§eEnergy§7: " + relay.getSideConfig(face, TYPE_ENERGY).displayName() + "  " +
                "§aItem§7: " + relay.getSideConfig(face, TYPE_ITEM).displayName() + "  " +
                "§bFluid§7: " + relay.getSideConfig(face, TYPE_FLUID).displayName()
        ));
        player.sendSystemMessage(Component.literal(
                "§8Wrench mode: " + TYPE_COLORS[mode] + TYPE_NAMES[mode] + " §8| Sneak+click to cycle"));
    }
}
