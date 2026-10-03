package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.VecDelta;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityPositionPacket(int entityId, VecDelta delta, boolean onGround)
        implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, EntityPositionPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(VAR_INT, MovementProperties.pack(value.onGround, value.delta.stepCount()));
            VecDelta.write(buffer, value.delta);
        }

        @Override
        public EntityPositionPacket read(NetworkBuffer buffer) {
            final int entityId = buffer.read(VAR_INT);
            final int properties = buffer.read(VAR_INT);
            final VecDelta delta = VecDelta.read(buffer, MovementProperties.stepCount(properties));
            return new EntityPositionPacket(entityId, delta, MovementProperties.onGround(properties));
        }
    };

    public static EntityPositionPacket getPacket(int entityId,
                                                 Pos newPosition, Pos oldPosition,
                                                 boolean onGround) {
        final short deltaX = CoordConversion.deltaShort4096(newPosition.x(), oldPosition.x());
        final short deltaY = CoordConversion.deltaShort4096(newPosition.y(), oldPosition.y());
        final short deltaZ = CoordConversion.deltaShort4096(newPosition.z(), oldPosition.z());
        return new EntityPositionPacket(entityId, new VecDelta.Linear(deltaX, deltaY, deltaZ), onGround);
    }
}
