package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.UUID;

public record ChatSession(@NotNull UUID sessionId, @NotNull PlayerPublicKey publicKey) {
    public static final NetworkBuffer.Type<ChatSession> SERIALIZER = NetworkBufferTemplate.template(
            UUID, ChatSession::sessionId,
            PlayerPublicKey.SERIALIZER, ChatSession::publicKey,
            ChatSession::new
    );
}
