package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUseItemPacket(@NotNull PlayerHand hand, int sequence, float yaw,
                                  float pitch) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientUseItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            Enum(PlayerHand.class), ClientUseItemPacket::hand,
            VAR_INT, ClientUseItemPacket::sequence,
            FLOAT, ClientUseItemPacket::yaw,
            FLOAT, ClientUseItemPacket::pitch,
            ClientUseItemPacket::new);
}
