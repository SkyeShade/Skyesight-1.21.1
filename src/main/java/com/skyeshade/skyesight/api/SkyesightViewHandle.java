package com.skyeshade.skyesight.api;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public interface SkyesightViewHandle {
    ResourceLocation id();

    ResourceKey<Level> dimension();

    SkyesightRenderMode renderMode();

    SkyesightViewStatus status();

    SkyesightViewCamera camera();

    void setDimension(ResourceKey<Level> dimension);

    void setRenderDistance(int renderDistanceChunks);

    int renderDistanceChunks();

    void resize(int width, int height);

    int width();

    int height();

    void render(float partialTick, Matrix4f projectionMatrix);

    TextureTarget outputTarget();

    int colorTextureId();

    void close();
}