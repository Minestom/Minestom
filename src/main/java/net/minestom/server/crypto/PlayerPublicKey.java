package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.security.PublicKey;
import java.time.Instant;

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
}
