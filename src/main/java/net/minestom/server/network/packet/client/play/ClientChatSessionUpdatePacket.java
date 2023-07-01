package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ChatSession;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientChatSessionUpdatePacket(@NotNull ChatSession chatSession) implements ClientPacket {
    public ClientChatSessionUpdatePacket(@NotNull NetworkBuffer reader) {
        this(new ChatSession(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(chatSession);
    }
}
