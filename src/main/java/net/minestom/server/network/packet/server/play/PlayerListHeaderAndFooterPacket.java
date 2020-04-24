package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class PlayerListHeaderAndFooterPacket implements ServerPacket {

    private static final String EMPTY_COMPONENT = "{\"translate\":\"\"}";

    public boolean emptyHeader;
    public boolean emptyFooter;

    public String header;
    public String footer;


    @Override
    public void write(PacketWriter writer) {
        if (emptyHeader) {
            writer.writeSizedString(EMPTY_COMPONENT);
        } else {
            writer.writeSizedString(header);
        }

        if (emptyFooter) {
            writer.writeSizedString(EMPTY_COMPONENT);
        } else {
            writer.writeSizedString(footer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
