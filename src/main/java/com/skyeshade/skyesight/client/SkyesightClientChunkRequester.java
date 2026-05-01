package com.skyeshade.skyesight.client;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorld;
import com.skyeshade.skyesight.client.world.SkyesightVisualWorldManager;
import com.skyeshade.skyesight.network.SkyesightChunkRequestPayload;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class SkyesightClientChunkRequester {
    private static final ResourceLocation DEBUG_VIEW_ID =
            ResourceLocation.fromNamespaceAndPath(Skyesight.MODID, "debug_gui_view");
    private static ChunkPos lastRequestedCenter;
    private static ResourceKey<Level> lastRequestedDimension;
    private static final ObjectOpenHashSet<PendingChunkKey> pendingChunks = new ObjectOpenHashSet<>();
    private record PendingChunkKey(ResourceKey<Level> dimension, int chunkX, int chunkZ) {}
    private SkyesightClientChunkRequester() {}

    public static void requestChunksFor(ResourceKey<Level> dimension, Camera camera, int radius) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }

        int centerChunkX = Mth.floor(camera.getPosition().x()) >> 4;
        int centerChunkZ = Mth.floor(camera.getPosition().z()) >> 4;

        SkyesightVisualWorld world =
                SkyesightVisualWorldManager.getOrCreate(dimension);

        if (world == null) {
            return;
        }

        int cacheRadius = radius + 3;

        world.chunkReceiver().setViewCenter(centerChunkX, centerChunkZ, cacheRadius);
        world.chunkReceiver().pruneOutside(centerChunkX, centerChunkZ, cacheRadius);
        prunePendingOutside(dimension, centerChunkX, centerChunkZ, cacheRadius);

        List<ChunkPos> missing = collectMissingChunks(world, dimension, centerChunkX, centerChunkZ, radius);

        ChunkPos center = new ChunkPos(centerChunkX, centerChunkZ);

        boolean centerChanged = lastRequestedCenter == null
                || !lastRequestedCenter.equals(center)
                || lastRequestedDimension == null
                || !lastRequestedDimension.equals(dimension);

        if (missing.isEmpty() && !centerChanged) {
            return;
        }

        lastRequestedCenter = center;
        lastRequestedDimension = dimension;

        missing.sort((a, b) -> {
            int da = Math.abs(a.x - centerChunkX) + Math.abs(a.z - centerChunkZ);
            int db = Math.abs(b.x - centerChunkX) + Math.abs(b.z - centerChunkZ);
            return Integer.compare(da, db);
        });

        for (ChunkPos pos : missing) {
            pendingChunks.add(packPending(dimension, pos.x, pos.z));
        }

        PacketDistributor.sendToServer(
                new SkyesightChunkRequestPayload(
                        DEBUG_VIEW_ID,
                        dimension,
                        centerChunkX,
                        centerChunkZ,
                        radius,
                        missing
                )
        );
    }
    private static PendingChunkKey packPending(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return new PendingChunkKey(dimension, chunkX, chunkZ);
    }
    public static void markChunkReceived(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        pendingChunks.remove(packPending(dimension, chunkX, chunkZ));
    }

    public static void prunePendingOutside(
            ResourceKey<Level> dimension,
            int centerChunkX,
            int centerChunkZ,
            int radius
    ) {
        ObjectOpenHashSet<PendingChunkKey> toRemove = new ObjectOpenHashSet<>();

        for (PendingChunkKey key : pendingChunks) {
            if (!key.dimension().equals(dimension)) {
                continue;
            }

            if (Math.abs(key.chunkX() - centerChunkX) > radius || Math.abs(key.chunkZ() - centerChunkZ) > radius) {
                toRemove.add(key);
            }
        }

        for (PendingChunkKey key : toRemove) {
            pendingChunks.remove(key);
        }
    }
    public static void reset() {
        pendingChunks.clear();
    }
    private static List<ChunkPos> collectMissingChunks(
            SkyesightVisualWorld world,
            ResourceKey<Level> dimension,
            int centerChunkX,
            int centerChunkZ,
            int radius
    ) {
        List<ChunkPos> missing = new ArrayList<>();

        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                PendingChunkKey key = packPending(dimension, chunkX, chunkZ);

                if (world.chunkReceiver().hasChunk(chunkX, chunkZ)) {
                    pendingChunks.remove(key);
                    continue;
                }

                if (pendingChunks.contains(key)) {
                    continue;
                }

                missing.add(new ChunkPos(chunkX, chunkZ));
            }
        }

        return missing;
    }
}