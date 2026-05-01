package com.skyeshade.skyesight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class SkyesightLightTextureRenderer {
    private SkyesightLightTextureRenderer() {}

    public static void updateFor(ClientLevel level, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        ClientLevel previousLevel = minecraft.level;

        try {
            minecraft.level = level;
            minecraft.gameRenderer.lightTexture().updateLightTexture(partialTick);
        } finally {
            minecraft.level = previousLevel;
        }
    }
}