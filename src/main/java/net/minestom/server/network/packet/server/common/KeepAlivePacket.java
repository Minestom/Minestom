package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record KeepAlivePacket(long id) implements ServerPacket.Configuration, ServerPacket.Play {
    public KeepAlivePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, id);
    }

}
