package com.skyeshade.skyesight.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SkyesightFrustumFactory {
    private SkyesightFrustumFactory() {}

    public static Frustum create(Camera camera, Matrix4f modelMatrix, Matrix4f projectionMatrix) {
        Frustum frustum = new Frustum(modelMatrix, projectionMatrix);

        Vec3 pos = camera.getPosition();
        frustum.prepare(pos.x(), pos.y(), pos.z());

        return frustum;
    }
}