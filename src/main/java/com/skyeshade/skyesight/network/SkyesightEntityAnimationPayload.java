package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.Skyesight;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record SkyesightEntityAnimationPayload(
        ResourceLocation viewId,
        ResourceKey<Level> dimension,
        UUID entityUuid,
        AnimationType animationType,
        InteractionHand hand
) implements CustomPacketPayload {
    public static final Type<SkyesightEntityAnimationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Skyesight.MODID, "entity_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkyesightEntityAnimationPayload> STREAM_CODEC =
            StreamCodec.of(
                    SkyesightEntityAnimationPayload::write,
                    SkyesightEntityAnimationPayload::read
            );

    private static SkyesightEntityAnimationPayload read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation viewId = buffer.readResourceLocation();
        ResourceLocation dimensionId = buffer.readResourceLocation();
        UUID entityUuid = buffer.readUUID();
        AnimationType animationType = buffer.readEnum(AnimationType.class);
        InteractionHand hand = buffer.readEnum(InteractionHand.class);

        return new SkyesightEntityAnimationPayload(
                viewId,
                ResourceKey.create(Registries.DIMENSION, dimensionId),
                entityUuid,
                animationType,
                hand
        );
    }

    private static void write(RegistryFriendlyByteBuf buffer, SkyesightEntityAnimationPayload payload) {
        buffer.writeResourceLocation(payload.viewId());
        buffer.writeResourceLocation(payload.dimension().location());
        buffer.writeUUID(payload.entityUuid());
        buffer.writeEnum(payload.animationType());
        buffer.writeEnum(payload.hand());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum AnimationType {
        SWING_HAND
    }
}