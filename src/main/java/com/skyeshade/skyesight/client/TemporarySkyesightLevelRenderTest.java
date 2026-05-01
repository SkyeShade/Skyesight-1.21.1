package com.skyeshade.skyesight.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.api.SkyesightRenderMode;
import com.skyeshade.skyesight.api.SkyesightViewHandle;
import com.skyeshade.skyesight.api.SkyesightViewSpec;
import com.skyeshade.skyesight.client.render.*;
import com.skyeshade.skyesight.client.render.env.SkyesightEnvironmentRendererSelector;
import com.skyeshade.skyesight.client.render.fog.SkyesightFogRenderer;
import com.skyeshade.skyesight.client.render.light.SkyesightLightTextureContext;
import com.skyeshade.skyesight.client.render.light.SkyesightLightTextureUpdater;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(
        modid = Skyesight.MODID,
        value = Dist.CLIENT
)
public final class TemporarySkyesightLevelRenderTest {
    private static final ResourceLocation DEBUG_VIEW_ID =
            ResourceLocation.fromNamespaceAndPath(Skyesight.MODID, "debug_gui_view");

    private static final int RENDER_DIST = 4;

    private TemporarySkyesightLevelRenderTest() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null || event.getCamera() == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        SkyesightViewHandle view = ensureDebugView();

        view.camera().copyFromMainCameraWithOffset(new Vec3(1000.0D, 0.0D, 0.0D));

        Matrix4f projectionMatrix = new Matrix4f(event.getProjectionMatrix());

        view.render(partialTick, projectionMatrix);
    }

    private static SkyesightViewHandle ensureDebugView() {
        return Skyesight.api()
                .getView(DEBUG_VIEW_ID)
                .orElseGet(() -> Skyesight.api().createView(
                        new SkyesightViewSpec(
                                DEBUG_VIEW_ID,
                                Level.OVERWORLD,
                                Vec3.ZERO,
                                new Quaternionf(),
                                RENDER_DIST,
                                640,
                                360,
                                SkyesightRenderMode.WORLD
                        )
                ));
    }
}