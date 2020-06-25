package net.minestom.server.extras.mojangAuth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class MojangCrypt {
   private static final Logger LOGGER = LogManager.getLogger();

   public static KeyPair generateKeyPair() {
      try {
         KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
         keyGen.initialize(1024);
         return keyGen.generateKeyPair();
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
         LOGGER.error("Key pair generation failed!");
         return null;
      }
   }

   public static byte[] digestData(String data, PublicKey publicKey, SecretKey secretKey) {
      try {
         return digestData("SHA-1", data.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
      } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
         return null;
      }
   }

   private static byte[] digestData(String algorithm, byte[]... data) {
      try {
         MessageDigest digest = MessageDigest.getInstance(algorithm);

         for(byte[] bytes : data) {
            digest.update(bytes);
         }

         return digest.digest();
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
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
         var4.printStackTrace();
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
         var4.printStackTrace();
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
