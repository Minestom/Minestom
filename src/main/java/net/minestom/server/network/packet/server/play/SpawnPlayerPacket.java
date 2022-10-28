package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnPlayerPacket(int entityId, @NotNull UUID playerUuid,
                                @NotNull Pos position) implements ServerPacket {
    public SpawnPlayerPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(UUID),
                new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                        (reader.read(BYTE) * 360f) / 256f, (reader.read(BYTE) * 360f) / 256f));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(UUID, playerUuid);
        writer.write(DOUBLE, position.x());
        writer.write(DOUBLE, position.y());
        writer.write(DOUBLE, position.z());
        writer.write(BYTE, (byte) (position.yaw() * 256f / 360f));
        writer.write(BYTE, (byte) (position.pitch() * 256f / 360f));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_PLAYER;
    }
}
