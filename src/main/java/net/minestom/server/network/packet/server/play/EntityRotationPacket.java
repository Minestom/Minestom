package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityRotationPacket(int entityId, float yaw, float pitch, boolean onGround) implements ServerPacket.Play {
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

}
