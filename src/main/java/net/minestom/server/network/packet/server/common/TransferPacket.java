package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record TransferPacket(
        @NotNull String host,
        int port
) implements ServerPacket.Configuration, ServerPacket.Play {

    public TransferPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.STRING), reader.read(NetworkBuffer.VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.STRING, host);
        writer.write(NetworkBuffer.VAR_INT, port);
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_TRANSFER;
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.TRANSFER;
    }
}
