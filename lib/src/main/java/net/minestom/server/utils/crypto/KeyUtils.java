package net.minestom.server.utils.crypto;

import org.jetbrains.annotations.ApiStatus;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@ApiStatus.Internal
public final class KeyUtils {
    private static final Base64.Encoder MIME_ENCODER = Base64.getMimeEncoder(76, "\n".getBytes(StandardCharsets.UTF_8));
    private static final String RSA_HEADER = "-----BEGIN RSA PUBLIC KEY-----\n";
    private static final String RSA_FOOTER = "\n-----END RSA PUBLIC KEY-----\n";

    public enum SignatureAlgorithm {
        SHA256withRSA,
        SHA1withRSA
    }

    public enum KeyAlgorithm {
        RSA
    }

    private KeyUtils() {
        //no instance
    }

    public static String rsaPublicKeyToString(PublicKey publicKey) {
        if (!publicKey.getAlgorithm().equals(KeyAlgorithm.RSA.name())) {
            throw new IllegalArgumentException("The provided key isn't an RSA key!");
        } else {
            return RSA_HEADER + MIME_ENCODER.encodeToString(publicKey.getEncoded()) + RSA_FOOTER;
        }
    }

    public static PublicKey publicRSAKeyFrom(byte[] data) {
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        final KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(KeyAlgorithm.RSA.name());
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
