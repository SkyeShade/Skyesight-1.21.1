package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.client.world.SkyesightVisualEntity;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class SkyesightClientEntityAnimationHandler {
    private SkyesightClientEntityAnimationHandler() {}

    public static void handle(SkyesightEntityAnimationPayload payload) {
        SkyesightVisualWorld world =
                SkyesightVisualWorldManager.getOrCreate(payload.dimension());

        if (world == null) {
            return;
        }

        SkyesightVisualEntity visualEntity =
                world.entityStore().get(payload.entityUuid());

        if (visualEntity == null) {
            return;
        }


        if (payload.animationType() == SkyesightEntityAnimationPayload.AnimationType.SWING_HAND) {
            visualEntity.triggerSwing(payload.hand());
        }
    }
}