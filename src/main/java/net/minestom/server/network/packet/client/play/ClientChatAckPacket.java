package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientChatAckPacket(@NotNull LastSeenMessages.Update update) implements ClientPacket {
    public ClientChatAckPacket(NetworkBuffer reader) {
        this(new LastSeenMessages.Update(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.write(update);
    }
}
