package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record SelectAdvancementTabPacket(String identifier) implements ServerPacket {
    public SelectAdvancementTabPacket(BinaryReader reader) {
        this(reader.readBoolean() ? reader.readSizedString() : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        final boolean hasId = identifier != null;
        writer.writeBoolean(hasId);
        if (hasId) writer.writeSizedString(identifier);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SELECT_ADVANCEMENT_TAB;
    }
}
