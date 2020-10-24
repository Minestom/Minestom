package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class TabCompletePacket implements ServerPacket {

    public int transactionId;
    public int start;
    public int length;
    public Match[] matches;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(start);
        writer.writeVarInt(length);

        writer.writeVarInt(matches.length);
        for (Match match : matches) {
            writer.writeSizedString(match.match);
            writer.writeBoolean(match.hasTooltip);
            if (match.hasTooltip)
                writer.writeSizedString(match.tooltip.toString());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAB_COMPLETE;
    }

    public static class Match {
        public String match;
        public boolean hasTooltip;
        public JsonMessage tooltip; // Only text
    }

}
