package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SelectAdvancementTabPacket(@Nullable String identifier) implements ServerPacket {
    public SelectAdvancementTabPacket(BinaryReader reader) {
        this(reader.readBoolean() ? reader.readSizedString() : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(identifier != null);
        if (identifier != null) writer.writeSizedString(identifier);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SELECT_ADVANCEMENT_TAB;
    }
}
