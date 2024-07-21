package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.INT;

public record EntityStatusPacket(int entityId, byte status) implements ServerPacket.Play {
    public EntityStatusPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, entityId);
        writer.write(BYTE, status);
    }

}
