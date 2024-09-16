package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.POS;

public record ClientPlayerPositionAndRotationPacket(@NotNull Pos position,
                                                    boolean onGround) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerPositionAndRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            POS, ClientPlayerPositionAndRotationPacket::position,
            BOOLEAN, ClientPlayerPositionAndRotationPacket::onGround,
            ClientPlayerPositionAndRotationPacket::new);
}
