package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(int entityId, Pos position, boolean onGround) implements ServerPacket {
    public EntityTeleportPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                        reader.read(BYTE) * 360f / 256f, reader.read(BYTE) * 360f / 256f),
                reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(DOUBLE, position.x());
        writer.write(DOUBLE, position.y());
        writer.write(DOUBLE, position.z());
        writer.write(BYTE, (byte) (position.yaw() * 256f / 360f));
        writer.write(BYTE, (byte) (position.pitch() * 256f / 360f));
        writer.write(BOOLEAN, onGround);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.ENTITY_TELEPORT;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
