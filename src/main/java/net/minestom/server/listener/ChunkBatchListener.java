package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientChunkBatchReceivedPacket;
import org.jetbrains.annotations.NotNull;

public final class ChunkBatchListener {

    public static void batchReceivedListener(@NotNull ClientChunkBatchReceivedPacket packet, @NotNull Player player) {
        player.onChunkBatchReceived(packet.targetChunksPerTick());
    }
}
