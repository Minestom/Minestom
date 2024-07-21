package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnExperienceOrbPacket(int entityId,
                                       @NotNull Pos position, short expCount) implements ServerPacket.Play {
    public SpawnExperienceOrbPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT),
                new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE)), reader.read(SHORT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(DOUBLE, position.x());
        writer.write(DOUBLE, position.y());
        writer.write(DOUBLE, position.z());
        writer.write(SHORT, expCount);
    }

}
