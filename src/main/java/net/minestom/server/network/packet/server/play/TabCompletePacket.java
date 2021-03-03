package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class TabCompletePacket implements ServerPacket {

    public int transactionId;
    public int start;
    public int length;
    public Match[] matches;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(start);
        writer.writeVarInt(length);

        writer.writeVarInt(matches.length);
        for (Match match : matches) {
            writer.writeSizedString(match.match);
            writer.writeBoolean(match.hasTooltip);
            if (match.hasTooltip)
                writer.writeSizedString(match.tooltipJson != null ? match.tooltipJson.toString() : match.tooltip);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAB_COMPLETE;
    }

    public static class Match {
        public String match;
        public boolean hasTooltip;
        public String tooltip;

        /**
         * @deprecated Use {@link #tooltip}
         */
        @Deprecated public String tooltipJson;
    }

}
