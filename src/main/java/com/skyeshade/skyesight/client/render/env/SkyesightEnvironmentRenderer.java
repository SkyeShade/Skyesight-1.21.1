package com.skyeshade.skyesight.client.render.env;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;

public interface SkyesightEnvironmentRenderer {
    void renderSky(
            ClientLevel level,
            Camera camera,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            float partialTick
    );
}