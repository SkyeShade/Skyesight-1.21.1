package com.skyeshade.skyesight.client.render;

import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class SkyesightCameraMatrices {
    private SkyesightCameraMatrices() {}

    public static Matrix4f createModelView(Camera camera) {
        Quaternionf rotation = camera.rotation().conjugate(new Quaternionf());
        return new Matrix4f().rotation(rotation);
    }
}