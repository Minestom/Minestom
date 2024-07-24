package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientPickItemPacket(int slot) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPickItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientPickItemPacket::slot,
            ClientPickItemPacket::new);
}
