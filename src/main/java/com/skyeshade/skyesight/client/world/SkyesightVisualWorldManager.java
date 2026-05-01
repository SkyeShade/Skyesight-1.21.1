package com.skyeshade.skyesight.client.world;

import com.skyeshade.skyesight.Skyesight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class SkyesightVisualWorldManager {
    private static final Map<ResourceKey<Level>, SkyesightVisualWorld> WORLDS = new HashMap<>();

    private SkyesightVisualWorldManager() {}

    public static SkyesightVisualWorld getOrCreate(ResourceKey<Level> dimension) {
        SkyesightVisualWorld existing = WORLDS.get(dimension);

        if (existing != null) {
            return existing;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.getConnection() == null) {
            return null;
        }

        ClientLevel skyesightLevel = SkyesightClientLevelFactory.create(dimension);
        SkyesightVisualWorld world = new SkyesightVisualWorld(dimension, skyesightLevel);
        Skyesight.LOGGER.info(
                "[Skyesight] Visual world dimension={} hasSkyLight={} ambientLight={}",
                dimension.location(),
                skyesightLevel.dimensionType().hasSkyLight(),
                skyesightLevel.dimensionType().ambientLight()
        );
        WORLDS.put(dimension, world);

        Skyesight.LOGGER.info(
                "[Skyesight] Created visual world dimension={} sameObjectAsMain={}",
                dimension.location(),
                skyesightLevel == minecraft.level
        );

        return world;
    }

    public static SkyesightVisualWorld getOrCreateCurrentLevelWorld() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        return getOrCreate(minecraft.level.dimension());
    }

    public static void close(ResourceKey<Level> dimension) {
        SkyesightVisualWorld world = WORLDS.remove(dimension);

        if (world != null) {
            world.close();
        }
    }

    public static void closeAll() {
        for (SkyesightVisualWorld world : WORLDS.values()) {
            world.close();
        }

        WORLDS.clear();
    }
}