package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPositionAndRotationPacket(int entityId, short deltaX, short deltaY, short deltaZ,
                                              float yaw, float pitch, boolean onGround) implements ServerPacket.Play {
    public EntityPositionAndRotationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(SHORT), reader.read(SHORT), reader.read(SHORT),
                reader.read(BYTE) * 360f / 256f, reader.read(BYTE) * 360f / 256f, reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(SHORT, deltaX);
        writer.write(SHORT, deltaY);
        writer.write(SHORT, deltaZ);
        writer.write(BYTE, (byte) (yaw * 256 / 360));
        writer.write(BYTE, (byte) (pitch * 256 / 360));
        writer.write(BOOLEAN, onGround);
    }

    public static EntityPositionAndRotationPacket getPacket(int entityId,
                                                            @NotNull Pos newPosition, @NotNull Pos oldPosition,
                                                            boolean onGround) {
        final short deltaX = (short) ((newPosition.x() * 32 - oldPosition.x() * 32) * 128);
        final short deltaY = (short) ((newPosition.y() * 32 - oldPosition.y() * 32) * 128);
        final short deltaZ = (short) ((newPosition.z() * 32 - oldPosition.z() * 32) * 128);
        return new EntityPositionAndRotationPacket(entityId, deltaX, deltaY, deltaZ, newPosition.yaw(), newPosition.pitch(), onGround);
    }
}
