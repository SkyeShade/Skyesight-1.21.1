package com.skyeshade.skyesight.client.world;

import com.skyeshade.skyesight.network.SkyesightEntitySnapshotPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public final class SkyesightVisualEntityStore {
    private final ClientLevel level;
    private final Map<java.util.UUID, SkyesightVisualEntity> entities = new HashMap<>();

    public SkyesightVisualEntityStore(ClientLevel level) {
        this.level = level;
    }

    public void applySnapshot(SkyesightEntitySnapshotPayload payload) {
        Set<java.util.UUID> seen = new HashSet<>();

        for (SkyesightEntitySnapshotPayload.Entry entry : payload.entities()) {
            seen.add(entry.uuid());

            SkyesightVisualEntity visualEntity = this.entities.get(entry.uuid());

            if (visualEntity == null) {
                Entity entity = createEntity(entry);

                if (entity == null) {
                    continue;
                }

                applyEntityData(entity, entry.entityData());

                visualEntity = new SkyesightVisualEntity(entity, entry);
                this.entities.put(entry.uuid(), visualEntity);
                continue;
            }

            applyEntityData(visualEntity.entity(), entry.entityData());
            visualEntity.acceptSnapshot(entry);
        }

        this.entities.keySet().removeIf(uuid -> !seen.contains(uuid));
    }

    public Iterable<SkyesightVisualEntity> entities() {
        return this.entities.values();
    }

    public void clear() {
        this.entities.clear();
    }

    private Entity createEntity(SkyesightEntitySnapshotPayload.Entry entry) {
        EntityType<?> type = entry.type();
        Entity entity = type.create(this.level);

        if (entity == null) {
            return null;
        }

        entity.setUUID(entry.uuid());
        return entity;
    }

    private static void applyEntityData(
            Entity entity,
            List<SynchedEntityData.DataValue<?>> values
    ) {
        if (values == null || values.isEmpty()) {
            return;
        }

        entity.getEntityData().assignValues(values);
    }
}