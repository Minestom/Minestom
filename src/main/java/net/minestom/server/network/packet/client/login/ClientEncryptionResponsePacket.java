package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;

public record ClientEncryptionResponsePacket(byte @NotNull [] sharedSecret,
                                             byte @NotNull [] encryptedVerifyToken) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientEncryptionResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE_ARRAY, ClientEncryptionResponsePacket::sharedSecret,
            BYTE_ARRAY, ClientEncryptionResponsePacket::encryptedVerifyToken,
            ClientEncryptionResponsePacket::new);

    public ClientEncryptionResponsePacket {
        sharedSecret = sharedSecret.clone();
        encryptedVerifyToken = encryptedVerifyToken.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ClientEncryptionResponsePacket(byte[] secret, byte[] verifyToken))) return false;
        return Arrays.equals(sharedSecret(), secret) && Arrays.equals(encryptedVerifyToken(), verifyToken);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sharedSecret());
        result = 31 * result + Arrays.hashCode(encryptedVerifyToken());
        return result;
    }
}
