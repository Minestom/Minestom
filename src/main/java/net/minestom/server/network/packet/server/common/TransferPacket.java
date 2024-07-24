package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record TransferPacket(
        @NotNull String host,
        int port
) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<TransferPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, TransferPacket::host,
            NetworkBuffer.VAR_INT, TransferPacket::port,
            TransferPacket::new);
}
