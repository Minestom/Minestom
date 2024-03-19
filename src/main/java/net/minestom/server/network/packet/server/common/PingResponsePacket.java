package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record PingResponsePacket(long number) implements ServerPacket.Status, ServerPacket.Play {
    public PingResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, number);
    }

    @Override
    public int statusId() {
        return ServerPacketIdentifier.STATUS_PING_RESPONSE;
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.PING_RESPONSE;
    }
}
