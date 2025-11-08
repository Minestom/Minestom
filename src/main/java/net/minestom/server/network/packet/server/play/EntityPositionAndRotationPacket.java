package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPositionAndRotationPacket(int entityId, short deltaX, short deltaY, short deltaZ,
                                              float yaw, float pitch, boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionAndRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityPositionAndRotationPacket::entityId,
            SHORT, EntityPositionAndRotationPacket::deltaX,
            SHORT, EntityPositionAndRotationPacket::deltaY,
            SHORT, EntityPositionAndRotationPacket::deltaZ,
            LP_ROTATION, EntityPositionAndRotationPacket::yaw,
            LP_ROTATION, EntityPositionAndRotationPacket::pitch,
            BOOLEAN, EntityPositionAndRotationPacket::onGround,
            EntityPositionAndRotationPacket::new
    );

    public static EntityPositionAndRotationPacket getPacket(int entityId,
                                                            Pos newPosition, Pos oldPosition,
                                                            boolean onGround) {
        final short deltaX = (short) ((newPosition.x() * 32 - oldPosition.x() * 32) * 128);
        final short deltaY = (short) ((newPosition.y() * 32 - oldPosition.y() * 32) * 128);
        final short deltaZ = (short) ((newPosition.z() * 32 - oldPosition.z() * 32) * 128);
        return new EntityPositionAndRotationPacket(entityId, deltaX, deltaY, deltaZ, newPosition.yaw(), newPosition.pitch(), onGround);
    }
}
