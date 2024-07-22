package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record ChunkBatchStartPacket() implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkBatchStartPacket> SERIALIZER = NetworkBufferTemplate.template(ChunkBatchStartPacket::new);
}
