package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ClientClickWindowButtonPacket(byte windowId, byte buttonId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientClickWindowButtonPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientClickWindowButtonPacket::windowId,
            BYTE, ClientClickWindowButtonPacket::buttonId,
            ClientClickWindowButtonPacket::new);
}
