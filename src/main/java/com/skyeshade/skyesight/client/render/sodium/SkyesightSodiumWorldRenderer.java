package com.skyeshade.skyesight.client.render.sodium;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.skyeshade.skyesight.client.render.entity.SkyesightNameTagSuppressor;
import com.skyeshade.skyesight.client.world.SkyesightVisualEntity;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SkyesightSodiumWorldRenderer implements AutoCloseable {
    private static final boolean BUILD_IMMEDIATELY = false;

    private final Minecraft minecraft;
    private final SodiumWorldRenderer renderer;
    private final ChunkTracker tracker;

    private ClientLevel level;

    public SkyesightSodiumWorldRenderer(Minecraft minecraft, ChunkTracker tracker) {
        this.minecraft = minecraft;
        this.tracker = tracker;
        this.renderer = new SodiumWorldRenderer(minecraft);
    }

    public SodiumWorldRenderer renderer() {
        return renderer;
    }

    public void setLevel(ClientLevel level) {
        if (this.level == level) {
            return;
        }

        RenderDevice.enterManagedCode();

        try {
            this.renderer.setLevel(level);
            this.level = level;

            if (level != null) {
                this.renderer.scheduleTerrainUpdate();
            }
        } finally {
            RenderDevice.exitManagedCode();
        }
    }
    public void renderEntities(
            Iterable<SkyesightVisualEntity> entities,
            Camera camera,
            Matrix4f modelMatrix,
            float partialTick
    ) {
        if (this.level == null || this.minecraft.player == null) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelMatrix);

        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();

        ClientLevel previousLevel = this.minecraft.level;

        try {
            this.minecraft.level = this.level;

            try (SkyesightNameTagSuppressor.Scope ignored =
                         SkyesightNameTagSuppressor.suppressOwner(this.minecraft.player.getUUID())) {
                for (SkyesightVisualEntity visualEntity : entities) {
                    visualEntity.applyInterpolated();

                    Entity entity = visualEntity.entity();

                    this.minecraft.getEntityRenderDispatcher().render(
                            entity,
                            entity.getX() - cameraPos.x(),
                            entity.getY() - cameraPos.y(),
                            entity.getZ() - cameraPos.z(),
                            entity.getYRot(),
                            partialTick,
                            poseStack,
                            bufferSource,
                            this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(entity, partialTick)
                    );
                }
            }

            bufferSource.endBatch();
        } finally {
            this.minecraft.level = previousLevel;

            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    public void renderTerrain(
            Camera camera,
            Frustum frustum,
            Matrix4f modelMatrix,
            Matrix4f projectionMatrix
    ) {
        if (level == null || minecraft.player == null) {
            return;
        }

        Viewport viewport = ((ViewportProvider) frustum).sodium$createViewport();
        Vec3 cameraPos = camera.getPosition();

        ChunkRenderMatrices matrices = new ChunkRenderMatrices(
                projectionMatrix,
                modelMatrix
        );

        boolean spectator = minecraft.player.isSpectator();

        RenderDevice.enterManagedCode();

        try (SkyesightSodiumRenderContext.Scope ignored =
                     SkyesightSodiumRenderContext.push(this.tracker)) {

            renderer.setupTerrain(camera, viewport, spectator, BUILD_IMMEDIATELY);

            drawTerrainLayer(RenderType.solid(), matrices, cameraPos);
            drawTerrainLayer(RenderType.translucent(), matrices, cameraPos);
        } finally {
            RenderDevice.exitManagedCode();

            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void renderBlockEntities(
            Camera camera,
            Matrix4f modelMatrix,
            float partialTick
    ) {
        if (this.level == null || this.minecraft.player == null) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelMatrix);

        RenderDevice.enterManagedCode();

        try (SkyesightSodiumRenderContext.Scope ignored =
                     SkyesightSodiumRenderContext.push(this.tracker)) {
            renderer.renderBlockEntities(
                    poseStack,
                    minecraft.renderBuffers(),
                    new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>(),
                    camera,
                    partialTick,
                    null
            );

            minecraft.renderBuffers().bufferSource().endBatch();
        } finally {
            RenderDevice.exitManagedCode();

            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    public void scheduleBlockUpdate(BlockPos pos) {
        if (this.level == null) {
            return;
        }

        int sectionX = pos.getX() >> 4;
        int sectionY = pos.getY() >> 4;
        int sectionZ = pos.getZ() >> 4;

        RenderDevice.enterManagedCode();

        try {
            this.renderer.scheduleRebuildForChunks(
                    sectionX - 1,
                    sectionY - 1,
                    sectionZ - 1,
                    sectionX + 1,
                    sectionY + 1,
                    sectionZ + 1,
                    true
            );

            this.renderer.scheduleTerrainUpdate();
        } finally {
            RenderDevice.exitManagedCode();
        }
    }
    public void scheduleTerrainUpdate() {
        this.renderer.scheduleTerrainUpdate();
    }

    private void drawTerrainLayer(RenderType renderType, ChunkRenderMatrices matrices, Vec3 cameraPos) {
        renderType.setupRenderState();

        try {
            renderer.drawChunkLayer(
                    renderType,
                    matrices,
                    cameraPos.x(),
                    cameraPos.y(),
                    cameraPos.z()
            );
        } finally {
            renderType.clearRenderState();
        }
    }

    public ClientLevel level() {
        return level;
    }


    @Override
    public void close() {
        setLevel(null);
    }


    public void scheduleChunkRebuild(int chunkX, int chunkZ, boolean important) {
        if (this.level == null) {
            return;
        }

        RenderDevice.enterManagedCode();

        try {
            this.renderer.scheduleRebuildForChunks(
                    chunkX,
                    this.level.getMinSection(),
                    chunkZ,
                    chunkX,
                    this.level.getMaxSection() - 1,
                    chunkZ,
                    important
            );

            this.renderer.scheduleTerrainUpdate();
        } finally {
            RenderDevice.exitManagedCode();
        }
    }
}