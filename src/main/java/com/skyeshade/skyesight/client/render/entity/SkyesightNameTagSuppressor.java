package com.skyeshade.skyesight.client.render.entity;

import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class SkyesightNameTagSuppressor {
    private static final ThreadLocal<UUID> SUPPRESSED_PLAYER_UUID = new ThreadLocal<>();

    private SkyesightNameTagSuppressor() {}

    public static Scope suppressOwner(UUID playerUuid) {
        UUID previous = SUPPRESSED_PLAYER_UUID.get();
        SUPPRESSED_PLAYER_UUID.set(playerUuid);
        return new Scope(previous);
    }

    public static boolean shouldSuppressName(Entity entity) {
        UUID suppressedUuid = SUPPRESSED_PLAYER_UUID.get();

        return suppressedUuid != null
                && entity != null
                && suppressedUuid.equals(entity.getUUID());
    }

    public static final class Scope implements AutoCloseable {
        private final UUID previous;

        private Scope(UUID previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (this.previous == null) {
                SUPPRESSED_PLAYER_UUID.remove();
            } else {
                SUPPRESSED_PLAYER_UUID.set(this.previous);
            }
        }
    }
}