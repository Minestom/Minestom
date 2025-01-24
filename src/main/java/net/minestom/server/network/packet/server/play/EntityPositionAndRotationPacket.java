package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPositionAndRotationPacket(int entityId, short deltaX, short deltaY, short deltaZ,
                                              float yaw, float pitch, boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionAndRotationPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntityPositionAndRotationPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(SHORT, value.deltaX);
            buffer.write(SHORT, value.deltaY);
            buffer.write(SHORT, value.deltaZ);
            buffer.write(BYTE, (byte) (value.yaw * 256 / 360));
            buffer.write(BYTE, (byte) (value.pitch * 256 / 360));
            buffer.write(BOOLEAN, value.onGround);
        }

        @Override
        public EntityPositionAndRotationPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityPositionAndRotationPacket(buffer.read(VAR_INT),
                    buffer.read(SHORT), buffer.read(SHORT), buffer.read(SHORT),
                    buffer.read(BYTE) * 360f / 256f, buffer.read(BYTE) * 360f / 256f,
                    buffer.read(BOOLEAN));
        }
    };

    public static EntityPositionAndRotationPacket getPacket(int entityId,
                                                            @NotNull Pos newPosition, @NotNull Pos oldPosition,
                                                            boolean onGround) {
        final short deltaX = (short) ((newPosition.x() * 32 - oldPosition.x() * 32) * 128);
        final short deltaY = (short) ((newPosition.y() * 32 - oldPosition.y() * 32) * 128);
        final short deltaZ = (short) ((newPosition.z() * 32 - oldPosition.z() * 32) * 128);
        return new EntityPositionAndRotationPacket(entityId, deltaX, deltaY, deltaZ, newPosition.yaw(), newPosition.pitch(), onGround);
    }
}
