package com.skyeshade.skyesight.client.world;

import com.skyeshade.skyesight.mixin.common.LivingEntityAnimationAccessor;
import com.skyeshade.skyesight.mixin.common.LivingEntityWalkAnimationAccessor;
import com.skyeshade.skyesight.mixin.common.WalkAnimationStateAccessor;
import com.skyeshade.skyesight.network.SkyesightEntitySnapshotPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkyesightVisualEntity {
    private static final long DEFAULT_SNAPSHOT_INTERVAL_MS = 100L;

    private final Entity entity;

    private Vec3 previousPosition;
    private Vec3 currentPosition;

    private float previousYRot;
    private float currentYRot;

    private float previousXRot;
    private float currentXRot;

    private float previousYBodyRot;
    private float currentYBodyRot;

    private float previousYBodyRotO;
    private float currentYBodyRotO;

    private float previousYHeadRot;
    private float currentYHeadRot;

    private float previousYHeadRotO;
    private float currentYHeadRotO;

    private int tickCount;

    private float walkPosition;
    private float walkSpeed;
    private float walkSpeedOld;

    private long snapshotStartMs;
    private long snapshotEndMs;
    private long animationSnapshotMs;

    private Vec3 deltaMovement;
    private boolean onGround;
    private float fallDistance;


    private float playerTargetWalkSpeed;
    private float playerWalkSpeed;
    private float playerWalkPosition;
    private long playerWalkAnimationMs;

    private float playerBodyYaw;
    private float playerBodyYawO;
    private float playerTargetBodyYaw;

    private int hurtTime;
    private int hurtDuration;
    private int deathTime;


    private boolean localSwinging;
    private InteractionHand localSwingingArm = InteractionHand.MAIN_HAND;
    private long localSwingStartMs;
    private long localSwingLastMs;
    private int localSwingDurationTicks = 6;
    private float localAttackAnim;
    private float localOAttackAnim;
    public SkyesightVisualEntity(
            Entity entity,
            SkyesightEntitySnapshotPayload.Entry entry
    ) {
        long now = System.currentTimeMillis();

        this.entity = entity;

        this.previousPosition = entry.position();
        this.currentPosition = entry.position();
        this.deltaMovement = entry.deltaMovement();
        this.onGround = entry.onGround();
        this.fallDistance = entry.fallDistance();
        this.previousYRot = entry.yRot();
        this.currentYRot = entry.yRot();

        this.previousXRot = entry.xRot();
        this.currentXRot = entry.xRot();

        this.previousYBodyRot = entry.yBodyRot();
        this.currentYBodyRot = entry.yBodyRot();

        this.previousYBodyRotO = entry.yBodyRotO();
        this.currentYBodyRotO = entry.yBodyRotO();

        this.previousYHeadRot = entry.yHeadRot();
        this.currentYHeadRot = entry.yHeadRot();

        this.previousYHeadRotO = entry.yHeadRotO();
        this.currentYHeadRotO = entry.yHeadRotO();

        this.snapshotStartMs = now;
        this.snapshotEndMs = now + DEFAULT_SNAPSHOT_INTERVAL_MS;
        this.animationSnapshotMs = now;

        this.playerTargetWalkSpeed = 0.0F;
        this.playerWalkSpeed = 0.0F;
        this.playerWalkPosition = entry.walkPosition();
        this.playerWalkAnimationMs = now;
        this.playerBodyYaw = entry.yBodyRot();
        this.playerBodyYawO = entry.yBodyRotO();
        this.playerTargetBodyYaw = entry.yBodyRot();
        acceptAnimation(entry);
        updatePlayerMovementAnimation(entry.position(), entry.position());
        applyInterpolated();
    }

    public Entity entity() {
        return this.entity;
    }

    public void acceptSnapshot(SkyesightEntitySnapshotPayload.Entry entry) {
        long now = System.currentTimeMillis();

        Vec3 interpolatedPosition = interpolatedPosition(now);

        this.previousPosition = interpolatedPosition;
        this.deltaMovement = entry.deltaMovement();
        this.onGround = entry.onGround();
        this.fallDistance = entry.fallDistance();
        this.previousYRot = interpolatedYRot(now);
        this.previousXRot = interpolatedXRot(now);

        this.previousYBodyRot = interpolatedYBodyRot(now);
        this.previousYBodyRotO = interpolatedYBodyRotO(now);
        this.previousYHeadRot = interpolatedYHeadRot(now);
        this.previousYHeadRotO = interpolatedYHeadRotO(now);

        this.currentPosition = entry.position();
        this.currentYRot = entry.yRot();
        this.currentXRot = entry.xRot();

        this.currentYBodyRot = entry.yBodyRot();
        this.currentYBodyRotO = entry.yBodyRotO();
        this.currentYHeadRot = entry.yHeadRot();
        this.currentYHeadRotO = entry.yHeadRotO();

        this.snapshotStartMs = now;
        this.snapshotEndMs = now + DEFAULT_SNAPSHOT_INTERVAL_MS;

        updatePlayerMovementAnimation(interpolatedPosition, entry.position());
        acceptAnimation(entry);
    }

    public void applyInterpolated() {
        long now = System.currentTimeMillis();
        float alpha = interpolationAlpha(now);

        Vec3 position = interpolatedPosition(now);

        float yRot = interpolatedYRot(now);
        float xRot = interpolatedXRot(now);

        float elapsedTicks = elapsedAnimationTicks();

        this.entity.tickCount = this.tickCount + Mth.floor(elapsedTicks);

        this.entity.setPos(position);
        this.entity.setDeltaMovement(this.deltaMovement);
        this.entity.setOnGround(this.onGround);
        this.entity.fallDistance = this.fallDistance;
        this.entity.xo = position.x();
        this.entity.yo = position.y();
        this.entity.zo = position.z();

        this.entity.setYRot(yRot);
        this.entity.setXRot(xRot);
        this.entity.yRotO = yRot;
        this.entity.xRotO = xRot;

        if (this.entity instanceof LivingEntity livingEntity) {
            if (this.entity instanceof Player) {
                stepPlayerBodyYaw();

                float bodyYaw = lerpDegrees(alpha, this.playerBodyYawO, this.playerBodyYaw);

                livingEntity.yBodyRotO = this.playerBodyYawO;
                livingEntity.yBodyRot = bodyYaw;


                livingEntity.yHeadRot = lerpDegrees(alpha, this.previousYHeadRot, this.currentYHeadRot);
                livingEntity.yHeadRotO = lerpDegrees(alpha, this.previousYHeadRotO, this.currentYHeadRotO);
            } else {
                livingEntity.yBodyRot = lerpDegrees(alpha, this.previousYBodyRot, this.currentYBodyRot);
                livingEntity.yBodyRotO = lerpDegrees(alpha, this.previousYBodyRotO, this.currentYBodyRotO);
                livingEntity.yHeadRot = lerpDegrees(alpha, this.previousYHeadRot, this.currentYHeadRot);
                livingEntity.yHeadRotO = lerpDegrees(alpha, this.previousYHeadRotO, this.currentYHeadRotO);
            }
            applyLivingAnimationState(livingEntity);
            applyWalkAnimation(livingEntity, elapsedTicks);
        }
    }
    private void applyLivingAnimationState(LivingEntity livingEntity) {
        float elapsedTicks = elapsedAnimationTicks();
        int extrapolatedTicks = Mth.floor(elapsedTicks);

        livingEntity.hurtDuration = this.hurtDuration;
        livingEntity.hurtTime = Math.max(0, this.hurtTime - extrapolatedTicks);
        livingEntity.deathTime = this.deathTime > 0
                ? this.deathTime + extrapolatedTicks
                : 0;

        LivingEntityAnimationAccessor accessor =
                (LivingEntityAnimationAccessor) livingEntity;

        if (this.localSwinging) {
            applyLocalSwingAnimation(accessor);
            return;
        }

        accessor.skyesight$setSwinging(false);
        accessor.skyesight$setSwingTime(0);
        accessor.skyesight$setOAttackAnim(0.0F);
        accessor.skyesight$setAttackAnim(0.0F);
    }
    private void applyLocalSwingAnimation(LivingEntityAnimationAccessor accessor) {
        long now = System.currentTimeMillis();

        float elapsedTicks =
                (now - this.localSwingStartMs) / 50.0F;

        float previousElapsedTicks =
                (this.localSwingLastMs - this.localSwingStartMs) / 50.0F;

        this.localSwingLastMs = now;

        float progress = Mth.clamp(
                elapsedTicks / (float) this.localSwingDurationTicks,
                0.0F,
                1.0F
        );

        float previousProgress = Mth.clamp(
                previousElapsedTicks / (float) this.localSwingDurationTicks,
                0.0F,
                1.0F
        );

        this.localOAttackAnim = previousProgress;
        this.localAttackAnim = progress;

        accessor.skyesight$setSwinging(progress < 1.0F);
        accessor.skyesight$setSwingingArm(this.localSwingingArm);
        accessor.skyesight$setSwingTime(Mth.floor(elapsedTicks));
        accessor.skyesight$setOAttackAnim(this.localOAttackAnim);
        accessor.skyesight$setAttackAnim(this.localAttackAnim);

        if (progress >= 1.0F) {
            this.localSwinging = false;
            accessor.skyesight$setSwinging(false);
            accessor.skyesight$setSwingTime(0);
            accessor.skyesight$setOAttackAnim(0.0F);
            accessor.skyesight$setAttackAnim(0.0F);
        }
    }
    private void applyWalkAnimation(LivingEntity livingEntity, float elapsedTicks) {
        WalkAnimationState walkAnimation =
                ((LivingEntityWalkAnimationAccessor) livingEntity).skyesight$getWalkAnimation();

        WalkAnimationStateAccessor accessor =
                (WalkAnimationStateAccessor) walkAnimation;

        if (this.entity instanceof Player) {
            float oldSpeed = this.playerWalkSpeed;

            stepPlayerWalkAnimation();

            accessor.skyesight$setPosition(this.playerWalkPosition);
            accessor.skyesight$setSpeed(this.playerWalkSpeed);
            accessor.skyesight$setSpeedOld(oldSpeed);
            return;
        }

        float extrapolatedWalkPosition =
                this.walkPosition + this.walkSpeed * elapsedTicks;

        accessor.skyesight$setPosition(extrapolatedWalkPosition);
        accessor.skyesight$setSpeed(this.walkSpeed);
        accessor.skyesight$setSpeedOld(this.walkSpeedOld);
    }
    private void stepPlayerWalkAnimation() {
        long now = System.currentTimeMillis();
        float elapsedTicks = Math.min((now - this.playerWalkAnimationMs) / 50.0F, 4.0F);

        if (elapsedTicks <= 0.0F) {
            return;
        }

        this.playerWalkAnimationMs = now;

        float smoothing = 1.0F - (float) Math.pow(0.6F, elapsedTicks);

        this.playerWalkSpeed = Mth.lerp(
                smoothing,
                this.playerWalkSpeed,
                this.playerTargetWalkSpeed
        );

        this.playerWalkPosition += this.playerWalkSpeed * elapsedTicks;
    }

    private void acceptAnimation(SkyesightEntitySnapshotPayload.Entry entry) {
        this.animationSnapshotMs = System.currentTimeMillis();

        this.tickCount = entry.tickCount();

        this.walkPosition = entry.walkPosition();
        this.walkSpeed = entry.walkSpeed();
        this.walkSpeedOld = entry.walkSpeedOld();
        this.hurtTime = entry.hurtTime();
        this.hurtDuration = entry.hurtDuration();
        this.deathTime = entry.deathTime();

    }
    public void triggerSwing(InteractionHand hand) {
        long now = System.currentTimeMillis();

        this.localSwinging = true;
        this.localSwingingArm = hand;
        this.localSwingStartMs = now;
        this.localSwingLastMs = now;
        this.localAttackAnim = 0.0F;
        this.localOAttackAnim = 0.0F;

        if (this.entity instanceof LivingEntity livingEntity) {
            LivingEntityAnimationAccessor accessor =
                    (LivingEntityAnimationAccessor) livingEntity;

            accessor.skyesight$setSwinging(true);
            accessor.skyesight$setSwingingArm(hand);
            accessor.skyesight$setSwingTime(0);
            accessor.skyesight$setOAttackAnim(0.0F);
            accessor.skyesight$setAttackAnim(0.0F);
        }
    }
    private void updatePlayerMovementAnimation(Vec3 from, Vec3 to) {
        double dx = to.x() - from.x();
        double dz = to.z() - from.z();

        double velocityDx = this.deltaMovement.x();
        double velocityDz = this.deltaMovement.z();

        double motionX = Math.abs(dx) > Math.abs(velocityDx) ? dx : velocityDx;
        double motionZ = Math.abs(dz) > Math.abs(velocityDz) ? dz : velocityDz;

        float horizontalDistance = Mth.sqrt((float) (motionX * motionX + motionZ * motionZ));

        float snapshotTicks = DEFAULT_SNAPSHOT_INTERVAL_MS / 50.0F;
        float blocksPerTick = horizontalDistance / Math.max(1.0F, snapshotTicks);

        float targetSpeed = Mth.clamp(blocksPerTick * 4.0F, 0.0F, 1.0F);

        this.playerTargetWalkSpeed = Mth.lerp(
                0.35F,
                this.playerTargetWalkSpeed,
                targetSpeed
        );

        if (horizontalDistance > 0.003F) {
            float movementYaw =
                    (float) (Mth.atan2(motionZ, motionX) * (180.0F / Math.PI)) - 90.0F;

            this.playerTargetBodyYaw = clampYawAroundHead(
                    movementYaw,
                    this.currentYHeadRot,
                    45.0F
            );
        } else {
            this.playerTargetBodyYaw = this.currentYBodyRot;
        }
    }
    private void stepPlayerBodyYaw() {
        this.playerBodyYawO = this.playerBodyYaw;

        this.playerBodyYaw = approachDegrees(
                this.playerBodyYaw,
                this.playerTargetBodyYaw,
                2.0F
        );
    }
    private static float clampYawAroundHead(float bodyYaw, float headYaw, float maxDifference) {
        float delta = Mth.wrapDegrees(bodyYaw - headYaw);
        float clampedDelta = Mth.clamp(delta, -maxDifference, maxDifference);
        return headYaw + clampedDelta;
    }

    private static float approachDegrees(float current, float target, float maxStep) {
        float delta = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(delta, -maxStep, maxStep);
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

    private float interpolatedYBodyRot(long nowMs) {
        return lerpDegrees(interpolationAlpha(nowMs), this.previousYBodyRot, this.currentYBodyRot);
    }

    private float interpolatedYBodyRotO(long nowMs) {
        return lerpDegrees(interpolationAlpha(nowMs), this.previousYBodyRotO, this.currentYBodyRotO);
    }

    private float interpolatedYHeadRot(long nowMs) {
        return lerpDegrees(interpolationAlpha(nowMs), this.previousYHeadRot, this.currentYHeadRot);
    }

    private float interpolatedYHeadRotO(long nowMs) {
        return lerpDegrees(interpolationAlpha(nowMs), this.previousYHeadRotO, this.currentYHeadRotO);
    }

    private float interpolationAlpha(long nowMs) {
        long duration = Math.max(1L, this.snapshotEndMs - this.snapshotStartMs);
        return Mth.clamp((float) (nowMs - this.snapshotStartMs) / (float) duration, 0.0F, 1.0F);
    }

    private float elapsedAnimationTicks() {
        long now = System.currentTimeMillis();
        long elapsedMs = Math.max(0L, now - this.animationSnapshotMs);

        return elapsedMs / 50.0F;
    }

    private static float lerpDegrees(float alpha, float from, float to) {
        float delta = Mth.wrapDegrees(to - from);
        return from + alpha * delta;
    }
}