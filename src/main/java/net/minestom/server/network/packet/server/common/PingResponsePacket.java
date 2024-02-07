package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record PingResponsePacket(long number) implements ServerPacket {
    public PingResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, number);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case STATUS -> ServerPacketIdentifier.STATUS_PING_RESPONSE;
            case PLAY -> ServerPacketIdentifier.PING_RESPONSE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.STATUS);
        };
    }
}
