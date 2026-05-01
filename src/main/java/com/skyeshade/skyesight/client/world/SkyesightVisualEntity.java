package com.skyeshade.skyesight.client.world;

import com.skyeshade.skyesight.mixin.common.LivingEntityWalkAnimationAccessor;
import com.skyeshade.skyesight.mixin.common.WalkAnimationStateAccessor;
import com.skyeshade.skyesight.network.SkyesightEntitySnapshotPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.Vec3;

public final class SkyesightVisualEntity {
    private static final long DEFAULT_SNAPSHOT_INTERVAL_MS = 250L;

    private final Entity entity;

    private Vec3 previousPosition;
    private Vec3 currentPosition;

    private float previousYRot;
    private float currentYRot;

    private float previousXRot;
    private float currentXRot;

    private int tickCount;

    private float previousYBodyRot;
    private float currentYBodyRot;

    private float previousYBodyRotO;
    private float currentYBodyRotO;

    private float previousYHeadRot;
    private float currentYHeadRot;

    private float previousYHeadRotO;
    private float currentYHeadRotO;

    private float walkPosition;
    private float walkSpeed;
    private float walkSpeedOld;

    private long snapshotStartMs;
    private long snapshotEndMs;

    private long animationSnapshotMs;
    public SkyesightVisualEntity(
            Entity entity,
            SkyesightEntitySnapshotPayload.Entry entry
    ) {
        long now = System.currentTimeMillis();

        this.entity = entity;
        this.previousPosition = entry.position();
        this.currentPosition = entry.position();

        this.previousYRot = entry.yRot();
        this.currentYRot = entry.yRot();

        this.previousXRot = entry.xRot();
        this.currentXRot = entry.xRot();

        this.snapshotStartMs = now;
        this.snapshotEndMs = now + DEFAULT_SNAPSHOT_INTERVAL_MS;

        this.previousYBodyRot = entry.yBodyRot();
        this.currentYBodyRot = entry.yBodyRot();

        this.previousYBodyRotO = entry.yBodyRotO();
        this.currentYBodyRotO = entry.yBodyRotO();

        this.previousYHeadRot = entry.yHeadRot();
        this.currentYHeadRot = entry.yHeadRot();

        this.previousYHeadRotO = entry.yHeadRotO();
        this.currentYHeadRotO = entry.yHeadRotO();
        acceptAnimation(entry);
        applyInterpolated();
    }

    public Entity entity() {
        return this.entity;
    }

    public void acceptSnapshot(SkyesightEntitySnapshotPayload.Entry entry) {
        long now = System.currentTimeMillis();

        this.previousPosition = interpolatedPosition(now);
        this.previousYRot = interpolatedYRot(now);
        this.previousXRot = interpolatedXRot(now);

        this.currentPosition = entry.position();
        this.currentYRot = entry.yRot();
        this.currentXRot = entry.xRot();

        this.snapshotStartMs = now;
        this.snapshotEndMs = now + DEFAULT_SNAPSHOT_INTERVAL_MS;

        acceptAnimation(entry);
    }

    public void applyInterpolated() {
        long now = System.currentTimeMillis();

        Vec3 position = interpolatedPosition(now);
        float yRot = interpolatedYRot(now);
        float xRot = interpolatedXRot(now);

        this.entity.tickCount = this.tickCount + Mth.floor(elapsedAnimationTicks());
        this.entity.setPos(position);

        this.entity.xo = position.x();
        this.entity.yo = position.y();
        this.entity.zo = position.z();

        this.entity.setYRot(yRot);
        this.entity.setXRot(xRot);

        this.entity.yRotO = yRot;
        this.entity.xRotO = xRot;

        if (this.entity instanceof LivingEntity livingEntity) {
            float elapsedTicks = elapsedAnimationTicks();

            float alpha = interpolationAlpha(System.currentTimeMillis());

            livingEntity.yBodyRot = lerpDegrees(alpha, this.previousYBodyRot, this.currentYBodyRot);
            livingEntity.yBodyRotO = lerpDegrees(alpha, this.previousYBodyRotO, this.currentYBodyRotO);
            livingEntity.yHeadRot = lerpDegrees(alpha, this.previousYHeadRot, this.currentYHeadRot);
            livingEntity.yHeadRotO = lerpDegrees(alpha, this.previousYHeadRotO, this.currentYHeadRotO);

            WalkAnimationState walkAnimation =
                    ((LivingEntityWalkAnimationAccessor) livingEntity).skyesight$getWalkAnimation();

            WalkAnimationStateAccessor accessor =
                    (WalkAnimationStateAccessor) walkAnimation;

            float extrapolatedWalkPosition =
                    this.walkPosition + this.walkSpeed * elapsedTicks;

            accessor.skyesight$setPosition(extrapolatedWalkPosition);
            accessor.skyesight$setSpeed(this.walkSpeed);
            accessor.skyesight$setSpeedOld(this.walkSpeedOld);
        }
    }
    private float elapsedAnimationTicks() {
        long now = System.currentTimeMillis();
        long elapsedMs = Math.max(0L, now - this.animationSnapshotMs);

        return elapsedMs / 50.0F;
    }
    private void acceptAnimation(SkyesightEntitySnapshotPayload.Entry entry) {
        this.animationSnapshotMs = System.currentTimeMillis();

        this.tickCount = entry.tickCount();

        this.previousYBodyRot = this.currentYBodyRot;
        this.previousYBodyRotO = this.currentYBodyRotO;
        this.previousYHeadRot = this.currentYHeadRot;
        this.previousYHeadRotO = this.currentYHeadRotO;

        this.currentYBodyRot = entry.yBodyRot();
        this.currentYBodyRotO = entry.yBodyRotO();
        this.currentYHeadRot = entry.yHeadRot();
        this.currentYHeadRotO = entry.yHeadRotO();

        this.walkPosition = entry.walkPosition();
        this.walkSpeed = entry.walkSpeed();
        this.walkSpeedOld = entry.walkSpeedOld();
    }

    private Vec3 interpolatedPosition(long nowMs) {
        return this.previousPosition.lerp(this.currentPosition, interpolationAlpha(nowMs));
    }

    private float interpolatedYRot(long nowMs) {
        return lerpDegrees(interpolationAlpha(nowMs), this.previousYRot, this.currentYRot);
    }

    private float interpolatedXRot(long nowMs) {
        return lerpDegrees(interpolationAlpha(nowMs), this.previousXRot, this.currentXRot);
    }

    private float interpolationAlpha(long nowMs) {
        long duration = Math.max(1L, this.snapshotEndMs - this.snapshotStartMs);
        return Mth.clamp((float) (nowMs - this.snapshotStartMs) / (float) duration, 0.0F, 1.0F);
    }

    private static float lerpDegrees(float alpha, float from, float to) {
        float delta = Mth.wrapDegrees(to - from);
        return from + alpha * delta;
    }
}