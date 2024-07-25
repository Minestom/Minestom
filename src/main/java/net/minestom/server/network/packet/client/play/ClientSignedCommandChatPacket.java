package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ArgumentSignatures;
import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientSignedCommandChatPacket(@NotNull String message, long timestamp,
                                            long salt, @NotNull ArgumentSignatures signatures,
                                            LastSeenMessages.@NotNull Update lastSeenMessages) implements ClientPacket {
    public static NetworkBuffer.Type<ClientSignedCommandChatPacket> SERIALIZER = new NetworkBuffer.Type<ClientSignedCommandChatPacket>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientSignedCommandChatPacket value) {
            buffer.write(STRING, value.message);
            buffer.write(LONG, value.timestamp);
            buffer.write(LONG, value.salt);
            buffer.write(value.signatures);
            buffer.write(value.lastSeenMessages);
        }

        @Override
        public ClientSignedCommandChatPacket read(@NotNull NetworkBuffer buffer) {
            return new ClientSignedCommandChatPacket(buffer.read(STRING), buffer.read(LONG),
                    buffer.read(LONG), new ArgumentSignatures(buffer),
                    new LastSeenMessages.Update(buffer));
        }
    };

    public ClientSignedCommandChatPacket {
        Check.argCondition(message.length() > 256, "Message length cannot be greater than 256");
    }
}
