package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record PingPacket(int id) implements ServerPacket.Configuration, ServerPacket.Play {
    public PingPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, id);
    }

}
