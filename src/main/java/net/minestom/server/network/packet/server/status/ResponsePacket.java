package net.minestom.server.network.packet.server.status;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ResponsePacket(@NotNull String jsonResponse) implements ServerPacket {
    public ResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, jsonResponse);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case STATUS -> ServerPacketIdentifier.STATUS_RESPONSE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.STATUS);
        };
    }
}
