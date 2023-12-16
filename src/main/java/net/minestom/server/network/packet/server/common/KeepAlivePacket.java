package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
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
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_KEEP_ALIVE;
            case PLAY -> ServerPacketIdentifier.KEEP_ALIVE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION, ConnectionState.PLAY);
        };
    }
}
