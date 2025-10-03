package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ArgumentSignatures;
import net.minestom.server.crypto.LastSeenMessages;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientSignedCommandChatPacket(String message, long timestamp,
                                            long salt, ArgumentSignatures signatures,
                                            LastSeenMessages.Update lastSeenMessages,
                                            byte checksum) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSignedCommandChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientSignedCommandChatPacket::message,
            LONG, ClientSignedCommandChatPacket::timestamp,
            LONG, ClientSignedCommandChatPacket::salt,
            ArgumentSignatures.SERIALIZER, ClientSignedCommandChatPacket::signatures,
            LastSeenMessages.Update.SERIALIZER, ClientSignedCommandChatPacket::lastSeenMessages,
            BYTE, ClientSignedCommandChatPacket::checksum,
            ClientSignedCommandChatPacket::new
    );

    public ClientSignedCommandChatPacket {
        Check.argCondition(message.length() > 256, "Message length cannot be greater than 256");
    }
}
