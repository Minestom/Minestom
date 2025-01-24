package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record PingResponsePacket(long number) implements ServerPacket.Status, ServerPacket.Play {
    public static final NetworkBuffer.Type<PingResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, PingResponsePacket::number,
            PingResponsePacket::new);
}
