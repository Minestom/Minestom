package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityHeadLookPacket(int entityId, float yaw) implements ServerPacket.Play {
    public EntityHeadLookPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), (reader.read(BYTE) * 360f) / 256f);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(BYTE, (byte) (this.yaw * 256 / 360));
    }

}
