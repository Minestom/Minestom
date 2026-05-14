package net.minestom.server.extras.mojangAuth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Stateless primitives for Mojang online-mode login. Entry point: {@link #completeOnlineLogin}. */
public final class MojangAuth {
    private MojangAuth() {
    }

    /** Username + 4-byte verify-token nonce issued at LoginStart, consumed at EncryptionResponse. */
    public record LoginChallenge(String username, byte[] nonce) {
        public static LoginChallenge create(String username, RandomGenerator rng) {
            final byte[] nonce = new byte[4];
            rng.nextBytes(nonce);
            return new LoginChallenge(username, nonce);
        }
    }

    /** Resolved profile + AES session key to install on the connection. */
    public record AuthResult(GameProfile profile, SecretKey encryptionKey) {
    }

    /** Auth payload is invalid. Distinct from {@link IOException}, which signals a transport failure to Mojang. */
    public static final class AuthException extends Exception {
        public enum Reason {VERIFY_TOKEN_INVALID, SHARED_SECRET_INVALID}

        private final Reason reason;

        public AuthException(Reason reason, String message) {
            this(reason, message, null);
        }

        public AuthException(Reason reason, String message, @Nullable Throwable cause) {
            super(message, cause);
            this.reason = reason;
        }

        public Reason reason() {
            return reason;
        }
    }

    /** Seam for the Mojang {@code hasJoined} call. Production: {@code MojangUtils::authenticateSession}. */
    @FunctionalInterface
    public interface SessionClient {
        JsonObject hasJoined(String username, String serverId, @Nullable InetAddress clientIp) throws IOException;
    }

    /** Lowercase hex of {@code SHA-1("" || serverPublicKey || sharedSecret)}, the {@code serverId} for {@code hasJoined}. */
    public static String serverIdHash(PublicKey serverPublicKey, SecretKey sharedSecret) {
        return new BigInteger(MojangCrypt.digestData("", serverPublicKey, sharedSecret)).toString(16);
    }

    /** Parses a Mojang {@code hasJoined} JSON response into a {@link GameProfile}. */
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
     * Verify-token check, shared-secret decryption, Mojang {@code hasJoined}, profile parse.
     * {@code clientHasPublicKey=true} forces rejection (legacy chat-signing path).
     * {@code clientIp} is forwarded to Mojang for {@code AUTH_PREVENT_PROXY_CONNECTIONS} when non-null.
     */
    public static AuthResult completeOnlineLogin(
            KeyPair serverKeyPair,
            LoginChallenge challenge,
            ClientEncryptionResponsePacket response,
            boolean clientHasPublicKey,
            @Nullable InetAddress clientIp,
            SessionClient sessionClient
    ) throws AuthException, IOException {
        final byte[] decryptedVerifyToken;
        try {
            decryptedVerifyToken = MojangCrypt.decryptUsingKey(serverKeyPair.getPrivate(), response.encryptedVerifyToken());
        } catch (MojangCrypt.CryptoException e) {
            throw new AuthException(AuthException.Reason.VERIFY_TOKEN_INVALID, "Failed to decrypt verify token", e);
        }
        if (clientHasPublicKey || !Arrays.equals(challenge.nonce(), decryptedVerifyToken)) {
            throw new AuthException(AuthException.Reason.VERIFY_TOKEN_INVALID, "Verify token mismatch");
        }

        final SecretKey secretKey;
        try {
            secretKey = MojangCrypt.decryptByteToSecretKey(serverKeyPair.getPrivate(), response.sharedSecret());
        } catch (MojangCrypt.CryptoException e) {
            throw new AuthException(AuthException.Reason.SHARED_SECRET_INVALID, "Failed to decrypt shared secret", e);
        }

        final String serverId = serverIdHash(serverKeyPair.getPublic(), secretKey);
        final JsonObject hasJoined = sessionClient.hasJoined(challenge.username(), serverId, clientIp);
        return new AuthResult(parseProfile(hasJoined), secretKey);
    }
}
