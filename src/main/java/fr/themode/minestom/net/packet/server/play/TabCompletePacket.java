package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class TabCompletePacket implements ServerPacket {

    public int transactionId;
    public int start;
    public int length;
    public Match[] matches;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(start);
        writer.writeVarInt(length);

        writer.writeVarInt(matches.length);
        for (Match match : matches) {
            writer.writeSizedString(match.match);
            writer.writeBoolean(match.hasTooltip);
            if (match.hasTooltip)
                writer.writeSizedString(match.tooltip);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAB_COMPLETE;
    }

    public static class Match {
        public String match;
        public boolean hasTooltip;
        public String tooltip; // Chat
    }

}
