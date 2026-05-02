package com.skyeshade.skyesight.server;

import com.skyeshade.skyesight.mixin.common.SynchedEntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class SkyesightEntityDataPacker {
    private SkyesightEntityDataPacker() {}

    public static List<SynchedEntityData.DataValue<?>> packAll(Entity entity) {
        SynchedEntityData data = entity.getEntityData();

        SynchedEntityData.DataItem<?>[] items =
                ((SynchedEntityDataAccessor) data).skyesight$getItemsById();

        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>(items.length);

        for (SynchedEntityData.DataItem<?> item : items) {
            values.add(item.value());
        }

        return values;
    }
}