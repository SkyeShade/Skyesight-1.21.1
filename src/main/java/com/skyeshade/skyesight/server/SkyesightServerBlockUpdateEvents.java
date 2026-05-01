package com.skyeshade.skyesight.server;

import com.skyeshade.skyesight.Skyesight;
import com.skyeshade.skyesight.network.SkyesightBlockUpdatesPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.List;

@EventBusSubscriber(modid = Skyesight.MODID)
public final class SkyesightServerBlockUpdateEvents {
    private SkyesightServerBlockUpdateEvents() {}

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        sendBlockUpdate(level, event.getPos(), event.getPlacedBlock());
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        sendBlockUpdate(
                level,
                event.getPos(),
                Blocks.AIR.defaultBlockState()
        );
    }

    private static void sendBlockUpdate(ServerLevel level, BlockPos pos, BlockState state) {
        ChunkPos chunkPos = new ChunkPos(pos);

        Skyesight.LOGGER.info(
                "[Skyesight] Server block update dim={} pos={} chunk={} state={}",
                level.dimension().location(),
                pos.toShortString(),
                chunkPos,
                state
        );

        Collection<SkyesightServerViewTracker.WatchedPlayerView> watches =
                SkyesightServerViewTracker.viewsWatching(level.dimension(), chunkPos);

        Skyesight.LOGGER.info(
                "[Skyesight] Watches for block update: {}",
                watches.size()
        );

        MinecraftServer server = level.getServer();

        for (SkyesightServerViewTracker.WatchedPlayerView watched : watches) {
            ServerPlayer player = server.getPlayerList().getPlayer(watched.playerId());

            if (player == null) {
                continue;
            }

            Skyesight.LOGGER.info(
                    "[Skyesight] Sending block update to {} view={}",
                    player.getGameProfile().getName(),
                    watched.watch().viewId()
            );

            PacketDistributor.sendToPlayer(
                    player,
                    new SkyesightBlockUpdatesPayload(
                            watched.watch().viewId(),
                            level.dimension(),
                            List.of(new SkyesightBlockUpdatesPayload.Entry(pos.immutable(), state))
                    )
            );
        }
    }
}