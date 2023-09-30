package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record KeepAlivePacket(long id) implements ServerPacket {
    public KeepAlivePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, id);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return state == ConnectionState.PLAY ? ServerPacketIdentifier.KEEP_ALIVE : ServerPacketIdentifier.CONFIGURATION_KEEP_ALIVE;
    }
}
