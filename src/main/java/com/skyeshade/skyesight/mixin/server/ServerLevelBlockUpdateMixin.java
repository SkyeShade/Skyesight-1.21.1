package com.skyeshade.skyesight.mixin.server;

import com.skyeshade.skyesight.server.SkyesightServerBlockUpdateBroadcaster;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelBlockUpdateMixin {
    @Inject(
            method = "sendBlockUpdated",
            at = @At("TAIL")
    )
    private void skyesight$onBlockUpdated(
            BlockPos pos,
            BlockState oldState,
            BlockState newState,
            int flags,
            CallbackInfo ci
    ) {
        if (oldState == newState || oldState.equals(newState)) {
            return;
        }

        SkyesightServerBlockUpdateBroadcaster.send(
                (ServerLevel) (Object) this,
                pos,
                newState
        );
    }
}