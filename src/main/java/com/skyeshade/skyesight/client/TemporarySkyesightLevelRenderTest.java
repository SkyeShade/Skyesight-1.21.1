package com.skyeshade.skyesight.client;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.api.SkyesightRenderMode;
import com.skyeshade.skyesight.api.SkyesightViewHandle;
import com.skyeshade.skyesight.api.SkyesightViewSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@EventBusSubscriber(
        modid = Skyesight.MODID,
        value = Dist.CLIENT
)
public final class TemporarySkyesightLevelRenderTest {
    private static final ResourceLocation DEBUG_VIEW_ID =
            ResourceLocation.fromNamespaceAndPath(Skyesight.MODID, "debug_gui_view");

    private static final int RENDER_DIST = 4;
    private static final boolean FOLLOW_MAIN_CAMERA = true;
    private static final Vec3 FOLLOW_OFFSET =
            new Vec3(4.0D, 0.0D, 0.0D);
    private static final Vec3 DEBUG_CAMERA_POSITION =
            new Vec3(0.5D, 90.0D, 0.5D);

    private static final float DEBUG_CAMERA_YAW = 0.0F;
    private static final float DEBUG_CAMERA_PITCH = 0.0F;
    private static final float DEBUG_CAMERA_ROLL = 0.0F;

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

        if (FOLLOW_MAIN_CAMERA) {
            view.camera().copyFromMainCameraWithOffset(FOLLOW_OFFSET);
        } else {
            view.camera().setPosition(DEBUG_CAMERA_POSITION);
            view.camera().setRotation(
                    DEBUG_CAMERA_YAW,
                    DEBUG_CAMERA_PITCH,
                    DEBUG_CAMERA_ROLL
            );
        }

        view.render(partialTick, new Matrix4f());
    }

    private static SkyesightViewHandle ensureDebugView() {
        return Skyesight.api()
                .getView(DEBUG_VIEW_ID)
                .orElseGet(() -> Skyesight.api().createView(
                        new SkyesightViewSpec(
                                DEBUG_VIEW_ID,
                                Level.OVERWORLD,
                                DEBUG_CAMERA_POSITION,
                                new Quaternionf(),
                                RENDER_DIST,
                                640,
                                360,
                                SkyesightRenderMode.WORLD
                        )
                ));
    }
}