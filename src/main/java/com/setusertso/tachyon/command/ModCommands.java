package com.setusertso.tachyon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorPattern;
import com.setusertso.tachyon.block.SingularityControllerBlock;
import com.setusertso.tachyon.block.SingularityPattern;
import com.setusertso.tachyon.block.SingularityPortBlock;
import com.setusertso.tachyon.block.entity.PortMode;
import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;

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
                .then(Commands.literal("singularity_build")
                        .executes(ModCommands::buildSingularity))
                .then(Commands.literal("singularity_clear")
                        .executes(ModCommands::clearSingularity))
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

        int centerX = controllerPos.getX() - ctrlDx;
        int centerZ = controllerPos.getZ() - ctrlDz;

        int[] placed = {0};
        int height = AcceleratorPattern.getHeight();

        // Place shell blocks for each layer
        for (int y = 0; y < height; y++) {
            for (AcceleratorPattern.RingOffset offset : AcceleratorPattern.getBlockOffsetsForLayer(y)) {
                int worldDx, worldDz;
                switch (facing) {
                    case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                    case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                    case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                    case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                    default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                }

                BlockPos placePos = new BlockPos(centerX + worldDx, controllerPos.getY() + y, centerZ + worldDz);

                // Skip the controller itself
                if (placePos.equals(controllerPos)) continue;

                // Only place if the position is air or replaceable
                if (!level.getBlockState(placePos).canBeReplaced()) continue;

                // Check if this position should be glass
                if (AcceleratorPattern.isGlassPosition(y, offset.localX(), offset.localZ())) {
                    level.setBlock(placePos, ModBlocks.ACCELERATOR_GLASS.get().defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(placePos, ModBlocks.ACCELERATOR_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                placed[0]++;
            }
        }

        // Clear air positions (ensure tube interior is air)
        for (int y = 0; y < height; y++) {
            for (AcceleratorPattern.RingOffset offset : AcceleratorPattern.getAirOffsetsForLayer(y)) {
                int worldDx, worldDz;
                switch (facing) {
                    case NORTH -> { worldDx = offset.localZ(); worldDz = -offset.localX(); }
                    case SOUTH -> { worldDx = -offset.localZ(); worldDz = offset.localX(); }
                    case EAST  -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                    case WEST  -> { worldDx = -offset.localX(); worldDz = -offset.localZ(); }
                    default    -> { worldDx = offset.localX(); worldDz = offset.localZ(); }
                }

                BlockPos tubePos = new BlockPos(centerX + worldDx, controllerPos.getY() + y, centerZ + worldDz);
                if (!level.getBlockState(tubePos).isAir()) {
                    level.setBlock(tubePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        source.sendSuccess(() -> Component.literal("Placed " + placed[0] + " blocks for accelerator torus"), true);
        return 1;
    }

    private static int buildSingularity(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a Singularity Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof SingularityControllerBlock)) {
            source.sendFailure(Component.literal("Look at a Singularity Controller block"));
            return 0;
        }

        SingularityPattern.ensureLoaded();
        BlockPos center = SingularityPattern.getCenterPosition(controllerPos);
        int sphereHeight = SingularityPattern.getHeight();
        int radius = sphereHeight / 2;
        int baseY = center.getY() - radius;

        int placed = 0;

        // Place shell blocks for each layer
        for (int y = 0; y < sphereHeight; y++) {
            for (int[] offset : SingularityPattern.getShellOffsetsForLayer(y)) {
                BlockPos placePos = new BlockPos(center.getX() + offset[0], baseY + y, center.getZ() + offset[1]);

                // Skip the controller itself
                if (placePos.equals(controllerPos)) continue;

                // Only place if the position is air or replaceable
                if (!level.getBlockState(placePos).canBeReplaced()) continue;

                level.setBlock(placePos, ModBlocks.SINGULARITY_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                placed++;
            }
        }

        // Place one Exotic Matter Core at the dead center of the sphere
        BlockPos corePos = new BlockPos(center.getX(), center.getY(), center.getZ());
        if (level.getBlockState(corePos).canBeReplaced() || level.getBlockState(corePos).getBlock() == ModBlocks.SINGULARITY_CASING.get()) {
            level.setBlock(corePos, ModBlocks.EXOTIC_MATTER_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }

        // Place 3 Singularity Ports on the horizontal ring for I/O
        BlockState energyPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ENERGY_INPUT);
        BlockState itemInPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ITEM_INPUT);
        BlockState itemOutPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ITEM_OUTPUT);

        // Energy input at +x side, item input at +z side, item output at -z side
        BlockPos energyPortPos = new BlockPos(center.getX() + radius, center.getY(), center.getZ());
        BlockPos itemInPortPos = new BlockPos(center.getX(), center.getY(), center.getZ() + radius);
        BlockPos itemOutPortPos = new BlockPos(center.getX(), center.getY(), center.getZ() - radius);

        for (var entry : new Object[][]{{energyPortPos, energyPort}, {itemInPortPos, itemInPort}, {itemOutPortPos, itemOutPort}}) {
            BlockPos portPos = (BlockPos) entry[0];
            BlockState portState = (BlockState) entry[1];
            if (!portPos.equals(controllerPos)) {
                BlockState existing = level.getBlockState(portPos);
                if (existing.canBeReplaced() || existing.getBlock() == ModBlocks.SINGULARITY_CASING.get()) {
                    level.setBlock(portPos, portState, Block.UPDATE_ALL);
                    placed++;
                }
            }
        }

        // Clear air positions (ensure interior is air)
        for (int y = 0; y < sphereHeight; y++) {
            for (int[] offset : SingularityPattern.getAirOffsetsForLayer(y)) {
                BlockPos airPos = new BlockPos(center.getX() + offset[0], baseY + y, center.getZ() + offset[1]);
                if (!level.getBlockState(airPos).isAir()) {
                    level.setBlock(airPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        int totalPlaced = placed;

        // Auto-form the structure
        if (level.getBlockEntity(controllerPos) instanceof SingularityControllerBlockEntity be) {
            be.tryFormStructure();
            if (be.isFormed()) {
                source.sendSuccess(() -> Component.literal("Built and formed singularity sphere (" + totalPlaced + " blocks)"), true);
            } else {
                source.sendSuccess(() -> Component.literal("Placed " + totalPlaced + " blocks but formation failed - check structure"), true);
            }
        } else {
            source.sendSuccess(() -> Component.literal("Placed " + totalPlaced + " blocks for singularity sphere"), true);
        }

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

    private static int clearSingularity(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a Singularity Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof SingularityControllerBlock)) {
            source.sendFailure(Component.literal("Look at a Singularity Controller block"));
            return 0;
        }

        // Disassemble first
        if (level.getBlockEntity(controllerPos) instanceof SingularityControllerBlockEntity be) {
            be.disassembleStructure();
        }

        SingularityPattern.ensureLoaded();
        BlockPos center = SingularityPattern.getCenterPosition(controllerPos);
        int sphereHeight = SingularityPattern.getHeight();
        int radius = sphereHeight / 2;
        int baseY = center.getY() - radius;

        int[] removed = {0};

        // Remove all shell blocks
        for (int y = 0; y < sphereHeight; y++) {
            for (int[] offset : SingularityPattern.getShellOffsetsForLayer(y)) {
                BlockPos pos = new BlockPos(center.getX() + offset[0], baseY + y, center.getZ() + offset[1]);
                if (pos.equals(controllerPos)) continue;
                if (!level.getBlockState(pos).isAir()) {
                    level.destroyBlock(pos, false);
                    removed[0]++;
                }
            }
        }

        source.sendSuccess(() -> Component.literal("Cleared " + removed[0] + " singularity blocks"), true);
        return 1;
    }
}
