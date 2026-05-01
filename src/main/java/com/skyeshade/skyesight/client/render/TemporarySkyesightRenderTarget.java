package com.skyeshade.skyesight.client.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;

public final class TemporarySkyesightRenderTarget {
    private static TextureTarget target;

    private TemporarySkyesightRenderTarget() {}

    public static TextureTarget getOrCreate(int width, int height) {
        if (target == null) {
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        }

        if (target.width != width || target.height != height) {
            target.resize(width, height, Minecraft.ON_OSX);
        }

        return target;
    }
    public static void close() {
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }
    public static int colorTextureId() {
        return target == null ? -1 : target.getColorTextureId();
    }

    public static boolean hasTarget() {
        return target != null;
    }
}