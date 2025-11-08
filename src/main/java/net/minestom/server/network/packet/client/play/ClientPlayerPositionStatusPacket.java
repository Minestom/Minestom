package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.intellij.lang.annotations.MagicConstant;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ClientPlayerPositionStatusPacket(@MagicConstant(flagsFromClass = ClientPlayerPositionStatusPacket.class) byte flags) implements ClientPacket.Play {
    public static final byte FLAG_ON_GROUND = 1;
    public static final byte FLAG_HORIZONTAL_COLLISION = 1 << 1;

    public static final NetworkBuffer.Type<ClientPlayerPositionStatusPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientPlayerPositionStatusPacket::flags,
            ClientPlayerPositionStatusPacket::new);

    public ClientPlayerPositionStatusPacket(boolean onGround, boolean horizontalCollision) {
        this((byte) ((onGround ? FLAG_ON_GROUND : 0) |
                (horizontalCollision ? FLAG_HORIZONTAL_COLLISION : 0)));
    }

    public boolean onGround() {
        return (flags & FLAG_ON_GROUND) != 0;
    }

    public boolean horizontalCollision() {
        return (flags & FLAG_HORIZONTAL_COLLISION) != 0;
    }

}
