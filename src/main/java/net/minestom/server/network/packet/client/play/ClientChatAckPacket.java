package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientChatAckPacket(@NotNull LastSeenMessages.Update update) implements ClientPacket {
    public ClientChatAckPacket(@NotNull NetworkBuffer reader) {
        this(new LastSeenMessages.Update(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(update);
    }
}
