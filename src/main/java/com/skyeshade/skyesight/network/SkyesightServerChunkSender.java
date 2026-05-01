package com.skyeshade.skyesight.network;

import com.skyeshade.skyesight.Skyesight;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SkyesightServerChunkSender {
    private static final int MAX_CHUNKS_PER_REQUEST = 256;

    private SkyesightServerChunkSender() {}

    public static void handleChunkRequest(
            SkyesightChunkRequestPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ServerLevel level = player.server.getLevel(payload.dimension());

            if (level == null) {
                Skyesight.LOGGER.warn(
                        "[Skyesight] Ignoring chunk request for missing dimension {}",
                        payload.dimension().location()
                );
                return;
            }

            int sent = 0;

            for (ChunkPos pos : payload.chunks()) {
                if (sent >= MAX_CHUNKS_PER_REQUEST) {
                    break;
                }

                if (!isWithinRequestRadius(pos, payload.centerChunkX(), payload.centerChunkZ(), payload.radius())) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(pos.x, pos.z);

                ClientboundLevelChunkPacketData chunkData =
                        new ClientboundLevelChunkPacketData(chunk);

                ClientboundLightUpdatePacketData lightData =
                        new ClientboundLightUpdatePacketData(
                                chunk.getPos(),
                                level.getLightEngine(),
                                null,
                                null
                        );

                PacketDistributor.sendToPlayer(
                        player,
                        new SkyesightChunkDataPayload(
                                payload.viewId(),
                                payload.dimension(),
                                payload.centerChunkX(),
                                payload.centerChunkZ(),
                                payload.radius(),
                                pos.x,
                                pos.z,
                                chunkData,
                                lightData
                        )
                );

                sent++;
            }

            Skyesight.LOGGER.info(
                    "[Skyesight] Sent {} chunks for view {}",
                    sent,
                    payload.viewId()
            );
        });
    }

    private static boolean isWithinRequestRadius(
            ChunkPos pos,
            int centerChunkX,
            int centerChunkZ,
            int radius
    ) {
        return Math.abs(pos.x - centerChunkX) <= radius
                && Math.abs(pos.z - centerChunkZ) <= radius;
    }
}