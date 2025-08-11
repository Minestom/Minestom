package net.minestom.server;

import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Objects;
import java.util.Set;

import static net.minestom.server.network.NetworkBuffer.*;

public sealed interface Auth {
    record Offline() implements Auth {
    }

    record Online(KeyPair keyPair) implements Auth {
        public Online() {
            this(Objects.requireNonNull(MojangCrypt.generateKeyPair()));
        }
    }

    record Velocity(Key key) implements Auth {
        public static final String PLAYER_INFO_CHANNEL = "velocity:player_info";
        private static final String MAC_ALGORITHM = "HmacSHA256";
        private static final int SUPPORTED_FORWARDING_VERSION = 1;

        public Velocity(String secret) {
            this(secretKey(secret));
        }

        public boolean checkIntegrity(NetworkBuffer buffer) {
            final byte[] signature = new byte[32];
            for (int i = 0; i < signature.length; i++) {
                signature[i] = buffer.read(BYTE);
            }
            final long index = buffer.readIndex();
            final byte[] data = buffer.read(RAW_BYTES);
            buffer.readIndex(index);
            try {
                Mac mac = Mac.getInstance(MAC_ALGORITHM);
                mac.init(key);
                final byte[] mySignature = mac.doFinal(data);
                if (!MessageDigest.isEqual(signature, mySignature)) {
                    return false;
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            final int version = buffer.read(VAR_INT);
            return version == SUPPORTED_FORWARDING_VERSION;
        }

        public static Key secretKey(String secret) {
            return new SecretKeySpec(secret.getBytes(), MAC_ALGORITHM);
        }
    }

    record Bungee(@Nullable Set<String> bungeeGuardTokens) implements Auth {
        public Bungee {
            if (bungeeGuardTokens != null && bungeeGuardTokens.isEmpty()) {
                throw new IllegalArgumentException("BungeeGuard tokens cannot be empty");
            }
        }

        public Bungee() {
            this(null);
        }

        public boolean validToken(String token) {
            return bungeeGuardTokens == null || bungeeGuardTokens.contains(token);
        }

        public boolean guard() {
            return bungeeGuardTokens != null;
        }
    }
}
