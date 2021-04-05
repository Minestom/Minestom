package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientSetRecipeBookStatePacket extends ClientPlayPacket {

    public BookType type = BookType.CRAFTING;
    public boolean bookOpen;
    public boolean filterActive;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.type = BookType.values()[reader.readVarInt()];
        this.bookOpen = reader.readBoolean();
        this.filterActive = reader.readBoolean();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(type.ordinal());
        writer.writeBoolean(bookOpen);
        writer.writeBoolean(filterActive);
    }

    public enum BookType {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
