package com.skyeshade.skyesight.mixin.client;

import com.skyeshade.skyesight.client.render.light.SkyesightLightTextureContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"
            )
    )
    private ClientLevel skyesight$useSkyesightLevel(Minecraft minecraft) {
        ClientLevel level = SkyesightLightTextureContext.level();

        if (level != null) {
            return level;
        }

        return minecraft.level;
    }
}