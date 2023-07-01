package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ArgumentSignatures;
import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientCommandChatPacket(@NotNull String message, long timestamp,
                                      long salt, @NotNull ArgumentSignatures signatures,
                                      LastSeenMessages.@NotNull Update lastSeenMessages) implements ClientPacket {
    public ClientCommandChatPacket {
        if (message.length() > 256) {
            throw new IllegalArgumentException("Message length cannot be greater than 256");
        }
    }

    public ClientCommandChatPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(LONG),
                reader.read(LONG), new ArgumentSignatures(reader),
                new LastSeenMessages.Update(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, message);
        writer.write(LONG, timestamp);
        writer.write(LONG, salt);
        writer.write(signatures);
        writer.write(lastSeenMessages);
    }
}
