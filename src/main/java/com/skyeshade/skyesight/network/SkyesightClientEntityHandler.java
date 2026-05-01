package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;

public final class SkyesightClientEntityHandler {
    private SkyesightClientEntityHandler() {}

    public static void handle(SkyesightEntitySnapshotPayload payload) {
        SkyesightVisualWorld world =
                SkyesightVisualWorldManager.getOrCreate(payload.dimension());

        if (world == null) {
            return;
        }

        world.entityStore().applySnapshot(payload);
    }
}