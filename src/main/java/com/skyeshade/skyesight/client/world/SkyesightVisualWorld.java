package com.skyeshade.skyesight.client.world;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.client.render.sodium.SkyesightSodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public final class SkyesightVisualWorld implements AutoCloseable {
    private final ResourceKey<Level> dimension;
    private final ClientLevel level;
    private final ChunkTracker chunkTracker;

    private final SkyesightSodiumWorldRenderer renderer;
    private final SkyesightRemoteChunkReceiver chunkReceiver;
    private final SkyesightVisualEntityStore entityStore;
    private final SkyesightVisualParticleManager particles;
    public SkyesightVisualWorld(
            ResourceKey<Level> dimension,
            ClientLevel level
    ) {
        this.dimension = dimension;
        this.level = level;
        this.chunkTracker = new ChunkTracker();
        this.chunkReceiver = new SkyesightRemoteChunkReceiver(level, this.chunkTracker);
        this.entityStore = new SkyesightVisualEntityStore(level);
        this.particles = new SkyesightVisualParticleManager(Minecraft.getInstance());
        this.renderer = new SkyesightSodiumWorldRenderer(Minecraft.getInstance(), this.chunkTracker);
        this.renderer.setLevel(level);

    }
    public SkyesightVisualParticleManager particles() {
        return this.particles;
    }
    public SkyesightRemoteChunkReceiver chunkReceiver() {
        return this.chunkReceiver;
    }
    public SkyesightVisualEntityStore entityStore() {
        return this.entityStore;
    }
    public void tick() {
        this.chunkReceiver.tickBlockEntities();
        this.particles.tick();
    }

    public void renderParticles(
            Camera camera,
            Matrix4f modelMatrix,
            Matrix4f projectionMatrix,
            float partialTick
    ) {
        this.particles.render(camera, modelMatrix, projectionMatrix, partialTick);
    }
    public SkyesightSodiumWorldRenderer renderer() {
        return this.renderer;
    }
    public void scheduleBlockUpdate(BlockPos pos) {
        this.renderer.scheduleBlockUpdate(pos);
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
    public void tickBlockEntities() {
        this.chunkReceiver.tickBlockEntities();
    }
    public void renderBlockEntities(
            Camera camera,
            Matrix4f modelMatrix,
            float partialTick
    ) {
        this.renderer.renderBlockEntitiesManual(
                this.chunkReceiver,
                camera,
                modelMatrix,
                partialTick
        );
    }
    public void renderEntities(
            Camera camera,
            Matrix4f modelMatrix,
            float partialTick
    ) {
        this.renderer.renderEntities(
                this.entityStore.entities(),
                camera,
                modelMatrix,
                partialTick
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
        this.entityStore.clear();
        this.renderer.close();
    }
}