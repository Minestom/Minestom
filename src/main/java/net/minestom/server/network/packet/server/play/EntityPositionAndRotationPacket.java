package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.VecDelta;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityPositionAndRotationPacket(int entityId, VecDelta delta,
                                              float yaw, float pitch, boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionAndRotationPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, EntityPositionAndRotationPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(VAR_INT, MovementProperties.pack(value.onGround, value.delta.stepCount()));
            VecDelta.write(buffer, value.delta);
            buffer.write(BYTE, (byte) (value.yaw * 256f / 360f));
            buffer.write(BYTE, (byte) (value.pitch * 256f / 360f));
        }

        @Override
        public EntityPositionAndRotationPacket read(NetworkBuffer buffer) {
            final int entityId = buffer.read(VAR_INT);
            final int properties = buffer.read(VAR_INT);
            final VecDelta delta = VecDelta.read(buffer, MovementProperties.stepCount(properties));
            final byte yaw = buffer.read(BYTE);
            final byte pitch = buffer.read(BYTE);
            return new EntityPositionAndRotationPacket(entityId, delta,
                    yaw * 360f / 256f, pitch * 360f / 256f, MovementProperties.onGround(properties));
        }
    };

    public static EntityPositionAndRotationPacket getPacket(int entityId,
                                                            Pos newPosition, Pos oldPosition,
                                                            boolean onGround) {
        final short deltaX = CoordConversion.deltaShort4096(newPosition.x(), oldPosition.x());
        final short deltaY = CoordConversion.deltaShort4096(newPosition.y(), oldPosition.y());
        final short deltaZ = CoordConversion.deltaShort4096(newPosition.z(), oldPosition.z());
        return new EntityPositionAndRotationPacket(entityId, new VecDelta.Linear(deltaX, deltaY, deltaZ),
                newPosition.yaw(), newPosition.pitch(), onGround);
    }
}
