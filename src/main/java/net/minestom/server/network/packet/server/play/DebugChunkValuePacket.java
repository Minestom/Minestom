package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.server.ServerPacket;

public record DebugChunkValuePacket(long chunkPos, DebugSubscription.Update<?> update) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DebugChunkValuePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.LONG, DebugChunkValuePacket::chunkPos,
            DebugSubscription.Update.NETWORK_TYPE, DebugChunkValuePacket::update,
            DebugChunkValuePacket::new);

    public DebugChunkValuePacket(int chunkX, int chunkZ, DebugSubscription.Update<?> update) {
        this((long) chunkX | (long) chunkZ << 32, update);
    }
}
