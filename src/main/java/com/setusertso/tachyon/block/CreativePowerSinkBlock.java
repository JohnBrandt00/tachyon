package com.setusertso.tachyon.block;

import com.setusertso.tachyon.block.entity.CreativePowerSinkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CreativePowerSinkBlock extends Block implements EntityBlock {

    public CreativePowerSinkBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativePowerSinkBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (lvl, pos, st, be) -> CreativePowerSinkBlockEntity.serverTick(lvl, pos, st, (CreativePowerSinkBlockEntity) be);
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CreativePowerSinkBlockEntity sink) {
            sink.cycleRate();
            String formatted = formatRate(sink.getConsumeRate());
            player.sendSystemMessage(Component.literal("Power Sink: " + formatted + " RF/t"));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static String formatRate(int rate) {
        if (rate >= 1_000_000) return (rate / 1_000_000) + "M";
        if (rate >= 1_000) return (rate / 1_000) + "k";
        return String.valueOf(rate);
    }
}
