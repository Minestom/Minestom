package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;

public record WorldBorderSizePacket(double diameter) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<WorldBorderSizePacket> SERIALIZER = NetworkBufferTemplate.template(
            DOUBLE, WorldBorderSizePacket::diameter,
            WorldBorderSizePacket::new);
}
