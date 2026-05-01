package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.Skyesight;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SkyesightEntitySnapshotPayload(
        ResourceLocation viewId,
        ResourceKey<Level> dimension,
        List<Entry> entities
) implements CustomPacketPayload {
    public static final Type<SkyesightEntitySnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Skyesight.MODID, "entity_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkyesightEntitySnapshotPayload> STREAM_CODEC =
            StreamCodec.of(
                    SkyesightEntitySnapshotPayload::write,
                    SkyesightEntitySnapshotPayload::read
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, EntityType<?>> ENTITY_TYPE_CODEC =
            ByteBufCodecs.registry(Registries.ENTITY_TYPE);

    private static SkyesightEntitySnapshotPayload read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation viewId = buffer.readResourceLocation();
        ResourceLocation dimensionId = buffer.readResourceLocation();

        int count = buffer.readVarInt();
        List<Entry> entities = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            UUID uuid = buffer.readUUID();
            EntityType<?> type = ENTITY_TYPE_CODEC.decode(buffer);

            double x = buffer.readDouble();
            double y = buffer.readDouble();
            double z = buffer.readDouble();

            float yRot = buffer.readFloat();
            float xRot = buffer.readFloat();

            boolean living = buffer.readBoolean();
            int tickCount = buffer.readVarInt();

            float yBodyRot = buffer.readFloat();
            float yBodyRotO = buffer.readFloat();
            float yHeadRot = buffer.readFloat();
            float yHeadRotO = buffer.readFloat();

            float walkPosition = buffer.readFloat();
            float walkSpeed = buffer.readFloat();
            float walkSpeedOld = buffer.readFloat();

            List<SynchedEntityData.DataValue<?>> entityData = readEntityData(buffer);

            entities.add(new Entry(
                    uuid,
                    type,
                    new Vec3(x, y, z),
                    yRot,
                    xRot,
                    living,
                    tickCount,
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

        return new SkyesightEntitySnapshotPayload(
                viewId,
                ResourceKey.create(Registries.DIMENSION, dimensionId),
                entities
        );
    }

    private static void write(RegistryFriendlyByteBuf buffer, SkyesightEntitySnapshotPayload payload) {
        buffer.writeResourceLocation(payload.viewId());
        buffer.writeResourceLocation(payload.dimension().location());

        buffer.writeVarInt(payload.entities().size());

        for (Entry entity : payload.entities()) {
            buffer.writeUUID(entity.uuid());
            ENTITY_TYPE_CODEC.encode(buffer, entity.type());

            buffer.writeDouble(entity.position().x());
            buffer.writeDouble(entity.position().y());
            buffer.writeDouble(entity.position().z());

            buffer.writeFloat(entity.yRot());
            buffer.writeFloat(entity.xRot());

            buffer.writeBoolean(entity.living());
            buffer.writeVarInt(entity.tickCount());

            buffer.writeFloat(entity.yBodyRot());
            buffer.writeFloat(entity.yBodyRotO());
            buffer.writeFloat(entity.yHeadRot());
            buffer.writeFloat(entity.yHeadRotO());

            buffer.writeFloat(entity.walkPosition());
            buffer.writeFloat(entity.walkSpeed());
            buffer.writeFloat(entity.walkSpeedOld());

            writeEntityData(buffer, entity.entityData());
        }
    }

    private static List<SynchedEntityData.DataValue<?>> readEntityData(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int id = buffer.readUnsignedByte();
            values.add(SynchedEntityData.DataValue.read(buffer, id));
        }

        return values;
    }

    private static void writeEntityData(
            RegistryFriendlyByteBuf buffer,
            List<SynchedEntityData.DataValue<?>> values
    ) {
        if (values == null || values.isEmpty()) {
            buffer.writeVarInt(0);
            return;
        }

        buffer.writeVarInt(values.size());

        for (SynchedEntityData.DataValue<?> value : values) {
            value.write(buffer);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(
            UUID uuid,
            EntityType<?> type,
            Vec3 position,
            float yRot,
            float xRot,
            boolean living,
            int tickCount,
            float yBodyRot,
            float yBodyRotO,
            float yHeadRot,
            float yHeadRotO,
            float walkPosition,
            float walkSpeed,
            float walkSpeedOld,
            List<SynchedEntityData.DataValue<?>> entityData
    ) {}
}