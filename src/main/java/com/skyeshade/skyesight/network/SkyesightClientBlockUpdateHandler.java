package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public final class SkyesightClientBlockUpdateHandler {
    private SkyesightClientBlockUpdateHandler() {}

    public static void handle(SkyesightBlockUpdatesPayload payload) {
        SkyesightVisualWorld world =
                SkyesightVisualWorldManager.getOrCreate(payload.dimension());

        if (world == null) {
            return;
        }

        for (SkyesightBlockUpdatesPayload.Entry update : payload.updates()) {
            world.chunkReceiver().applyBlockUpdate(update.pos(), update.state());
            world.renderer().scheduleBlockUpdate(update.pos());
        }

        world.renderer().scheduleTerrainUpdate();
    }
}