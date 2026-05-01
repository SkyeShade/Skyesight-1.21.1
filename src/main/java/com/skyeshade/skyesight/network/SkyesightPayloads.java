package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.Skyesight;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SkyesightPayloads {
    private SkyesightPayloads() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        Skyesight.LOGGER.info("[Skyesight] Registering network payloads");

        PayloadRegistrar registrar = event.registrar(Skyesight.MODID)
                .versioned("1");

        registrar.playToServer(
                SkyesightChunkRequestPayload.TYPE,
                SkyesightChunkRequestPayload.STREAM_CODEC,
                SkyesightServerChunkSender::handleChunkRequest
        );

        registrar.playToClient(
                SkyesightChunkDataPayload.TYPE,
                SkyesightChunkDataPayload.STREAM_CODEC,
                SkyesightClientboundPayloads::handleChunkData
        );
    }
}