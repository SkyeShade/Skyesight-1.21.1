package com.skyeshade.skyesight.client.render.light;

import com.skyeshade.skyesight.mixin.client.LightTextureAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;

public final class SkyesightLightTextureUpdater {
    private SkyesightLightTextureUpdater() {}

    public static void updateFor(ClientLevel level, Camera camera, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LightTexture lightTexture = minecraft.gameRenderer.lightTexture();

        try (SkyesightLightTextureContext.Scope ignored =
                     SkyesightLightTextureContext.push(level, camera)) {
            ((LightTextureAccessor) lightTexture).skyesight$setUpdateLightTexture(true);
            lightTexture.updateLightTexture(partialTick);
        }
    }

    public static void restoreMain(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.gameRenderer == null) {
            return;
        }

        LightTexture lightTexture = minecraft.gameRenderer.lightTexture();

        ((LightTextureAccessor) lightTexture).skyesight$setUpdateLightTexture(true);
        lightTexture.updateLightTexture(partialTick);
    }
}