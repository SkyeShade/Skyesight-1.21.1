package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.server.SkyesightServerChunkSender;
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
        registrar.playToClient(
                SkyesightBlockUpdatesPayload.TYPE,
                SkyesightBlockUpdatesPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> SkyesightClientBlockUpdateHandler.handle(payload)
                )
        );
        registrar.playToClient(
                SkyesightLightDataPayload.TYPE,
                SkyesightLightDataPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> SkyesightClientLightDataHandler.handle(payload)
                )
        );
        registrar.playToClient(
                SkyesightEntitySnapshotPayload.TYPE,
                SkyesightEntitySnapshotPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> SkyesightClientEntityHandler.handle(payload)
                )
        );
        registrar.playToClient(
                SkyesightBlockEventPayload.TYPE,
                SkyesightBlockEventPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> SkyesightClientBlockEventHandler.handle(payload)
                )
        );
        registrar.playToClient(
                SkyesightEntityAnimationPayload.TYPE,
                SkyesightEntityAnimationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> SkyesightClientEntityAnimationHandler.handle(payload)
                )
        );
        registrar.playToClient(
                SkyesightParticlePayload.TYPE,
                SkyesightParticlePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> SkyesightClientParticleHandler.handle(payload)
                )
        );
    }
}