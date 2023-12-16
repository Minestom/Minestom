package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnEntityPacket(int entityId, @NotNull UUID uuid, int type,
                                @NotNull Pos position, float headRot, int data,
                                short velocityX, short velocityY, short velocityZ) implements ServerPacket {
    public SpawnEntityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(UUID), reader.read(VAR_INT),
                new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                        reader.read(BYTE) * 360f / 256f, reader.read(BYTE) * 360f / 256f), reader.read(BYTE) * 360f / 256f,
                reader.read(VAR_INT), reader.read(SHORT), reader.read(SHORT), reader.read(SHORT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(UUID, uuid);
        writer.write(VAR_INT, type);

        writer.write(DOUBLE, position.x());
        writer.write(DOUBLE, position.y());
        writer.write(DOUBLE, position.z());

        writer.write(BYTE, (byte) (position.pitch() * 256 / 360));
        writer.write(BYTE, (byte) (position.yaw() * 256 / 360));
        writer.write(BYTE, (byte) (headRot * 256 / 360));

        writer.write(VAR_INT, data);

        writer.write(SHORT, velocityX);
        writer.write(SHORT, velocityY);
        writer.write(SHORT, velocityZ);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.SPAWN_ENTITY;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
