package com.setusertso.tachyon.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                CyclePortModePacket.TYPE,
                CyclePortModePacket.STREAM_CODEC,
                CyclePortModePacket::handle
        );

        registrar.playToServer(
                ReformStructurePacket.TYPE,
                ReformStructurePacket.STREAM_CODEC,
                ReformStructurePacket::handle
        );

        registrar.playToServer(
                SetInjectorRatePacket.TYPE,
                SetInjectorRatePacket.STREAM_CODEC,
                SetInjectorRatePacket::handle
        );

        registrar.playToServer(
                SetShieldPowerPacket.TYPE,
                SetShieldPowerPacket.STREAM_CODEC,
                SetShieldPowerPacket::handle
        );
    }
}
