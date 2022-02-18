package net.minestom.server.extras.mojangAuth;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

public final class MojangCrypt {
    private static final Logger LOGGER = LoggerFactory.getLogger(MojangCrypt.class);

    public static @Nullable KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            LOGGER.error("Key pair generation failed!");
            return null;
        }
    }

    public static byte @Nullable [] digestData(String data, PublicKey publicKey, SecretKey secretKey) {
        try {
            return digestData("SHA-1", data.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (UnsupportedEncodingException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    private static byte @Nullable [] digestData(String algorithm, byte[]... data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            for (byte[] bytes : data) {
                digest.update(bytes);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bytes) {
        return new SecretKeySpec(decryptUsingKey(privateKey, bytes), "AES");
    }

    public static byte[] decryptUsingKey(Key key, byte[] bytes) {
        return cipherData(2, key, bytes);
    }

    private static byte[] cipherData(int mode, Key key, byte[] data) {
        try {
            return setupCipher(mode, key.getAlgorithm(), key).doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException var4) {
            MinecraftServer.getExceptionManager().handleException(var4);
        }
        LOGGER.error("Cipher data failed!");
        return null;
    }

    private static Cipher setupCipher(int mode, String transformation, Key key) {
        try {
            Cipher cipher4 = Cipher.getInstance(transformation);
            cipher4.init(mode, key);
            return cipher4;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException var4) {
            MinecraftServer.getExceptionManager().handleException(var4);
        }
        LOGGER.error("Cipher creation failed!");
        return null;
    }

    public static Cipher getCipher(int mode, Key key) {
        try {
            Cipher cipher3 = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher3.init(mode, key, new IvParameterSpec(key.getEncoded()));
            return cipher3;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
