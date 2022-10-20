package net.minestom.server.network.packet.client.play;

import net.minestom.server.crypto.PlayerPublicKey;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ClientChatSessionUpdatePacket(@NotNull UUID sessionId,
                                            @Nullable PlayerPublicKey publicKey) implements ClientPacket {
    public ClientChatSessionUpdatePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), new PlayerPublicKey(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, sessionId);
        writer.writeOptional(publicKey);
    }
}
