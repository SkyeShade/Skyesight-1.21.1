package com.skyeshade.skyesight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererEndSkyInvoker {
    @Invoker("renderEndSky")
    void skyesight$renderEndSky(PoseStack poseStack);
}