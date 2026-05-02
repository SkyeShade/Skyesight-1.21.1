package com.skyeshade.skyesight.client.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.skyeshade.skyesight.network.SkyesightParticlePayload;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class SkyesightVisualParticleManager {
    private final Minecraft minecraft;

    public SkyesightVisualParticleManager(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addParticle(SkyesightParticlePayload payload) {
        if (this.minecraft.level == null) {
            return;
        }

        if (payload.count() == 0) {
            this.minecraft.particleEngine.createParticle(
                    payload.particle(),
                    payload.x(),
                    payload.y(),
                    payload.z(),
                    payload.xDist(),
                    payload.yDist(),
                    payload.zDist()
            );
            return;
        }

        for (int i = 0; i < payload.count(); i++) {
            double xSpeed = this.minecraft.level.random.nextGaussian() * payload.xDist();
            double ySpeed = this.minecraft.level.random.nextGaussian() * payload.yDist();
            double zSpeed = this.minecraft.level.random.nextGaussian() * payload.zDist();

            double x = payload.x() + this.minecraft.level.random.nextGaussian() * payload.xDist();
            double y = payload.y() + this.minecraft.level.random.nextGaussian() * payload.yDist();
            double z = payload.z() + this.minecraft.level.random.nextGaussian() * payload.zDist();

            if (payload.maxSpeed() != 0.0D) {
                xSpeed *= payload.maxSpeed();
                ySpeed *= payload.maxSpeed();
                zSpeed *= payload.maxSpeed();
            }

            this.minecraft.particleEngine.createParticle(
                    payload.particle(),
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed
            );
        }
    }

    public void tick() {
        // Main particle engine already ticks normally.
    }

    public void render(
            Camera camera,
            Matrix4f modelMatrix,
            Matrix4f projectionMatrix,
            float partialTick
    ) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());

        modelViewStack.pushMatrix();

        try {
            modelViewStack.identity();
            modelViewStack.mul(modelMatrix);
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setProjectionMatrix(
                    projectionMatrix,
                    VertexSorting.DISTANCE_TO_ORIGIN
            );

            this.minecraft.particleEngine.render(
                    this.minecraft.gameRenderer.lightTexture(),
                    camera,
                    partialTick
            );
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setProjectionMatrix(
                    previousProjection,
                    VertexSorting.DISTANCE_TO_ORIGIN
            );

            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    public void close() {
        // Do not close minecraft.particleEngine.
    }
}