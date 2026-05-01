package com.skyeshade.skyesight.client.world;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.client.render.sodium.SkyesightSodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public final class SkyesightVisualWorld implements AutoCloseable {
    private final ResourceKey<Level> dimension;
    private final ClientLevel level;
    private final ChunkTracker chunkTracker;

    private final SkyesightSodiumWorldRenderer renderer;
    private final SkyesightRemoteChunkReceiver chunkReceiver;

    public SkyesightVisualWorld(
            ResourceKey<Level> dimension,
            ClientLevel level
    ) {
        this.dimension = dimension;
        this.level = level;
        this.chunkTracker = new ChunkTracker();
        this.chunkReceiver = new SkyesightRemoteChunkReceiver(level, this.chunkTracker);
        this.renderer = new SkyesightSodiumWorldRenderer(Minecraft.getInstance(), this.chunkTracker);
        this.renderer.setLevel(level);

    }
    public SkyesightRemoteChunkReceiver chunkReceiver() {
        return this.chunkReceiver;
    }

    public SkyesightSodiumWorldRenderer renderer() {
        return this.renderer;
    }
    public void renderTerrain(
            Camera camera,
            Frustum frustum,
            Matrix4f modelMatrix,
            Matrix4f projectionMatrix,
            int chunkRadius
    ) {
        this.renderer.renderTerrain(
                camera,
                frustum,
                modelMatrix,
                projectionMatrix
        );
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public ClientLevel level() {
        return level;
    }

    public ChunkTracker chunkTracker() {
        return chunkTracker;
    }

    @Override
    public void close() {
        this.renderer.close();
    }
}