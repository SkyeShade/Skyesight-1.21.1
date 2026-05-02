package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;

public final class SkyesightClientParticleHandler {
    private SkyesightClientParticleHandler() {}

    public static void handle(SkyesightParticlePayload payload) {
        SkyesightVisualWorld world =
                SkyesightVisualWorldManager.getOrCreate(payload.dimension());

        if (world == null) {
            return;
        }

        world.particles().addParticle(payload);
    }
}