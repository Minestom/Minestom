package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientQueryEntityNbtPacket(int transactionId, int entityId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientQueryEntityNbtPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientQueryEntityNbtPacket::transactionId,
            VAR_INT, ClientQueryEntityNbtPacket::entityId,
            ClientQueryEntityNbtPacket::new);
}
