package com.skyeshade.skyesight.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SkyesightClientboundPayloads {
    private SkyesightClientboundPayloads() {}

    public static void handleChunkData(
            SkyesightChunkDataPayload payload,
            IPayloadContext context
    ) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        context.enqueueWork(() -> SkyesightClientChunkHandler.handleChunkDataOnClient(payload));
    }
}