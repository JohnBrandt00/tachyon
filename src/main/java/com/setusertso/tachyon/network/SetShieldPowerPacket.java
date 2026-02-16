package com.setusertso.tachyon.network;

import com.setusertso.tachyon.block.entity.SingularityPortBlockEntity;
import com.setusertso.tachyon.tachyon;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetShieldPowerPacket(BlockPos pos, int rate) implements CustomPacketPayload {

    public static final Type<SetShieldPowerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "set_shield_power"));

    public static final StreamCodec<ByteBuf, SetShieldPowerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SetShieldPowerPacket::pos,
                    ByteBufCodecs.VAR_INT,
                    SetShieldPowerPacket::rate,
                    SetShieldPowerPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetShieldPowerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                if (sp.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5,
                        packet.pos.getZ() + 0.5) < 64) {
                    if (sp.level().getBlockEntity(packet.pos) instanceof SingularityPortBlockEntity port) {
                        port.setShieldPowerRate(packet.rate);
                    }
                }
            }
        });
    }
}
