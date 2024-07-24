package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientTeleportConfirmPacket(int teleportId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientTeleportConfirmPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientTeleportConfirmPacket::teleportId,
            ClientTeleportConfirmPacket::new);
}
