package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket.FLAG_HORIZONTAL_COLLISION;
import static net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket.FLAG_ON_GROUND;

public record ClientPlayerRotationPacket(float yaw, float pitch, byte flags) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, ClientPlayerRotationPacket::yaw,
            FLOAT, ClientPlayerRotationPacket::pitch,
            BYTE, ClientPlayerRotationPacket::flags,
            ClientPlayerRotationPacket::new);

    public ClientPlayerRotationPacket(float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
        this(yaw, pitch, (byte) ((onGround ? FLAG_ON_GROUND : 0) |
                (byte) (horizontalCollision ? FLAG_HORIZONTAL_COLLISION : 0)));
    }

    public boolean onGround() {
        return (flags & FLAG_ON_GROUND) != 0;
    }

    public boolean horizontalCollision() {
        return (flags & FLAG_HORIZONTAL_COLLISION) != 0;
    }
}
