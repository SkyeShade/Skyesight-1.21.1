package com.skyeshade.skyesight.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class TemporarySkyesightOutputBlitter {
    private TemporarySkyesightOutputBlitter() {}

    public static void blit(GuiGraphics graphics, int textureId, int x, int y, int width, int height) {
        if (textureId <= 0) {
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Matrix4f matrix = graphics.pose().last().pose();

        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX
        );

        buffer.addVertex(matrix, x, y + height, 0).setUv(0.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y, 0).setUv(1.0F, 1.0F);
        buffer.addVertex(matrix, x, y, 0).setUv(0.0F, 1.0F);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}