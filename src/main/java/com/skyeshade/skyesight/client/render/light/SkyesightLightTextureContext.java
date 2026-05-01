package com.skyeshade.skyesight.client.render.light;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

public final class SkyesightLightTextureContext {
    private static final ThreadLocal<State> CURRENT = new ThreadLocal<>();

    private SkyesightLightTextureContext() {}

    public static Scope push(ClientLevel level, Camera camera) {
        State previous = CURRENT.get();
        CURRENT.set(new State(level, camera));
        return new Scope(previous);
    }

    @Nullable
    public static ClientLevel level() {
        State state = CURRENT.get();
        return state == null ? null : state.level();
    }

    @Nullable
    public static Camera camera() {
        State state = CURRENT.get();
        return state == null ? null : state.camera();
    }

    public static boolean active() {
        return CURRENT.get() != null;
    }

    private record State(ClientLevel level, Camera camera) {}

    public static final class Scope implements AutoCloseable {
        private final State previous;

        private Scope(State previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (this.previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(this.previous);
            }
        }
    }
}