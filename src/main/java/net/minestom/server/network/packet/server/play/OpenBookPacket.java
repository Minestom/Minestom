package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record OpenBookPacket(@NotNull PlayerHand hand) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<OpenBookPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(PlayerHand.class), OpenBookPacket::hand,
            OpenBookPacket::new);
}
