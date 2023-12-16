package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityRotationPacket(int entityId, float yaw, float pitch, boolean onGround) implements ServerPacket {
    public EntityRotationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BYTE) * 360f / 256f, reader.read(BYTE) * 360f / 256f, reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(BYTE, (byte) (yaw * 256 / 360));
        writer.write(BYTE, (byte) (pitch * 256 / 360));
        writer.write(BOOLEAN, onGround);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.ENTITY_ROTATION;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
