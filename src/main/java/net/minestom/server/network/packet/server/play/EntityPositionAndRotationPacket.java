package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPositionAndRotationPacket(int entityId, short deltaX, short deltaY, short deltaZ,
                                              float yaw, float pitch, boolean onGround) implements ServerPacket {
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

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.ENTITY_POSITION_AND_ROTATION;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
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
