package com.skyeshade.skyesight.client.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class SameLevelSkyesightChunkSource implements SkyesightClientChunkSource {
    private final LongSet trackedChunks = new LongOpenHashSet();

    @Override
    public void updateReadyChunks(
            ClientLevel level,
            ChunkTracker tracker,
            Vec3 cameraPosition,
            int radius
    ) {
        int centerChunkX = ((int) Math.floor(cameraPosition.x())) >> 4;
        int centerChunkZ = ((int) Math.floor(cameraPosition.z())) >> 4;

        LongSet wanted = new LongOpenHashSet();

        for (int dz = -radius - 1; dz <= radius + 1; dz++) {
            for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                if (level.getChunkSource().getChunk(chunkX, chunkZ, false) != null) {
                    wanted.add(ChunkPos.asLong(chunkX, chunkZ));
                }
            }
        }

        for (long packed : wanted) {
            if (this.trackedChunks.add(packed)) {
                tracker.onChunkStatusAdded(
                        ChunkPos.getX(packed),
                        ChunkPos.getZ(packed),
                        3
                );
            }
        }

        LongSet removed = new LongOpenHashSet();

        for (long packed : this.trackedChunks) {
            if (!wanted.contains(packed)) {
                removed.add(packed);
            }
        }

        for (long packed : removed) {
            this.trackedChunks.remove(packed);

            tracker.onChunkStatusRemoved(
                    ChunkPos.getX(packed),
                    ChunkPos.getZ(packed),
                    3
            );
        }
    }
}