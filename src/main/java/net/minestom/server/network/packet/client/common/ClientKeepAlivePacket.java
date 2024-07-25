package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record ClientKeepAlivePacket(long id) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientKeepAlivePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, ClientKeepAlivePacket::id, ClientKeepAlivePacket::new);

    @Override
    public boolean processImmediately() {
        return true;
    }
}
