package net.minestom.server.extras.mojangAuth;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public final class MojangCrypt {
    private MojangCrypt() {
    }

    public static final class CryptoException extends RuntimeException {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA not available", e);
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bytes) {
        return new SecretKeySpec(decryptUsingKey(privateKey, bytes), "AES");
    }

    public static byte[] decryptUsingKey(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Cipher " + key.getAlgorithm() + " not available", e);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

    public static Cipher getCipher(int mode, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(mode, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES/CFB8/NoPadding not available", e);
        }
    }
}
