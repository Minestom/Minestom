package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.server.ServerPacket;

public record DebugChunkValuePacket(int chunkX, int chunkZ, DebugSubscription.Update<?> update) implements ServerPacket.Play {
    // The client reads the chunk position as one big endian long with x in the low bits
    public static final NetworkBuffer.Type<DebugChunkValuePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.INT, DebugChunkValuePacket::chunkZ,
            NetworkBuffer.INT, DebugChunkValuePacket::chunkX,
            DebugSubscription.Update.NETWORK_TYPE, DebugChunkValuePacket::update,
            (chunkZ, chunkX, update) -> new DebugChunkValuePacket(chunkX, chunkZ, update));
}
