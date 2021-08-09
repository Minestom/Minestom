package net.minestom.server.extras.velocity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static boolean checkIntegrity(@NotNull BinaryReader reader) {
        final byte[] signature = reader.readBytes(32);
        ByteBuffer buf = reader.getBuffer();
        buf.mark();
        final byte[] data = reader.readRemainingBytes();
        buf.reset();
        try {
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            mac.init(key);
            final byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature)) {
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        final int version = reader.readVarInt();
        return version == SUPPORTED_FORWARDING_VERSION;
    }

    public static InetAddress readAddress(@NotNull BinaryReader reader) {
        try {
            return InetAddress.getByName(reader.readSizedString());
        } catch (UnknownHostException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    public static PlayerSkin readSkin(@NotNull BinaryReader reader) {
        String skinTexture = null;
        String skinSignature = null;
        final int properties = reader.readVarInt();
        for (int i = 0; i < properties; i++) {
            final String name = reader.readSizedString(Short.MAX_VALUE);
            final String value = reader.readSizedString(Short.MAX_VALUE);
            final String signature = reader.readBoolean() ? reader.readSizedString(Short.MAX_VALUE) : null;

            if (name.equals("textures")) {
                skinTexture = value;
                skinSignature = signature;
            }
        }

        if (skinTexture != null && skinSignature != null) {
            return new PlayerSkin(skinTexture, skinSignature);
        } else {
            return null;
        }
    }
}
