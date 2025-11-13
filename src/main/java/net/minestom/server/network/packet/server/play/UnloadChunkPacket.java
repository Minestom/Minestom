package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.INT;

public record UnloadChunkPacket(int chunkX, int chunkZ) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UnloadChunkPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, UnloadChunkPacket::chunkZ, // Client reads this as a single long in big endian, so we have to write it backwards
            INT, UnloadChunkPacket::chunkX,
            (chunkZ, chunkX) -> new UnloadChunkPacket(chunkX, chunkZ)
    );
}
