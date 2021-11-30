package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ClientEditBookPacket(int slot, @NotNull List<String> pages,
                                   @Nullable String title) implements ClientPacket {
    public ClientEditBookPacket {
        pages = List.copyOf(pages);
    }

    public ClientEditBookPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarIntList(BinaryReader::readSizedString),
                reader.readBoolean() ? reader.readSizedString(128) : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(slot);
        writer.writeVarIntList(pages, BinaryWriter::writeSizedString);
        writer.writeBoolean(title != null);
        if (title != null) writer.writeSizedString(title);
    }
}
