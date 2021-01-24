package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SelectAdvancementTabPacket implements ServerPacket {

    public String identifier;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        final boolean hasId = identifier != null;

        writer.writeBoolean(hasId);
        if (hasId) {
            writer.writeSizedString(identifier);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SELECT_ADVANCEMENT_TAB;
    }
}
