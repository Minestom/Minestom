package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ChunkBatchFinishedPacket(int batchSize) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkBatchFinishedPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ChunkBatchFinishedPacket::batchSize,
            ChunkBatchFinishedPacket::new);
}
