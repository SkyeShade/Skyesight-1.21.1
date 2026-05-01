package com.skyeshade.skyesight.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class SkyesightDebugCamera extends Camera {
    public void copyFromMainWithOffset(Camera mainCamera, Vec3 offset, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Entity entity = mainCamera.getEntity();

        if (level == null || entity == null) {
            return;
        }

        boolean detached = mainCamera.isDetached();
        boolean thirdPersonReverse = minecraft.options.getCameraType().isMirrored();

        this.setup(
                level,
                entity,
                detached,
                thirdPersonReverse,
                partialTick
        );

        this.setRotation(
                mainCamera.getYRot() + 90.0F,
                mainCamera.getXRot(),
                mainCamera.getRoll()
        );

        this.setPosition(mainCamera.getPosition().add(offset));
    }
}