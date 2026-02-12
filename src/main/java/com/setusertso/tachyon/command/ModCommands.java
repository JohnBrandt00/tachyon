package com.setusertso.tachyon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorPattern;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tachyon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("build")
                        .executes(ModCommands::buildAccelerator))
                .then(Commands.literal("clear")
                        .executes(ModCommands::clearAccelerator))
        );
    }

    private static int buildAccelerator(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at an Accelerator Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof AcceleratorControllerBlock)) {
            source.sendFailure(Component.literal("Look at an Accelerator Controller block"));
            return 0;
        }

        Direction facing = state.getValue(AcceleratorControllerBlock.FACING);
        AcceleratorPattern.ensureLoaded();

        int[] ctrlOffset = AcceleratorPattern.getControllerOffset();
        int ctrlLocalX = ctrlOffset[0];
        int ctrlLocalZ = ctrlOffset[1];
        int ctrlDx, ctrlDz;
        switch (facing) {
            case NORTH -> { ctrlDx = ctrlLocalZ; ctrlDz = -ctrlLocalX; }
            case SOUTH -> { ctrlDx = -ctrlLocalZ; ctrlDz = ctrlLocalX; }
            case EAST  -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
            case WEST  -> { ctrlDx = -ctrlLocalX; ctrlDz = -ctrlLocalZ; }
            default    -> { ctrlDx = ctrlLocalX; ctrlDz = ctrlLocalZ; }
        }

        int[] placed = {0};
        int height = AcceleratorPattern.getHeight();

        for (AcceleratorPattern.RingOffset offset : AcceleratorPattern.getRingOffsets()) {
            int worldDx, worldDz;
            switch (facing) {
                case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
            }

            int finalX = controllerPos.getX() - ctrlDx + worldDx;
            int finalZ = controllerPos.getZ() - ctrlDz + worldDz;

            for (int y = 0; y < height; y++) {
                BlockPos placePos = new BlockPos(finalX, controllerPos.getY() + y, finalZ);

                // Skip the controller itself
                if (placePos.equals(controllerPos)) continue;

                // Only place if the position is air or replaceable
                if (!level.getBlockState(placePos).canBeReplaced()) continue;

                // Check if this position should be glass
                if (AcceleratorPattern.isGlassPosition(y, offset.localX(), offset.localZ())) {
                    level.setBlock(placePos, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(placePos, ModBlocks.ACCELERATOR_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                placed[0]++;
            }
        }

        source.sendSuccess(() -> Component.literal("Placed " + placed[0] + " blocks for accelerator ring"), true);
        return 1;
    }

    private static int clearAccelerator(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at an Accelerator Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof AcceleratorControllerBlock)) {
            source.sendFailure(Component.literal("Look at an Accelerator Controller block"));
            return 0;
        }

        Direction facing = state.getValue(AcceleratorControllerBlock.FACING);
        var positions = AcceleratorPattern.getStructurePositions(controllerPos, facing);

        int[] removed = {0};
        for (BlockPos pos : positions) {
            if (!level.getBlockState(pos).isAir()) {
                level.destroyBlock(pos, false);
                removed[0]++;
            }
        }

        source.sendSuccess(() -> Component.literal("Cleared " + removed[0] + " blocks"), true);
        return 1;
    }
}
