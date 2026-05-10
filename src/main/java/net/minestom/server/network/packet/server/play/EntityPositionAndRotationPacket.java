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
            BYTE, value -> (byte) (value.yaw * 256f / 360f),
            BYTE, value -> (byte) (value.pitch * 256f / 360f),
            BOOLEAN, EntityPositionAndRotationPacket::onGround,
            (entityId, deltaX, deltaY, deltaZ, yaw, pitch, onGround) -> new EntityPositionAndRotationPacket(
                    entityId, deltaX, deltaY, deltaZ,
                    yaw * 360f / 256f, pitch * 360f / 256f, onGround)
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
