package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

public class PlayerListHeaderAndFooterPacket implements ServerPacket {
    public String header;
    public String footer;

    public PlayerListHeaderAndFooterPacket(@NotNull String header, @NotNull String footer) {
        Validate.notNull(header, "Header cannot be null.");
        Validate.notNull(footer, "Footer cannot be null.");
        this.header = header;
        this.footer = footer;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(header);
        writer.writeSizedString(footer);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
