package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ChatSession;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientChatSessionUpdatePacket(@NotNull ChatSession chatSession) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChatSessionUpdatePacket> SERIALIZER = NetworkBufferTemplate.template(
            ChatSession.SERIALIZER, ClientChatSessionUpdatePacket::chatSession,
            ClientChatSessionUpdatePacket::new
    );
}
