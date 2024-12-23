package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPickItemFromEntityPacket(int entityId, boolean includeData) implements ClientPacket {
    public static final Type<ClientPickItemFromEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientPickItemFromEntityPacket::entityId,
            BOOLEAN, ClientPickItemFromEntityPacket::includeData,
            ClientPickItemFromEntityPacket::new);
}
