package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientInteractEntityPacket(int targetId, PlayerHand hand, Vec location, boolean usingSecondaryAction) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientInteractEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientInteractEntityPacket::targetId,
            PlayerHand.NETWORK_TYPE, ClientInteractEntityPacket::hand,
            LP_VECTOR3, ClientInteractEntityPacket::location,
            BOOLEAN, ClientInteractEntityPacket::usingSecondaryAction,
            ClientInteractEntityPacket::new
    );
}
