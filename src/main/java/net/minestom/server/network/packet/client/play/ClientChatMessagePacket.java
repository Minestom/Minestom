package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientChatMessagePacket(@NotNull String message,
                                      long timestamp, long salt, @NotNull MessageSignature signature,
                                      boolean signedPreview,
                                      @NotNull LastSeenMessages.Update lastSeenMessages) implements ClientPacket {
    public ClientChatMessagePacket {
        if (message.length() > 256) {
            throw new IllegalArgumentException("Message cannot be more than 256 characters long.");
        }
    }

    public ClientChatMessagePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING),
                reader.read(LONG), reader.read(LONG), new MessageSignature(reader),
                reader.read(BOOLEAN),
                new LastSeenMessages.Update(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, message);
        writer.write(LONG, timestamp);
        writer.write(LONG, salt);
        writer.write(signature);
        writer.write(BOOLEAN, signedPreview);
        writer.write(lastSeenMessages);
    }
}
