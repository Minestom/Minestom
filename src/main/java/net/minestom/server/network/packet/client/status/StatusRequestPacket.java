package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record StatusRequestPacket() implements ClientPacket {
    public static final NetworkBuffer.Type<StatusRequestPacket> SERIALIZER = NetworkBufferTemplate.template(StatusRequestPacket::new);
}
