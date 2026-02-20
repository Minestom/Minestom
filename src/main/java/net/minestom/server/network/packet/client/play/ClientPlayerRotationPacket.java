package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.intellij.lang.annotations.MagicConstant;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientPlayerRotationPacket(float yaw, float pitch, @MagicConstant(flagsFromClass = ClientPlayerRotationPacket.class) byte flags) implements ClientPacket.Play {
    public static final byte FLAG_ON_GROUND = 1;
    public static final byte FLAG_HORIZONTAL_COLLISION = 1 << 1;

    public static final NetworkBuffer.Type<ClientPlayerRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, ClientPlayerRotationPacket::yaw,
            FLOAT, ClientPlayerRotationPacket::pitch,
            BYTE, ClientPlayerRotationPacket::flags,
            ClientPlayerRotationPacket::new);

    public ClientPlayerRotationPacket(float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
        this(yaw, pitch, (byte) ((onGround ? FLAG_ON_GROUND : 0) |
                (horizontalCollision ? FLAG_HORIZONTAL_COLLISION : 0)));
    }

    public boolean onGround() {
        return (flags & FLAG_ON_GROUND) != 0;
    }

    public boolean horizontalCollision() {
        return (flags & FLAG_HORIZONTAL_COLLISION) != 0;
    }
}
