package com.skyeshade.skyesight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skyeshade.skyesight.client.render.entity.SkyesightNameTagSuppressor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererNameTagMixin<T extends Entity> {
    @Inject(
            method = "renderNameTag",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skyesight$hideOwnerNameTag(
            T entity,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            float partialTick,
            CallbackInfo ci
    ) {
        if (SkyesightNameTagSuppressor.shouldSuppressName(entity)) {
            ci.cancel();
        }
    }
}