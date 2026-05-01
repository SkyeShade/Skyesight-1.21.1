package com.skyeshade.skyesight.mixin.client;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
    @Accessor("updateLightTexture")
    void skyesight$setUpdateLightTexture(boolean value);
}