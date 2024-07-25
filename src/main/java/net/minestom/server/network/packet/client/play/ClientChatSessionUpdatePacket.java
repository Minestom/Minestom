package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.ChatSession;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientChatSessionUpdatePacket(@NotNull ChatSession chatSession) implements ClientPacket {
    public static NetworkBuffer.Type<ClientChatSessionUpdatePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientChatSessionUpdatePacket value) {
            buffer.write(value.chatSession);
        }

        @Override
        public ClientChatSessionUpdatePacket read(@NotNull NetworkBuffer buffer) {
            return new ClientChatSessionUpdatePacket(new ChatSession(buffer));
        }
    };
}
