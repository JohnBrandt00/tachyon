package com.setusertso.tachyon.network;

import com.setusertso.tachyon.block.entity.AcceleratorPortBlockEntity;
import com.setusertso.tachyon.block.entity.CondenserPortBlockEntity;
import com.setusertso.tachyon.block.entity.SingularityPortBlockEntity;
import com.setusertso.tachyon.tachyon;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CyclePortModePacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<CyclePortModePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "cycle_port_mode"));

    public static final StreamCodec<ByteBuf, CyclePortModePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    CyclePortModePacket::pos,
                    CyclePortModePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CyclePortModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                if (sp.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5,
                        packet.pos.getZ() + 0.5) < 64) {
                    if (sp.level().getBlockEntity(packet.pos) instanceof AcceleratorPortBlockEntity port) {
                        port.cycleMode();
                    } else if (sp.level().getBlockEntity(packet.pos) instanceof SingularityPortBlockEntity port) {
                        port.cycleMode();
                    } else if (sp.level().getBlockEntity(packet.pos) instanceof CondenserPortBlockEntity port) {
                        port.cycleMode();
                    }
                }
            }
        });
    }
}
