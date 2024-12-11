package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record DisplayScoreboardPacket(byte position, String scoreName) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DisplayScoreboardPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, DisplayScoreboardPacket::position,
            STRING, DisplayScoreboardPacket::scoreName,
            DisplayScoreboardPacket::new);
}
