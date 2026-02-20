package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientAnimationPacket(PlayerHand hand) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            PlayerHand.NETWORK_TYPE, ClientAnimationPacket::hand,
            ClientAnimationPacket::new);
}
