package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientAnimationPacket(@NotNull PlayerHand hand) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(PlayerHand.class), ClientAnimationPacket::hand,
            ClientAnimationPacket::new);
}
