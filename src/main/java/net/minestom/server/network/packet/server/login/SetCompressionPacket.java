package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetCompressionPacket(int threshold) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<SetCompressionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetCompressionPacket::threshold,
            SetCompressionPacket::new);
}
