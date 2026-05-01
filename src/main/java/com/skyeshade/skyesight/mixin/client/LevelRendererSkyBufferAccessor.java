package com.skyeshade.skyesight.mixin.client;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererSkyBufferAccessor {
    @Accessor("skyBuffer")
    VertexBuffer skyesight$getSkyBuffer();

    @Accessor("starBuffer")
    VertexBuffer skyesight$getStarBuffer();

    @Accessor("darkBuffer")
    VertexBuffer skyesight$getDarkBuffer();

    @Accessor("ticks")
    int skyesight$getTicks();
}