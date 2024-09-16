package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientCommandChatPacket(@NotNull String message) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCommandChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientCommandChatPacket::message,
            ClientCommandChatPacket::new);

    public ClientCommandChatPacket {
        Check.argCondition(message.length() > 256, "Message length cannot be greater than 256");
    }
}
