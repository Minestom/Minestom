package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientEditBookPacket(int slot, @NotNull List<String> pages,
                                   @Nullable String title) implements ClientPacket {
    public ClientEditBookPacket {
        pages = List.copyOf(pages);
        if (title != null && title.length() > 128) {
            throw new IllegalArgumentException("Title length cannot be greater than 128");
        }
    }

    public ClientEditBookPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readCollection(STRING),
                reader.readOptional(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, slot);
        writer.writeCollection(STRING, pages);
        writer.writeOptional(STRING, title);
    }
}
