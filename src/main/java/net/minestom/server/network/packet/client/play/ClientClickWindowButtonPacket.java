package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientClickWindowButtonPacket(int windowId, int buttonId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientClickWindowButtonPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientClickWindowButtonPacket::windowId,
            VAR_INT, ClientClickWindowButtonPacket::buttonId,
            ClientClickWindowButtonPacket::new);
}
