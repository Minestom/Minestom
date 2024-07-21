package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(int entityId, Pos position, boolean onGround) implements ServerPacket.Play {
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

}
