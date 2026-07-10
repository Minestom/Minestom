package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record KeepAlivePacket(long id) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<KeepAlivePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, KeepAlivePacket::id, KeepAlivePacket::new);
}
