package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public record ClientPlayerPositionPacket(@NotNull Point position, byte flags) implements ClientPacket {
    public static final int FLAG_ON_GROUND = 1;
    public static final int FLAG_HORIZONTAL_COLLISION = 1 << 1;

    public static final NetworkBuffer.Type<ClientPlayerPositionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VECTOR3D, ClientPlayerPositionPacket::position,
            BYTE, ClientPlayerPositionPacket::flags,
            ClientPlayerPositionPacket::new);

    public ClientPlayerPositionPacket(@NotNull Point position, boolean onGround, boolean horizontalCollision) {
        this(position, (byte) ((onGround ? FLAG_ON_GROUND : 0) |
                (byte) (horizontalCollision ? FLAG_HORIZONTAL_COLLISION : 0)));
    }

    public boolean onGround() {
        return (flags & FLAG_ON_GROUND) != 0;
    }

    public boolean horizontalCollision() {
        return (flags & FLAG_HORIZONTAL_COLLISION) != 0;
    }

}
