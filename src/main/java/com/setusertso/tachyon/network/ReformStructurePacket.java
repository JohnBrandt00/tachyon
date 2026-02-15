package com.setusertso.tachyon.network;

import com.setusertso.tachyon.block.entity.SingularityControllerBlockEntity;
import com.setusertso.tachyon.tachyon;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReformStructurePacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ReformStructurePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "reform_structure"));

    public static final StreamCodec<ByteBuf, ReformStructurePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ReformStructurePacket::pos,
                    ReformStructurePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReformStructurePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                if (sp.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5,
                        packet.pos.getZ() + 0.5) < 64) {
                    if (sp.level().getBlockEntity(packet.pos) instanceof SingularityControllerBlockEntity controller) {
                        controller.reformStructure();
                    }
                }
            }
        });
    }
}
