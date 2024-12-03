package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientChunkBatchReceivedPacket(float targetChunksPerTick) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChunkBatchReceivedPacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, ClientChunkBatchReceivedPacket::targetChunksPerTick,
            ClientChunkBatchReceivedPacket::new);
}
