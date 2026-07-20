package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.scoreboard.DisplaySlot;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record DisplayScoreboardPacket(DisplaySlot position, String scoreName) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DisplayScoreboardPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(DisplaySlot.class), DisplayScoreboardPacket::position,
            STRING, DisplayScoreboardPacket::scoreName,
            DisplayScoreboardPacket::new);
}
