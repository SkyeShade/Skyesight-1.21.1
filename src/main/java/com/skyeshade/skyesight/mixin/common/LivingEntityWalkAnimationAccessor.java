package com.skyeshade.skyesight.mixin.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityWalkAnimationAccessor {
    @Accessor("walkAnimation")
    WalkAnimationState skyesight$getWalkAnimation();
}