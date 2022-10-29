package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientChatPreviewPacket(int queryId, @NotNull String query) implements ClientPacket {
    public ClientChatPreviewPacket {
        if (query.length() > 256) {
            throw new IllegalArgumentException("Query length cannot be greater than 256");
        }
    }

    public ClientChatPreviewPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, queryId);
        writer.write(STRING, query);
    }
}
