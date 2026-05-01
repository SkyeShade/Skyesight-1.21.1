package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.client.SkyesightClientChunkRequester;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SkyesightClientChunkHandler {
    private SkyesightClientChunkHandler() {}

    public static void handleChunkDataOnClient(SkyesightChunkDataPayload payload) {
        Skyesight.LOGGER.info(
                "[Skyesight] Received chunk {}, {} for view {}",
                payload.chunkX(),
                payload.chunkZ(),
                payload.viewId()
        );

        SkyesightVisualWorld world =
                SkyesightVisualWorldManager.getOrCreate(payload.dimension());

        if (world == null) {
            return;
        }

        world.chunkReceiver().setViewCenter(
                payload.centerChunkX(),
                payload.centerChunkZ(),
                payload.radius() + 3
        );

        world.chunkReceiver().pruneOutside(
                payload.centerChunkX(),
                payload.centerChunkZ(),
                payload.radius() + 3
        );


        boolean inserted = world.chunkReceiver().receiveChunkWithLight(
                payload.chunkX(),
                payload.chunkZ(),
                payload.chunkData(),
                payload.lightData(),
                world.renderer()::scheduleTerrainUpdate
        );

        if (inserted) {
            SkyesightClientChunkRequester.markChunkReceived(
                    payload.dimension(),
                    payload.chunkX(),
                    payload.chunkZ()
            );
        }


        Skyesight.LOGGER.info(
                "[Skyesight] Skyesight loaded chunks={}",
                world.level().getChunkSource().getLoadedChunksCount()
        );
    }

}