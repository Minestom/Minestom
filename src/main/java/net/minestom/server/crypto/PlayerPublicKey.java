package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Player's public key used to sign chat messages
 */
public record PlayerPublicKey(Instant expiresAt, PublicKey publicKey, byte[] signature) {
    public static final NetworkBuffer.Type<PlayerPublicKey> SERIALIZER = NetworkBufferTemplate.template(
            INSTANT_MS, PlayerPublicKey::expiresAt,
            PUBLIC_KEY, PlayerPublicKey::publicKey,
            BYTE_ARRAY, PlayerPublicKey::signature,
            PlayerPublicKey::new
    );

    public PlayerPublicKey {
        signature = signature.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerPublicKey(Instant at, PublicKey key, byte[] signature1))) return false;
        return Arrays.equals(signature(), signature1) && expiresAt().equals(at) && publicKey().equals(key);
    }

    @Override
    public int hashCode() {
        int result = expiresAt().hashCode();
        result = 31 * result + publicKey().hashCode();
        result = 31 * result + Arrays.hashCode(signature());
        return result;
    }
}
