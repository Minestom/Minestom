package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.POS;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Acknowledges a teleport and reports where the client ended up.
 *
 * @param teleportId the id of the teleport being acknowledged
 * @param position   the position the client ended up at
 */
public record ClientTeleportConfirmPacket(int teleportId, Pos position) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientTeleportConfirmPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientTeleportConfirmPacket::teleportId,
            POS, ClientTeleportConfirmPacket::position,
            ClientTeleportConfirmPacket::new);
}
