package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.INT;

public record PingPacket(int id) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<PingPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, PingPacket::id, PingPacket::new);
}
