package com.skyeshade.skyesight.server;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.BiConsumer;

public final class SkyesightServerViewTracker {
    private static final Map<UUID, Map<ResourceLocation, ViewWatch>> WATCHES = new HashMap<>();

    private SkyesightServerViewTracker() {}

    public static void updateWatch(
            ServerPlayer player,
            ResourceLocation viewId,
            ResourceKey<Level> dimension,
            int centerChunkX,
            int centerChunkZ,
            int radius,
            Collection<ChunkPos> chunks
    ) {
        Map<ResourceLocation, ViewWatch> playerWatches =
                WATCHES.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());

        playerWatches.put(
                viewId,
                new ViewWatch(
                        viewId,
                        dimension,
                        centerChunkX,
                        centerChunkZ,
                        radius,
                        Set.copyOf(chunks)
                )
        );
    }

    public static ViewWatch getWatch(ServerPlayer player, ResourceLocation viewId) {
        Map<ResourceLocation, ViewWatch> playerWatches = WATCHES.get(player.getUUID());

        if (playerWatches == null) {
            return null;
        }

        return playerWatches.get(viewId);
    }

    public static void forEachWatch(BiConsumer<UUID, ViewWatch> consumer) {
        for (Map.Entry<UUID, Map<ResourceLocation, ViewWatch>> playerEntry : WATCHES.entrySet()) {
            UUID playerId = playerEntry.getKey();

            for (ViewWatch watch : playerEntry.getValue().values()) {
                consumer.accept(playerId, watch);
            }
        }
    }

    public static void removePlayer(ServerPlayer player) {
        WATCHES.remove(player.getUUID());
    }

    public static Collection<WatchedPlayerView> viewsWatching(
            ResourceKey<Level> dimension,
            ChunkPos chunkPos
    ) {
        List<WatchedPlayerView> result = new ArrayList<>();

        for (Map.Entry<UUID, Map<ResourceLocation, ViewWatch>> playerEntry : WATCHES.entrySet()) {
            UUID playerId = playerEntry.getKey();

            for (ViewWatch watch : playerEntry.getValue().values()) {
                if (!watch.dimension().equals(dimension)) {
                    continue;
                }

                if (!watch.chunks().contains(chunkPos)) {
                    continue;
                }

                result.add(new WatchedPlayerView(playerId, watch));
            }
        }

        return result;
    }

    public record WatchedPlayerView(UUID playerId, ViewWatch watch) {}

    public record ViewWatch(
            ResourceLocation viewId,
            ResourceKey<Level> dimension,
            int centerChunkX,
            int centerChunkZ,
            int radius,
            Set<ChunkPos> chunks
    ) {}
}