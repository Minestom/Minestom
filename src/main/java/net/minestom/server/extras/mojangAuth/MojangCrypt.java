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
         KeyPairGenerator keyPairGenerator1 = KeyPairGenerator.getInstance("RSA");
         keyPairGenerator1.initialize(1024);
         return keyPairGenerator1.generateKeyPair();
      } catch (NoSuchAlgorithmException var1) {
         var1.printStackTrace();
         LOGGER.error("Key pair generation failed!");
         return null;
      }
   }

   public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) {
      try {
         return digestData("SHA-1", string.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
      } catch (UnsupportedEncodingException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   private static byte[] digestData(String string, byte[]... arr) {
      try {
         MessageDigest messageDigest3 = MessageDigest.getInstance(string);

         for(byte[] arr7 : arr) {
            messageDigest3.update(arr7);
         }

         return messageDigest3.digest();
      } catch (NoSuchAlgorithmException var7) {
         var7.printStackTrace();
         return null;
      }
   }

   public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] arr) {
      return new SecretKeySpec(decryptUsingKey(privateKey, arr), "AES");
   }

   public static byte[] decryptUsingKey(Key key, byte[] arr) {
      return cipherData(2, key, arr);
   }

   private static byte[] cipherData(int integer, Key key, byte[] arr) {
      try {
         return setupCipher(integer, key.getAlgorithm(), key).doFinal(arr);
      } catch (IllegalBlockSizeException var4) {
         var4.printStackTrace();
      } catch (BadPaddingException var5) {
         var5.printStackTrace();
      }

      LOGGER.error("Cipher data failed!");
      return null;
   }

   private static Cipher setupCipher(int integer, String string, Key key) {
      try {
         Cipher cipher4 = Cipher.getInstance(string);
         cipher4.init(integer, key);
         return cipher4;
      } catch (InvalidKeyException var4) {
         var4.printStackTrace();
      } catch (NoSuchAlgorithmException var5) {
         var5.printStackTrace();
      } catch (NoSuchPaddingException var6) {
         var6.printStackTrace();
      }

      LOGGER.error("Cipher creation failed!");
      return null;
   }

   public static Cipher getCipher(int integer, Key key) {
      try {
         Cipher cipher3 = Cipher.getInstance("AES/CFB8/NoPadding");
         cipher3.init(integer, key, new IvParameterSpec(key.getEncoded()));
         return cipher3;
      } catch (GeneralSecurityException var3) {
         throw new RuntimeException(var3);
      }
   }
}
