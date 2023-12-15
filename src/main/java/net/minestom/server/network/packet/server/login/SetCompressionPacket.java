package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetCompressionPacket(int threshold) implements ServerPacket {
    public SetCompressionPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, threshold);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case LOGIN -> ServerPacketIdentifier.LOGIN_SET_COMPRESSION;
            default -> throw new IllegalArgumentException();
        };
    }
}
