package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ArgumentSignatures;
import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientCommandChatPacket(@NotNull String message, long timestamp,
                                      long salt, @NotNull ArgumentSignatures signatures,
                                      boolean signedPreview,
                                      LastSeenMessages.@NotNull Update lastSeenMessages) implements ClientPacket {
    public ClientCommandChatPacket(NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(LONG),
                reader.read(LONG), new ArgumentSignatures(reader), reader.read(BOOLEAN), new LastSeenMessages.Update(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message);
        writer.writeLong(timestamp);
        writer.writeLong(salt);
        writer.write(signatures);
        writer.writeBoolean(signedPreview);
        writer.write(lastSeenMessages);
    }
}
