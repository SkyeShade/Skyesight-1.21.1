package com.skyeshade.skyesight.client;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.api.SkyesightRenderMode;
import com.skyeshade.skyesight.api.SkyesightViewHandle;
import com.skyeshade.skyesight.api.SkyesightViewSpec;
import com.skyeshade.skyesight.client.render.TemporarySkyesightOutputBlitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(
        modid = Skyesight.MODID,
        value = Dist.CLIENT
)
public final class TemporarySkyesightDebugOverlay {
    private static final ResourceLocation DEBUG_VIEW_ID =
            ResourceLocation.fromNamespaceAndPath(Skyesight.MODID, "debug_gui_view");

    private static final int PANEL_WIDTH = 160;
    private static final int PANEL_HEIGHT = 90;
    private static final int PANEL_MARGIN = 12;

    private TemporarySkyesightDebugOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        SkyesightViewHandle view = ensureDebugView();

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();

        int x = screenWidth - PANEL_WIDTH - PANEL_MARGIN;
        int y = PANEL_MARGIN;

        renderDebugView(graphics, view, x, y, PANEL_WIDTH, PANEL_HEIGHT);
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
                                4,
                                640,
                                360,
                                SkyesightRenderMode.WORLD
                        )
                ));
    }

    private static void renderDebugView(
            GuiGraphics graphics,
            SkyesightViewHandle view,
            int x,
            int y,
            int width,
            int height
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xAA000000);

        if (view.outputTarget() != null && view.status().isRenderable()) {
            TemporarySkyesightOutputBlitter.blit(
                    graphics,
                    view.colorTextureId(),
                    x,
                    y,
                    width,
                    height
            );
        } else {
            graphics.fill(x, y, x + width, y + height, 0xFF707070);
        }

        graphics.drawString(
                minecraft.font,
                "Skyesight Debug View",
                x + 8,
                y + 8,
                0xFFFFFFFF,
                false
        );

        graphics.drawString(
                minecraft.font,
                view.dimension().location().toString(),
                x + 8,
                y + 22,
                0xFFE0E0E0,
                false
        );

        graphics.drawString(
                minecraft.font,
                view.status().name(),
                x + 8,
                y + 36,
                0xFFE0E0E0,
                false
        );
    }
}