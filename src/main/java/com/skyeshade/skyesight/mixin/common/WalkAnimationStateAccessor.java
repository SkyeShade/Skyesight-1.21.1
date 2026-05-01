package com.skyeshade.skyesight.mixin.common;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("position")
    float skyesight$getPosition();

    @Accessor("position")
    void skyesight$setPosition(float position);

    @Accessor("speed")
    float skyesight$getSpeed();

    @Accessor("speed")
    void skyesight$setSpeed(float speed);

    @Accessor("speedOld")
    float skyesight$getSpeedOld();

    @Accessor("speedOld")
    void skyesight$setSpeedOld(float speedOld);
}