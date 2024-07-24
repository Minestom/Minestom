package net.minestom.server.network.packet.server.status;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ResponsePacket(@NotNull String jsonResponse) implements ServerPacket.Status {
    public static final NetworkBuffer.Type<ResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ResponsePacket::jsonResponse,
            ResponsePacket::new);
}
