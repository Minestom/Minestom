package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientChatMessagePacket(@NotNull String message,
                                      long timestamp, long salt, @NotNull MessageSignature signature,
                                      boolean signedPreview,
                                      @NotNull LastSeenMessages.Update lastSeenMessages) implements ClientPacket {
    public ClientChatMessagePacket {
        if (message.length() > 256) {
            throw new IllegalArgumentException("Message cannot be more than 256 characters long.");
        }
    }

    public ClientChatMessagePacket(BinaryReader reader) {
        this(reader.readSizedString(256),
                reader.readLong(), reader.readLong(), new MessageSignature(reader),
                reader.readBoolean(),
                new LastSeenMessages.Update(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message);
        writer.writeLong(timestamp);
        writer.writeLong(salt);
        writer.write(signature);
        writer.writeBoolean(signedPreview);
        writer.write(lastSeenMessages);
    }
}
