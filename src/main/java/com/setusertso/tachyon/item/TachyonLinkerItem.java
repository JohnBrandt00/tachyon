package com.setusertso.tachyon.item;

import com.setusertso.tachyon.block.TachyonRelayBlock;
import com.setusertso.tachyon.block.entity.TachyonRelayBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TachyonLinkerItem extends Item {

    public TachyonLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide) {
            return state.getBlock() instanceof TachyonRelayBlock ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!(state.getBlock() instanceof TachyonRelayBlock)) {
            // Clicked a non-relay block — clear stored position
            if (hasStoredPos(stack)) {
                clearStoredPos(stack);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal("Selection cleared"));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // Clicked a relay
        if (!(level.getBlockEntity(pos) instanceof TachyonRelayBlockEntity relay)) {
            return InteractionResult.FAIL;
        }

        if (!hasStoredPos(stack)) {
            // First click — store this relay's position
            storePos(stack, pos);
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                        Component.literal("Relay selected at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
            }
            return InteractionResult.SUCCESS;
        }

        // Second click — try to link
        BlockPos storedPos = getStoredPos(stack);
        if (storedPos == null) {
            clearStoredPos(stack);
            return InteractionResult.FAIL;
        }

        // Validate: not self
        if (storedPos.equals(pos)) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(Component.literal("Cannot link a relay to itself!"));
            }
            return InteractionResult.FAIL;
        }

        // Validate: range
        if (storedPos.distSqr(pos) > 64 * 64) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(Component.literal("Relays are too far apart! (max 64 blocks)"));
            }
            return InteractionResult.FAIL;
        }

        // Validate: partner still exists
        if (!(level.getBlockEntity(storedPos) instanceof TachyonRelayBlockEntity partnerRelay)) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(Component.literal("First relay no longer exists!"));
            }
            clearStoredPos(stack);
            return InteractionResult.FAIL;
        }

        // Clear any existing links on both relays
        partnerRelay.notifyPartnerOfRemoval();
        relay.notifyPartnerOfRemoval();

        // Create the link
        partnerRelay.setLinkedPos(pos);
        relay.setLinkedPos(storedPos);

        clearStoredPos(stack);

        if (context.getPlayer() != null) {
            context.getPlayer().sendSystemMessage(Component.literal("Relays linked! ["
                    + storedPos.getX() + ", " + storedPos.getY() + ", " + storedPos.getZ() + "] <-> ["
                    + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (hasStoredPos(stack)) {
            BlockPos pos = getStoredPos(stack);
            if (pos != null) {
                tooltipComponents.add(Component.literal("Selected: [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
            }
        }
    }

    private static void storePos(ItemStack stack, BlockPos pos) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putInt("LinkX", pos.getX());
        tag.putInt("LinkY", pos.getY());
        tag.putInt("LinkZ", pos.getZ());
        tag.putBoolean("HasLink", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean hasStoredPos(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean("HasLink");
    }

    private static BlockPos getStoredPos(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        if (!tag.getBoolean("HasLink")) return null;
        return new BlockPos(tag.getInt("LinkX"), tag.getInt("LinkY"), tag.getInt("LinkZ"));
    }

    private static void clearStoredPos(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }
}
