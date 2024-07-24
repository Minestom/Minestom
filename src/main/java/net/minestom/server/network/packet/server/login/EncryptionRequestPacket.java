package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EncryptionRequestPacket(
        @NotNull String serverId,
        byte @NotNull [] publicKey,
        byte @NotNull [] verifyToken,
        boolean shouldAuthenticate
) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<EncryptionRequestPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, EncryptionRequestPacket::serverId,
            BYTE_ARRAY, EncryptionRequestPacket::publicKey,
            BYTE_ARRAY, EncryptionRequestPacket::verifyToken,
            BOOLEAN, EncryptionRequestPacket::shouldAuthenticate,
            EncryptionRequestPacket::new);
}
