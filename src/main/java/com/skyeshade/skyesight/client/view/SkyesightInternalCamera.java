package com.skyeshade.skyesight.client.view;

import com.skyeshade.skyesight.api.SkyesightViewCamera;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class SkyesightInternalCamera implements SkyesightViewCamera {
    private final SkyesightMutableCamera camera = new SkyesightMutableCamera();

    public Camera minecraftCamera() {
        return this.camera;
    }

    @Override
    public Vec3 position() {
        return this.camera.getPosition();
    }

    @Override
    public float pitch() {
        return this.camera.getXRot();
    }

    @Override
    public float yaw() {
        return this.camera.getYRot();
    }

    @Override
    public float roll() {
        return this.camera.getRoll();
    }

    @Override
    public Quaternionf rotation() {
        return new Quaternionf(this.camera.rotation());
    }

    @Override
    public void setPosition(Vec3 position) {
        this.camera.setPositionPublic(position);
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        this.camera.setRotationPublic(yaw, pitch, 0.0F);
    }

    @Override
    public void setRotation(float yaw, float pitch, float roll) {
        this.camera.setRotationPublic(yaw, pitch, roll);
    }

    public void setRotation(Quaternionf rotation) {
        this.camera.setRotationPublic(rotation);
    }

    @Override
    public void copyFromMainCamera() {
        Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.camera.copyFrom(mainCamera);
    }

    @Override
    public void copyFromMainCameraWithOffset(Vec3 offset) {
        Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.camera.copyFromWithOffset(mainCamera, offset);
    }
}