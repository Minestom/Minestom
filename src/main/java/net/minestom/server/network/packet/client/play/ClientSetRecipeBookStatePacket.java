package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientSetRecipeBookStatePacket(@NotNull BookType bookType,
                                             boolean bookOpen, boolean filterActive) implements ClientPacket {
    public ClientSetRecipeBookStatePacket(BinaryReader reader) {
        this(BookType.values()[reader.readVarInt()], reader.readBoolean(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(bookType.ordinal());
        writer.writeBoolean(bookOpen);
        writer.writeBoolean(filterActive);
    }

    public enum BookType {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
