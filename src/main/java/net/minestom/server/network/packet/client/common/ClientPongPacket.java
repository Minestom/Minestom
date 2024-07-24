package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.INT;

public record ClientPongPacket(int id) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPongPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, ClientPongPacket::id, ClientPongPacket::new);
}
