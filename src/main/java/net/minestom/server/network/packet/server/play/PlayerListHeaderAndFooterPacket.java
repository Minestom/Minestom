package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class PlayerListHeaderAndFooterPacket implements ServerPacket {

    private static final String EMPTY_COMPONENT = "{\"translate\":\"\"}";

    public boolean emptyHeader;
    public boolean emptyFooter;

    public JsonMessage header; // Only text
    public JsonMessage footer; // Only text


    @Override
    public void write(BinaryWriter writer) {
        if (emptyHeader) {
            writer.writeSizedString(EMPTY_COMPONENT);
        } else {
            writer.writeSizedString(header.toString());
        }

        if (emptyFooter) {
            writer.writeSizedString(EMPTY_COMPONENT);
        } else {
            writer.writeSizedString(footer.toString());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
