package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.*;

public record EncryptionRequestPacket(
        String serverId,
        byte [] publicKey,
        byte [] verifyToken,
        boolean shouldAuthenticate
) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<EncryptionRequestPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, EncryptionRequestPacket::serverId,
            BYTE_ARRAY, EncryptionRequestPacket::publicKey,
            BYTE_ARRAY, EncryptionRequestPacket::verifyToken,
            BOOLEAN, EncryptionRequestPacket::shouldAuthenticate,
            EncryptionRequestPacket::new);

    public EncryptionRequestPacket {
        publicKey = publicKey.clone();
        verifyToken = verifyToken.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EncryptionRequestPacket(String id, byte[] key, byte[] token, boolean authenticate))) return false;
        return shouldAuthenticate() == authenticate && serverId().equals(id) && Arrays.equals(publicKey(), key) && Arrays.equals(verifyToken(), token);
    }

    @Override
    public int hashCode() {
        int result = serverId().hashCode();
        result = 31 * result + Arrays.hashCode(publicKey());
        result = 31 * result + Arrays.hashCode(verifyToken());
        result = 31 * result + Boolean.hashCode(shouldAuthenticate());
        return result;
    }
}
