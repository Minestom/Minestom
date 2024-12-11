package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClearTitlesPacket(boolean reset) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ClearTitlesPacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, ClearTitlesPacket::reset,
            ClearTitlesPacket::new);
}
