package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientSetRecipeBookStatePacket(@NotNull BookType bookType,
                                             boolean bookOpen, boolean filterActive) implements ClientPacket {
    public ClientSetRecipeBookStatePacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(BookType.class), reader.read(BOOLEAN), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(BookType.class, bookType);
        writer.write(BOOLEAN, bookOpen);
        writer.write(BOOLEAN, filterActive);
    }

    public enum BookType {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
