package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ClientCloseWindowPacket(byte windowId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCloseWindowPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientCloseWindowPacket::windowId,
            ClientCloseWindowPacket::new);
}
