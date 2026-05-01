package com.skyeshade.skyesight.client.world;

import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public interface SkyesightClientChunkSource {
    void updateReadyChunks(
            ClientLevel level,
            ChunkTracker tracker,
            Vec3 cameraPosition,
            int radius
    );
}