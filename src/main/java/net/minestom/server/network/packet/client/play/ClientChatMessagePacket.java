package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientChatMessagePacket(@NotNull String message, MessageSignature signature, boolean signedPreview) implements ClientPacket {
    public ClientChatMessagePacket {
        if (message.length() > 256) {
            throw new IllegalArgumentException("Message cannot be more than 256 characters long.");
        }
    }

    public ClientChatMessagePacket(BinaryReader reader) {
        this(reader.readSizedString(256), new MessageSignature(reader), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message);
        writer.write(signature);
        writer.writeBoolean(signedPreview);
    }
}
