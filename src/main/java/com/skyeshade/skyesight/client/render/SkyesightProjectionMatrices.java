package com.skyeshade.skyesight.client.render;

import org.joml.Matrix4f;

public final class SkyesightProjectionMatrices {
    private SkyesightProjectionMatrices() {}

    public static Matrix4f perspective(
            float fovDegrees,
            float aspect,
            float nearPlane,
            float farPlane
    ) {
        return new Matrix4f().perspective(
                (float) Math.toRadians(fovDegrees),
                aspect,
                nearPlane,
                farPlane
        );
    }
}