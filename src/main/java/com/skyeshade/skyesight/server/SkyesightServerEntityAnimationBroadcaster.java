package com.skyeshade.skyesight.server;

import com.skyeshade.skyesight.network.SkyesightEntityAnimationPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkyesightServerEntityAnimationBroadcaster {
    private SkyesightServerEntityAnimationBroadcaster() {}

    public static void sendSwing(ServerLevel level, Entity entity, InteractionHand hand) {
        ChunkPos chunkPos = entity.chunkPosition();
        MinecraftServer server = level.getServer();

        for (SkyesightServerViewTracker.WatchedPlayerView watched :
                SkyesightServerViewTracker.viewsWatching(level.dimension(), chunkPos)) {
            ServerPlayer player = server.getPlayerList().getPlayer(watched.playerId());

            if (player == null) {
                continue;
            }

            PacketDistributor.sendToPlayer(
                    player,
                    new SkyesightEntityAnimationPayload(
                            watched.watch().viewId(),
                            level.dimension(),
                            entity.getUUID(),
                            SkyesightEntityAnimationPayload.AnimationType.SWING_HAND,
                            hand
                    )
            );
        }
    }
}