package com.skyeshade.skyesight.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("level")
    ClientLevel skyesight$getLevel();

    @Accessor("level")
    void skyesight$setLevel(ClientLevel level);
}