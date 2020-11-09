package net.minestom.server.extras.velocity;

import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VelocityProxy {

    public static final String PLAYER_INFO_CHANNEL = "velocity:player_info";

    private static boolean enabled;
    private static byte[] secret;

    /**
     * Enables velocity modern forwarding.
     *
     * @param secret the forwarding secret,
     *               be sure to do not hardcode it in your code but to retrieve it from a file or anywhere else safe
     */
    public static void enable(@NotNull String secret) {
        VelocityProxy.enabled = true;
        VelocityProxy.secret = secret.getBytes();
    }

    /**
     * Gets if velocity modern forwarding is enabled.
     *
     * @return true if velocity modern forwarding is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean checkIntegrity(@NotNull BinaryReader reader) {

        if (!enabled) {
            return false;
        }

        final byte[] signature = reader.readBytes(32);

        final byte[] data = reader.getRemainingBytes();

        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            final byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature)) {
                return false;
            }
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        /*int version = buf.readVarInt();
        if (version != SUPPORTED_FORWARDING_VERSION) {
            throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted " + SUPPORTED_FORWARDING_VERSION);
        }*/

        return true;
    }

    private static void readProperties(final BinaryReader reader) {
        final int properties = reader.readVarInt();
        for (int i1 = 0; i1 < properties; i1++) {
            final String name = reader.readSizedString();
            final String value = reader.readSizedString();
            final String signature = reader.readBoolean() ? reader.readSizedString() : null;
            System.out.println("test: " + name + " " + value + " " + signature);
        }
    }

}
