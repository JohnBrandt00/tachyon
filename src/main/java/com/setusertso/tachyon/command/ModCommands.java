package com.setusertso.tachyon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.setusertso.tachyon.ModBlocks;
import com.setusertso.tachyon.block.AcceleratorControllerBlock;
import com.setusertso.tachyon.block.AcceleratorPattern;
import com.setusertso.tachyon.block.PhotonicInjectorBlock;
import com.setusertso.tachyon.block.SingularityControllerBlock;
import com.setusertso.tachyon.block.SingularityPattern;
import com.setusertso.tachyon.block.SingularityPortBlock;
import com.setusertso.tachyon.block.VoidMinerControllerBlock;
import com.setusertso.tachyon.block.VoidMinerPattern;
import com.setusertso.tachyon.block.VoidMinerPortBlock;
import com.setusertso.tachyon.block.CondenserControllerBlock;
import com.setusertso.tachyon.block.CondenserPattern;
import com.setusertso.tachyon.block.CondenserPortBlock;
import com.setusertso.tachyon.block.entity.CondenserControllerBlockEntity;
import com.setusertso.tachyon.block.entity.CondenserPortMode;
import com.setusertso.tachyon.block.entity.PhotonicInjectorBlockEntity;
import com.setusertso.tachyon.block.entity.PortMode;
import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;
import com.setusertso.tachyon.block.entity.VoidMinerControllerBlockEntity;
import com.setusertso.tachyon.ModItems;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
                .then(Commands.literal("void_miner_build")
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
                                .executes(ModCommands::buildVoidMiner)))
                .then(Commands.literal("void_miner_clear")
                        .executes(ModCommands::clearVoidMiner))
                .then(Commands.literal("condenser_build")
                        .executes(ModCommands::buildCondenser))
                .then(Commands.literal("condenser_clear")
                        .executes(ModCommands::clearCondenser))
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

        // Place 4 Singularity Ports on the horizontal ring for I/O
        BlockState energyInPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ENERGY_INPUT);
        BlockState energyOutPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ENERGY_OUTPUT);
        BlockState itemInPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ITEM_INPUT);
        BlockState itemOutPort = ModBlocks.SINGULARITY_PORT.get().defaultBlockState()
                .setValue(SingularityPortBlock.MODE, PortMode.ITEM_OUTPUT);

        // Energy input at +x, energy output at top, item input at +z, item output at -z
        BlockPos energyInPortPos = new BlockPos(center.getX() + radius, center.getY(), center.getZ());
        BlockPos energyOutPortPos = new BlockPos(center.getX(), center.getY() + radius, center.getZ());
        BlockPos itemInPortPos = new BlockPos(center.getX(), center.getY(), center.getZ() + radius);
        BlockPos itemOutPortPos = new BlockPos(center.getX(), center.getY(), center.getZ() - radius);

        for (var entry : new Object[][]{
                {energyInPortPos, energyInPort}, {energyOutPortPos, energyOutPort},
                {itemInPortPos, itemInPort}, {itemOutPortPos, itemOutPort}}) {
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

        // Place a Photonic Injector on the +Z side, 1 block beyond the shell, facing -Z (toward center)
        BlockPos injectorPos = new BlockPos(center.getX(), center.getY(), center.getZ() + radius + 1);
        if (level.getBlockState(injectorPos).canBeReplaced()) {
            BlockState injectorState = ModBlocks.PHOTONIC_INJECTOR.get().defaultBlockState()
                    .setValue(PhotonicInjectorBlock.FACING, Direction.NORTH); // facing -Z = toward center
            level.setBlock(injectorPos, injectorState, Block.UPDATE_ALL);
            placed++;
            // Load it with condensed light
            if (level.getBlockEntity(injectorPos) instanceof PhotonicInjectorBlockEntity injector) {
                injector.getItems().setStackInSlot(0, new net.minecraft.world.item.ItemStack(ModItems.CONDENSED_LIGHT.get(), 64));
            }
        }

        // Place Creative Power Source next to energy input port (+x side, one more out)
        BlockPos powerSourcePos = new BlockPos(center.getX() + radius + 1, center.getY(), center.getZ());
        if (level.getBlockState(powerSourcePos).canBeReplaced()) {
            level.setBlock(powerSourcePos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }

        // Place Creative Power Sink next to energy output port (top, one more up)
        BlockPos powerSinkPos = new BlockPos(center.getX(), center.getY() + radius + 1, center.getZ());
        if (level.getBlockState(powerSinkPos).canBeReplaced()) {
            level.setBlock(powerSinkPos, ModBlocks.CREATIVE_POWER_SINK.get().defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }

        int totalPlaced = placed;

        // Auto-form the structure
        if (level.getBlockEntity(controllerPos) instanceof SingularityControllerBlockEntity be) {
            be.tryFormStructure();
            if (be.isFormed()) {
                source.sendSuccess(() -> Component.literal("Built and formed singularity sphere (" + totalPlaced + " blocks). Injector + power blocks placed."), true);
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

    private static int buildVoidMiner(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        int tier = IntegerArgumentType.getInteger(context, "tier");

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a block to place the void miner there"));
            return 0;
        }

        BlockPos targetPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        Level level = player.level();

        int outerSize = VoidMinerPattern.SHELL_SIZES[tier];
        int originX = targetPos.getX();
        int originY = targetPos.getY();
        int originZ = targetPos.getZ();

        // Place controller at origin (0,0,0) of the outermost shell
        level.setBlock(targetPos, ModBlocks.VOID_MINER_CONTROLLER.get().defaultBlockState(), Block.UPDATE_ALL);
        int placed = 1;

        // Place each shell from tier 1 up to the requested tier
        for (int t = 1; t <= tier; t++) {
            int shellSize = VoidMinerPattern.SHELL_SIZES[t];
            int offset = (outerSize - shellSize) / 2;
            Block frameBlock = VoidMinerPattern.getFrameBlockForTier(t);

            for (int y = 0; y < shellSize; y++) {
                for (int x = 0; x < shellSize; x++) {
                    for (int z = 0; z < shellSize; z++) {
                        if (!VoidMinerPattern.isStructureBlock(x, y, z, shellSize)) continue;

                        BlockPos pos = new BlockPos(
                                originX + offset + x,
                                originY + offset + y,
                                originZ + offset + z);

                        if (pos.equals(targetPos)) continue;

                        level.setBlock(pos, frameBlock.defaultBlockState(), Block.UPDATE_ALL);
                        placed++;
                    }
                }
            }
        }

        // Clear 3x3x3 interior
        int interiorOffset = (outerSize - VoidMinerPattern.INTERIOR_SIZE) / 2;
        for (int y = 0; y < VoidMinerPattern.INTERIOR_SIZE; y++) {
            for (int x = 0; x < VoidMinerPattern.INTERIOR_SIZE; x++) {
                for (int z = 0; z < VoidMinerPattern.INTERIOR_SIZE; z++) {
                    BlockPos airPos = new BlockPos(
                            originX + interiorOffset + x,
                            originY + interiorOffset + y,
                            originZ + interiorOffset + z);
                    if (!level.getBlockState(airPos).isAir()) {
                        level.setBlock(airPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Clear chimney (from top of interior through all shell tops)
        int chimneyStartY = originY + interiorOffset + VoidMinerPattern.INTERIOR_SIZE;
        int chimneyEndY = originY + outerSize;
        int chimneyXStart = originX + (outerSize - VoidMinerPattern.INTERIOR_SIZE) / 2;
        int chimneyZStart = originZ + (outerSize - VoidMinerPattern.INTERIOR_SIZE) / 2;
        for (int y = chimneyStartY; y < chimneyEndY; y++) {
            for (int x = 0; x < VoidMinerPattern.INTERIOR_SIZE; x++) {
                for (int z = 0; z < VoidMinerPattern.INTERIOR_SIZE; z++) {
                    BlockPos chimneyPos = new BlockPos(chimneyXStart + x, y, chimneyZStart + z);
                    if (!level.getBlockState(chimneyPos).isAir()) {
                        level.setBlock(chimneyPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Clear sky access above (10 blocks)
        for (int dy = 0; dy < 10; dy++) {
            for (int x = 0; x < VoidMinerPattern.INTERIOR_SIZE; x++) {
                for (int z = 0; z < VoidMinerPattern.INTERIOR_SIZE; z++) {
                    BlockPos skyPos = new BlockPos(chimneyXStart + x, chimneyEndY + dy, chimneyZStart + z);
                    if (!level.getBlockState(skyPos).isAir()) {
                        level.setBlock(skyPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Place 3 ports on the outer shell bottom face
        BlockPos energyPortPos = new BlockPos(originX + outerSize / 2, originY, originZ);
        level.setBlock(energyPortPos, ModBlocks.VOID_MINER_PORT.get().defaultBlockState()
                .setValue(VoidMinerPortBlock.MODE, VoidMinerPortBlock.VoidMinerPortMode.ENERGY_INPUT), Block.UPDATE_ALL);

        BlockPos itemPortPos = new BlockPos(originX + outerSize / 2, originY, originZ + outerSize - 1);
        level.setBlock(itemPortPos, ModBlocks.VOID_MINER_PORT.get().defaultBlockState()
                .setValue(VoidMinerPortBlock.MODE, VoidMinerPortBlock.VoidMinerPortMode.ITEM_OUTPUT), Block.UPDATE_ALL);

        BlockPos catalystPortPos = new BlockPos(originX, originY, originZ + outerSize / 2);
        level.setBlock(catalystPortPos, ModBlocks.VOID_MINER_PORT.get().defaultBlockState()
                .setValue(VoidMinerPortBlock.MODE, VoidMinerPortBlock.VoidMinerPortMode.CATALYST_INPUT), Block.UPDATE_ALL);

        // Place Creative Power Source adjacent to energy port
        BlockPos powerSourcePos = new BlockPos(originX + outerSize / 2, originY, originZ - 1);
        if (level.getBlockState(powerSourcePos).canBeReplaced()) {
            level.setBlock(powerSourcePos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }

        // Auto-form and load catalyst
        int totalPlaced = placed;
        if (level.getBlockEntity(targetPos) instanceof VoidMinerControllerBlockEntity be) {
            be.tryFormStructure();
            if (be.isFormed()) {
                be.getItems().setStackInSlot(VoidMinerControllerBlockEntity.CATALYST_SLOT,
                        new ItemStack(ModItems.EXOTIC_MATTER.get(), 64));
                source.sendSuccess(() -> Component.literal("Built and formed Tier " + tier + " Void Miner ("
                        + outerSize + "x" + outerSize + "x" + outerSize + ", " + totalPlaced + " blocks). Power source + exotic matter placed."), true);
            } else {
                source.sendSuccess(() -> Component.literal("Placed " + totalPlaced + " blocks but formation failed - check sky access"), true);
            }
        } else {
            source.sendSuccess(() -> Component.literal("Placed " + totalPlaced + " blocks for Tier " + tier + " Void Miner"), true);
        }

        return 1;
    }

    private static int clearVoidMiner(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a Void Miner Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof VoidMinerControllerBlock)) {
            source.sendFailure(Component.literal("Look at a Void Miner Controller block"));
            return 0;
        }

        // Get structure origin from the controller entity if formed
        BlockPos origin = null;
        int outerSize = 0;

        if (level.getBlockEntity(controllerPos) instanceof VoidMinerControllerBlockEntity be) {
            if (be.getStructureOrigin() != null) {
                origin = be.getStructureOrigin();
                outerSize = VoidMinerPattern.SHELL_SIZES[be.getStructureTier()];
            }
            be.disassembleStructure();
        }

        // If we couldn't get origin from the entity, assume controller is at corner
        if (origin == null) {
            origin = controllerPos;
            outerSize = VoidMinerPattern.SHELL_SIZES[1]; // assume T1
        }

        if (origin == null) {
            source.sendFailure(Component.literal("Could not determine void miner structure origin"));
            return 0;
        }

        int removed = 0;

        // Clear the entire bounding box of the largest possible structure
        for (int y = 0; y < outerSize; y++) {
            for (int x = 0; x < outerSize; x++) {
                for (int z = 0; z < outerSize; z++) {
                    BlockPos pos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.destroyBlock(pos, false);
                        removed++;
                    }
                }
            }
        }

        // Remove creative power source if present nearby (check 1 block out from each face)
        for (int x = -1; x <= outerSize; x++) {
            for (int z = -1; z <= outerSize; z++) {
                for (int y = -1; y <= outerSize; y++) {
                    // Only check border positions
                    if (x >= 0 && x < outerSize && z >= 0 && z < outerSize && y >= 0 && y < outerSize) continue;
                    BlockPos checkPos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (level.getBlockState(checkPos).getBlock() == ModBlocks.CREATIVE_POWER_SOURCE.get()) {
                        level.destroyBlock(checkPos, false);
                        removed++;
                    }
                }
            }
        }

        int totalRemoved = removed;
        source.sendSuccess(() -> Component.literal("Cleared " + totalRemoved + " void miner blocks"), true);
        return 1;
    }

    private static int buildCondenser(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a Condenser Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof CondenserControllerBlock)) {
            source.sendFailure(Component.literal("Look at a Condenser Controller block"));
            return 0;
        }

        Direction facing = state.getValue(CondenserControllerBlock.FACING);
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();

        int placed = 0;

        // Build the 3x3x5 structure around the controller
        for (int ly = 0; ly < 5; ly++) {
            for (int lx = -1; lx <= 1; lx++) {
                for (int lz = 0; lz < 3; lz++) {
                    BlockPos worldPos = controllerPos
                            .relative(right, lx)
                            .relative(back, lz)
                            .above(ly);

                    if (worldPos.equals(controllerPos)) continue;

                    // Interior air column: center (lx=0, lz=1) at y=1,2,3
                    boolean isInterior = (lx == 0 && lz == 1 && ly >= 1 && ly <= 3);
                    if (isInterior) {
                        if (!level.getBlockState(worldPos).isAir()) {
                            level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                        continue;
                    }

                    if (!level.getBlockState(worldPos).canBeReplaced()) continue;

                    level.setBlock(worldPos, ModBlocks.CONDENSER_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                    placed++;
                }
            }
        }

        // Place ports: energy input on bottom back-center, fluid output on top, item input on a side
        // Energy input port at bottom-back-center (lx=0, lz=2, ly=0)
        BlockPos energyPortPos = controllerPos.relative(back, 2);
        if (level.getBlockState(energyPortPos).getBlock() == ModBlocks.CONDENSER_CASING.get()) {
            level.setBlock(energyPortPos, ModBlocks.CONDENSER_PORT.get().defaultBlockState()
                    .setValue(CondenserPortBlock.MODE, CondenserPortMode.ENERGY_INPUT), Block.UPDATE_ALL);
        }

        // Fluid output port at top-back-center (lx=0, lz=2, ly=4)
        BlockPos fluidPortPos = controllerPos.relative(back, 2).above(4);
        if (level.getBlockState(fluidPortPos).getBlock() == ModBlocks.CONDENSER_CASING.get()) {
            level.setBlock(fluidPortPos, ModBlocks.CONDENSER_PORT.get().defaultBlockState()
                    .setValue(CondenserPortBlock.MODE, CondenserPortMode.FLUID_OUTPUT), Block.UPDATE_ALL);
        }

        // Item input port on the right side (lx=1, lz=1, ly=0)
        BlockPos itemPortPos = controllerPos.relative(right, 1).relative(back, 1);
        if (level.getBlockState(itemPortPos).getBlock() == ModBlocks.CONDENSER_CASING.get()) {
            level.setBlock(itemPortPos, ModBlocks.CONDENSER_PORT.get().defaultBlockState()
                    .setValue(CondenserPortBlock.MODE, CondenserPortMode.ITEM_INPUT), Block.UPDATE_ALL);
        }

        // Place Creative Power Source adjacent to energy port
        BlockPos powerSourcePos = energyPortPos.relative(back, 1);
        if (level.getBlockState(powerSourcePos).canBeReplaced()) {
            level.setBlock(powerSourcePos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }

        // Auto-form the structure
        int totalPlaced = placed;
        if (level.getBlockEntity(controllerPos) instanceof CondenserControllerBlockEntity be) {
            be.tryFormStructure();
            if (be.isFormed()) {
                source.sendSuccess(() -> Component.literal("Built and formed Tachyon Condenser (" + totalPlaced + " blocks). Power source placed."), true);
            } else {
                source.sendSuccess(() -> Component.literal("Placed " + totalPlaced + " blocks but formation failed - check structure"), true);
            }
        } else {
            source.sendSuccess(() -> Component.literal("Placed " + totalPlaced + " blocks for Tachyon Condenser"), true);
        }

        return 1;
    }

    private static int clearCondenser(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a Condenser Controller block"));
            return 0;
        }

        BlockPos controllerPos = blockHit.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(controllerPos);

        if (!(state.getBlock() instanceof CondenserControllerBlock)) {
            source.sendFailure(Component.literal("Look at a Condenser Controller block"));
            return 0;
        }

        // Disassemble first
        if (level.getBlockEntity(controllerPos) instanceof CondenserControllerBlockEntity be) {
            be.disassembleStructure();
        }

        Direction facing = state.getValue(CondenserControllerBlock.FACING);
        var positions = CondenserPattern.getStructurePositions(controllerPos, facing);

        int removed = 0;
        for (BlockPos pos : positions) {
            if (!level.getBlockState(pos).isAir()) {
                level.destroyBlock(pos, false);
                removed++;
            }
        }

        // Also remove creative power source if placed behind the back
        Direction back = facing.getOpposite();
        BlockPos powerSourcePos = controllerPos.relative(back, 3);
        if (level.getBlockState(powerSourcePos).getBlock() == ModBlocks.CREATIVE_POWER_SOURCE.get()) {
            level.destroyBlock(powerSourcePos, false);
            removed++;
        }

        int totalRemoved = removed;
        source.sendSuccess(() -> Component.literal("Cleared " + totalRemoved + " condenser blocks"), true);
        return 1;
    }
}
