package com.skyeshade.skyesight.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;

public final class SkyesightProjectionScope implements AutoCloseable {
    private final Matrix4f previousProjection;

    public SkyesightProjectionScope(Matrix4f projectionMatrix) {
        this.previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());

        RenderSystem.setProjectionMatrix(
                projectionMatrix,
                VertexSorting.DISTANCE_TO_ORIGIN
        );
    }

    @Override
    public void close() {
        RenderSystem.setProjectionMatrix(
                this.previousProjection,
                VertexSorting.DISTANCE_TO_ORIGIN
        );
    }
}