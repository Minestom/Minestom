package net.minestom.server.extras.velocity;

import net.minestom.server.extras.MojangAuth;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Support for <a href="https://velocitypowered.com/">Velocity</a> modern forwarding.
 * <p>
 * Can be enabled by simply calling {@link #enable(String)}.
 */
public final class VelocityProxy {
    public static final String PLAYER_INFO_CHANNEL = "velocity:player_info";
    private static final int SUPPORTED_FORWARDING_VERSION = 1;
    private static final String MAC_ALGORITHM = "HmacSHA256";

    private static volatile boolean enabled;
    private static Key key;

    /**
     * Enables velocity modern forwarding.
     *
     * @param secret the forwarding secret,
     *               be sure to do not hardcode it in your code but to retrieve it from a file or anywhere else safe
     */
    public static void enable(@NotNull String secret) {
        Check.stateCondition(enabled, "Velocity modern forwarding is already enabled");
        Check.stateCondition(MojangAuth.isEnabled(), "Velocity modern forwarding should not be enabled with MojangAuth");

        VelocityProxy.enabled = true;
        VelocityProxy.key = new SecretKeySpec(secret.getBytes(), MAC_ALGORITHM);
    }

    /**
     * Gets if velocity modern forwarding is enabled.
     *
     * @return true if velocity modern forwarding is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean checkIntegrity(@NotNull NetworkBuffer buffer) {
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
}
