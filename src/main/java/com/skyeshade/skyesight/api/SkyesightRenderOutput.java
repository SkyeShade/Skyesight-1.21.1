package com.skyeshade.skyesight.api;

import net.minecraft.resources.ResourceLocation;

public interface SkyesightRenderOutput {
    ResourceLocation viewId();

    boolean isReady();

    int width();

    int height();
}