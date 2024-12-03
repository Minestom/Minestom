package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ArgumentSignatures;
import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientSignedCommandChatPacket(@NotNull String message, long timestamp,
                                            long salt, @NotNull ArgumentSignatures signatures,
                                            LastSeenMessages.@NotNull Update lastSeenMessages) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSignedCommandChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientSignedCommandChatPacket::message,
            LONG, ClientSignedCommandChatPacket::timestamp,
            LONG, ClientSignedCommandChatPacket::salt,
            ArgumentSignatures.SERIALIZER, ClientSignedCommandChatPacket::signatures,
            LastSeenMessages.Update.SERIALIZER, ClientSignedCommandChatPacket::lastSeenMessages,
            ClientSignedCommandChatPacket::new
    );

    public ClientSignedCommandChatPacket {
        Check.argCondition(message.length() > 256, "Message length cannot be greater than 256");
    }
}
