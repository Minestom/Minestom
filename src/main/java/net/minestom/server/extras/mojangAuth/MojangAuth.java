package net.minestom.server.extras.mojangAuth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Stateless crypto primitives for Mojang online-mode login. Pure: no IO and no server state.
 * The session-server {@code hasJoined} call lives in {@link net.minestom.server.utils.mojang.MojangUtils};
 * a caller {@linkplain #verifyEncryptionResponse verifies} the encryption response here, hashes the
 * {@linkplain #serverIdHash server id}, fetches the profile JSON, then {@linkplain #parseProfile parses} it.
 */
public final class MojangAuth {
    private MojangAuth() {
    }

    /**
     * Username + 4-byte verify-token nonce issued at LoginStart, consumed at EncryptionResponse.
     */
    public record LoginChallenge(String username, byte[] nonce) {
        public static LoginChallenge create(String username, RandomGenerator rng) {
            final byte[] nonce = new byte[4];
            rng.nextBytes(nonce);
            return new LoginChallenge(username, nonce);
        }
    }

    /**
     * The client's encryption response failed verification: bad verify token or undecryptable shared secret.
     */
    public static final class AuthException extends Exception {
        public AuthException(String message) {
            this(message, null);
        }

        public AuthException(String message, @Nullable Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Lowercase hex of {@code SHA-1(sharedSecret || serverPublicKey)}, the {@code serverId} for {@code hasJoined}.
     */
    public static String serverIdHash(PublicKey serverPublicKey, SecretKey sharedSecret) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(sharedSecret.getEncoded());
            digest.update(serverPublicKey.getEncoded());
            return new BigInteger(digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }

    /**
     * Parses a Mojang {@code hasJoined} JSON response into a {@link GameProfile}.
     */
    public static GameProfile parseProfile(JsonObject hasJoinedResponse) {
        final UUID uuid = UUID.fromString(hasJoinedResponse.get("id").getAsString().replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        final String name = hasJoinedResponse.get("name").getAsString();
        final List<GameProfile.Property> properties = new ArrayList<>();
        for (JsonElement element : hasJoinedResponse.get("properties").getAsJsonArray()) {
            final JsonObject obj = element.getAsJsonObject();
            final JsonElement signature = obj.get("signature");
            properties.add(new GameProfile.Property(
                    obj.get("name").getAsString(),
                    obj.get("value").getAsString(),
                    signature == null ? null : signature.getAsString()));
        }
        return new GameProfile(uuid, name, properties);
    }

    /**
     * Verifies the client's encryption response and returns the negotiated AES session key.
     * Checks the verify-token nonce, then decrypts the shared secret.
     * {@code clientHasPublicKey=true} forces rejection (legacy chat-signing path).
     */
    public static SecretKey verifyEncryptionResponse(
            KeyPair serverKeyPair,
            LoginChallenge challenge,
            ClientEncryptionResponsePacket response,
            boolean clientHasPublicKey
    ) throws AuthException {
        final byte[] decryptedVerifyToken;
        try {
            decryptedVerifyToken = MojangCrypt.decryptUsingKey(serverKeyPair.getPrivate(), response.encryptedVerifyToken());
        } catch (MojangCrypt.CryptoException e) {
            throw new AuthException("Failed to decrypt verify token", e);
        }
        if (clientHasPublicKey || !Arrays.equals(challenge.nonce(), decryptedVerifyToken)) {
            throw new AuthException("Verify token mismatch");
        }
        try {
            return MojangCrypt.decryptByteToSecretKey(serverKeyPair.getPrivate(), response.sharedSecret());
        } catch (MojangCrypt.CryptoException e) {
            throw new AuthException("Failed to decrypt shared secret", e);
        }
    }
}
