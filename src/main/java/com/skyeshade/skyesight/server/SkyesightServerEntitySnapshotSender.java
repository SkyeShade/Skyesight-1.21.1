package com.skyeshade.skyesight.server;

import com.skyeshade.skyesight.mixin.common.LivingEntityWalkAnimationAccessor;
import com.skyeshade.skyesight.mixin.common.WalkAnimationStateAccessor;
import com.skyeshade.skyesight.network.SkyesightEntitySnapshotPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class SkyesightServerEntitySnapshotSender {
    private static final int MAX_ENTITIES_PER_SNAPSHOT = 128;

    private SkyesightServerEntitySnapshotSender() {}

    public static void sendSnapshot(
            ServerPlayer player,
            SkyesightServerViewTracker.ViewWatch watch,
            ServerLevel level
    ) {
        int radiusBlocks = watch.radius() * 16 + 16;
        double centerX = watch.centerChunkX() * 16.0D + 8.0D;
        double centerZ = watch.centerChunkZ() * 16.0D + 8.0D;

        AABB area = new AABB(
                centerX - radiusBlocks,
                level.getMinBuildHeight(),
                centerZ - radiusBlocks,
                centerX + radiusBlocks,
                level.getMaxBuildHeight(),
                centerZ + radiusBlocks
        );

        List<SkyesightEntitySnapshotPayload.Entry> entries = new ArrayList<>();

        for (Entity entity : level.getEntities(player, area, ignored -> true)) {
            if (entries.size() >= MAX_ENTITIES_PER_SNAPSHOT) {
                break;
            }

            boolean living = entity instanceof LivingEntity;

            float yBodyRot = entity.getYRot();
            float yBodyRotO = entity.getYRot();
            float yHeadRot = entity.getYRot();
            float yHeadRotO = entity.getYRot();

            float walkPosition = 0.0F;
            float walkSpeed = 0.0F;
            float walkSpeedOld = 0.0F;

            if (entity instanceof LivingEntity livingEntity) {
                yBodyRot = livingEntity.yBodyRot;
                yBodyRotO = livingEntity.yBodyRotO;
                yHeadRot = livingEntity.yHeadRot;
                yHeadRotO = livingEntity.yHeadRotO;

                WalkAnimationState walkAnimation =
                        ((LivingEntityWalkAnimationAccessor) livingEntity).skyesight$getWalkAnimation();

                WalkAnimationStateAccessor walkAccessor =
                        (WalkAnimationStateAccessor) walkAnimation;

                walkPosition = walkAccessor.skyesight$getPosition();
                walkSpeed = walkAccessor.skyesight$getSpeed();
                walkSpeedOld = walkAccessor.skyesight$getSpeedOld();
            }

            List<SynchedEntityData.DataValue<?>> entityData =
                    entity.getEntityData().getNonDefaultValues();

            if (entityData == null) {
                entityData = List.of();
            }

            entries.add(new SkyesightEntitySnapshotPayload.Entry(
                    entity.getUUID(),
                    entity.getType(),
                    entity.position(),
                    entity.getYRot(),
                    entity.getXRot(),
                    living,
                    entity.tickCount,
                    yBodyRot,
                    yBodyRotO,
                    yHeadRot,
                    yHeadRotO,
                    walkPosition,
                    walkSpeed,
                    walkSpeedOld,
                    entityData
            ));
        }

        PacketDistributor.sendToPlayer(
                player,
                new SkyesightEntitySnapshotPayload(
                        watch.viewId(),
                        level.dimension(),
                        entries
                )
        );
    }
}