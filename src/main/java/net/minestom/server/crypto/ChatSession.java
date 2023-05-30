package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChatSession(@NotNull UUID sessionId, @NotNull PlayerPublicKey publicKey) implements NetworkBuffer.Writer {
    public ChatSession(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), new PlayerPublicKey(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, sessionId);
        writer.write(publicKey);
    }
}
