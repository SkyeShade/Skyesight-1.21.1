package com.skyeshade.skyesight.client.render.env;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skyeshade.skyesight.client.render.SkyesightClonedSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class SkyesightVanillaEnvironmentRenderer implements SkyesightEnvironmentRenderer {
    @Override
    public void renderSky(
            ClientLevel level,
            Camera camera,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            float partialTick
    ) {
        Vec3 skyColor = level.getSkyColor(camera.getPosition(), partialTick);

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

        SkyesightClonedSkyRenderer.renderSky(
                level,
                camera,
                modelViewMatrix,
                projectionMatrix,
                partialTick
        );

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }
}