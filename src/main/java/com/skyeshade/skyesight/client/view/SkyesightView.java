package com.skyeshade.skyesight.client.view;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skyeshade.skyesight.api.SkyesightRenderMode;
import com.skyeshade.skyesight.api.SkyesightViewCamera;
import com.skyeshade.skyesight.api.SkyesightViewHandle;
import com.skyeshade.skyesight.api.SkyesightViewSpec;
import com.skyeshade.skyesight.client.SkyesightClientChunkRequester;
import com.skyeshade.skyesight.client.render.SkyesightCameraMatrices;
import com.skyeshade.skyesight.client.render.SkyesightFrustumFactory;
import com.skyeshade.skyesight.client.render.env.SkyesightEnvironmentRendererSelector;
import com.skyeshade.skyesight.client.render.fog.SkyesightFogRenderer;
import com.skyeshade.skyesight.client.render.light.SkyesightLightTextureUpdater;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class SkyesightView implements SkyesightViewHandle {
    private final ResourceLocation id;
    private final SkyesightInternalCamera camera;
    private final SkyesightRenderMode renderMode;

    private ResourceKey<Level> dimension;
    private int renderDistanceChunks;
    private int width;
    private int height;
    private TextureTarget target;

    public SkyesightView(SkyesightViewSpec spec) {
        this.id = spec.id();
        this.dimension = spec.dimension();
        this.renderDistanceChunks = spec.renderDistanceChunks();
        this.width = spec.width();
        this.height = spec.height();
        this.renderMode = spec.renderMode();
        this.camera = new SkyesightInternalCamera();

        this.camera.setPosition(spec.position());
        this.camera.setRotation(spec.rotation());

        resize(this.width, this.height);
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    @Override
    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    @Override
    public SkyesightRenderMode renderMode() {
        return this.renderMode;
    }


    @Override
    public SkyesightViewCamera camera() {
        return this.camera;
    }

    @Override
    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Override
    public void setRenderDistance(int renderDistanceChunks) {
        this.renderDistanceChunks = Math.max(1, renderDistanceChunks);
    }

    @Override
    public int renderDistanceChunks() {
        return this.renderDistanceChunks;
    }

    @Override
    public void resize(int width, int height) {
        width = Math.max(1, width);
        height = Math.max(1, height);

        if (this.target != null && this.width == width && this.height == height) {
            return;
        }

        this.width = width;
        this.height = height;

        if (this.target != null) {
            this.target.destroyBuffers();
        }

        this.target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public TextureTarget outputTarget() {
        return this.target;
    }

    @Override
    public int colorTextureId() {
        return this.target.getColorTextureId();
    }

    @Override
    public void render(float partialTick, Matrix4f projectionMatrix) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null || this.target == null) {
            return;
        }

        SkyesightVisualWorld visualWorld =
                SkyesightVisualWorldManager.getOrCreate(this.dimension);

        if (visualWorld == null) {
            return;
        }


        SkyesightClientChunkRequester.requestChunksFor(
                this.dimension,
                this.camera.minecraftCamera(),
                this.renderDistanceChunks
        );

        visualWorld.level().setDayTime(minecraft.level.getDayTime());
        visualWorld.level().setGameTime(minecraft.level.getGameTime());

        Matrix4f modelMatrix = SkyesightCameraMatrices.createModelView(this.camera.minecraftCamera());

        Frustum frustum = SkyesightFrustumFactory.create(
                this.camera.minecraftCamera(),
                modelMatrix,
                projectionMatrix
        );

        this.target.bindWrite(true);
        RenderSystem.viewport(0, 0, this.width, this.height);

        Vec3 skyColor = visualWorld.level().getSkyColor(
                this.camera.minecraftCamera().getPosition(),
                partialTick
        );

        RenderSystem.clearColor(
                (float) skyColor.x(),
                (float) skyColor.y(),
                (float) skyColor.z(),
                1.0F
        );

        RenderSystem.clear(
                GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT,
                Minecraft.ON_OSX
        );

        int fogDistanceChunks = this.renderDistanceChunks;

        SkyesightFogRenderer.setupForSky(
                visualWorld.level(),
                this.camera.minecraftCamera(),
                partialTick,
                fogDistanceChunks
        );

        SkyesightEnvironmentRendererSelector.get().renderSky(
                visualWorld.level(),
                this.camera.minecraftCamera(),
                modelMatrix,
                projectionMatrix,
                partialTick
        );

        SkyesightLightTextureUpdater.updateFor(
                visualWorld.level(),
                this.camera.minecraftCamera(),
                partialTick
        );

        SkyesightFogRenderer.setupForTerrain(
                visualWorld.level(),
                this.camera.minecraftCamera(),
                partialTick,
                fogDistanceChunks
        );

        visualWorld.renderTerrain(
                this.camera.minecraftCamera(),
                frustum,
                modelMatrix,
                projectionMatrix,
                this.renderDistanceChunks
        );
        visualWorld.renderBlockEntities(
                this.camera.minecraftCamera(),
                modelMatrix,
                partialTick
        );
        visualWorld.renderEntities(
                this.camera.minecraftCamera(),
                modelMatrix,
                partialTick
        );
        SkyesightFogRenderer.clear();
        SkyesightLightTextureUpdater.restoreMain(partialTick);

        minecraft.getMainRenderTarget().bindWrite(true);
        RenderSystem.viewport(
                0,
                0,
                minecraft.getWindow().getWidth(),
                minecraft.getWindow().getHeight()
        );

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


    }

    @Override
    public void close() {
        if (this.target != null) {
            this.target.destroyBuffers();
            this.target = null;
        }

    }
}