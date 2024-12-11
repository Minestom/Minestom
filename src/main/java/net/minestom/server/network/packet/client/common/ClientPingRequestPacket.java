package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record ClientPingRequestPacket(long number) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPingRequestPacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, ClientPingRequestPacket::number, ClientPingRequestPacket::new);
}
