package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public record ClientPlayerPositionPacket(@NotNull Point position,
                                         boolean onGround) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerPositionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VECTOR3D, ClientPlayerPositionPacket::position,
            BOOLEAN, ClientPlayerPositionPacket::onGround,
            ClientPlayerPositionPacket::new);
}
