package com.skyeshade.skyesight.mixin.server;

import com.skyeshade.skyesight.server.SkyesightServerBlockEventBroadcaster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelBlockEventMixin {
    @Inject(
            method = "doBlockEvent",
            at = @At("RETURN")
    )
    private void skyesight$onDoBlockEvent(
            BlockEventData event,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) {
            return;
        }

        SkyesightServerBlockEventBroadcaster.send(
                (ServerLevel) (Object) this,
                event.pos(),
                event.paramA(),
                event.paramB()
        );
    }
}