package com.skyeshade.skyesight.client.render.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;

public final class SkyesightSodiumRenderContext {
    private static final ThreadLocal<ChunkTracker> ACTIVE_TRACKER = new ThreadLocal<>();

    private SkyesightSodiumRenderContext() {}

    public static Scope push(ChunkTracker tracker) {
        ACTIVE_TRACKER.set(tracker);
        return new Scope();
    }

    public static ChunkTracker currentTracker() {
        return ACTIVE_TRACKER.get();
    }

    public static final class Scope implements AutoCloseable {
        private Scope() {}

        @Override
        public void close() {
            ACTIVE_TRACKER.remove();
        }
    }
}