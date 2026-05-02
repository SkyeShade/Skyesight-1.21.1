package com.skyeshade.skyesight.mixin.server;

import com.skyeshade.skyesight.server.SkyesightServerEntityAnimationBroadcaster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
    @Inject(
            method = "swing(Lnet/minecraft/world/InteractionHand;Z)V",
            at = @At("TAIL")
    )
    private void skyesight$onSwing(
            InteractionHand hand,
            boolean updateSelf,
            CallbackInfo ci
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        SkyesightServerEntityAnimationBroadcaster.sendSwing(
                level,
                entity,
                hand
        );
    }
}