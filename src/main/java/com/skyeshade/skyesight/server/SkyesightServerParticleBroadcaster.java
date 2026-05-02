package com.skyeshade.skyesight.server;

import com.skyeshade.skyesight.network.SkyesightParticlePayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkyesightServerParticleBroadcaster {
    private SkyesightServerParticleBroadcaster() {}

    public static void send(
            ServerLevel level,
            ParticleOptions particle,
            boolean overrideLimiter,
            double x,
            double y,
            double z,
            int count,
            double xDist,
            double yDist,
            double zDist,
            double maxSpeed
    ) {
        ChunkPos chunkPos = new ChunkPos(
                Mth.floor(x) >> 4,
                Mth.floor(z) >> 4
        );

        MinecraftServer server = level.getServer();

        for (SkyesightServerViewTracker.WatchedPlayerView watched :
                SkyesightServerViewTracker.viewsWatching(level.dimension(), chunkPos)) {
            ServerPlayer player = server.getPlayerList().getPlayer(watched.playerId());

            if (player == null) {
                continue;
            }

            PacketDistributor.sendToPlayer(
                    player,
                    new SkyesightParticlePayload(
                            watched.watch().viewId(),
                            level.dimension(),
                            particle,
                            overrideLimiter,
                            x,
                            y,
                            z,
                            xDist,
                            yDist,
                            zDist,
                            maxSpeed,
                            count
                    )
            );
        }
    }
}