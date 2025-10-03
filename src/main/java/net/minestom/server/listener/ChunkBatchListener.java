package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientChunkBatchReceivedPacket;

public final class ChunkBatchListener {

    public static void batchReceivedListener(ClientChunkBatchReceivedPacket packet, Player player) {
        player.onChunkBatchReceived(packet.targetChunksPerTick());
    }
}
