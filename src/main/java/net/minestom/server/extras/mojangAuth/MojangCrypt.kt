package net.minestom.server.extras.mojangAuth

import net.minestom.server.MinecraftServer.Companion.schedulerManager
import net.minestom.server.MinecraftServer.Companion.server
import net.minestom.server.utils.time.Cooldown.isReady
import net.minestom.server.utils.time.Cooldown.refreshLastUpdate
import net.minestom.server.MinecraftServer.Companion.exceptionManager
import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.MinecraftServer.Companion.biomeManager
import net.minestom.server.world.biomes.BiomeManager.addBiome
import net.minestom.server.world.biomes.Biome.Companion.builder
import net.minestom.server.world.biomes.Biome.Builder.name
import net.minestom.server.world.biomes.Biome.Builder.build
import net.minestom.server.MinecraftServer.Companion.connectionManager
import net.minestom.server.MinecraftServer.Companion.brandName
import net.minestom.server.MinecraftServer.Companion.extensionManager
import net.minestom.server.utils.binary.BinaryWriter.writeBytes
import net.minestom.server.utils.binary.BinaryWriter.writeNullTerminatedString
import net.minestom.server.utils.binary.BinaryWriter.buffer
import net.minestom.server.utils.binary.BinaryWriter.writeByte
import net.minestom.server.utils.binary.BinaryWriter.writeInt
import net.minestom.server.utils.binary.BinaryWriter.toByteArray
import net.minestom.server.utils.binary.Writeable.write
import net.minestom.server.utils.binary.BinaryReader.readBytes
import net.minestom.server.utils.binary.BinaryReader.buffer
import net.minestom.server.utils.binary.BinaryReader.readRemainingBytes
import net.minestom.server.utils.binary.BinaryReader.readVarInt
import net.minestom.server.utils.binary.BinaryReader.readSizedString
import net.minestom.server.utils.binary.BinaryReader.readBoolean
import java.net.InetSocketAddress
import net.minestom.server.utils.time.Cooldown
import java.net.DatagramSocket
import java.net.DatagramPacket
import net.minestom.server.extras.lan.OpenToLANConfig
import net.minestom.server.extras.lan.OpenToLAN
import java.net.SocketException
import net.minestom.server.MinecraftServer
import java.lang.Runnable
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.ping.ServerListPingType
import java.io.IOException
import net.minestom.server.extras.mojangAuth.MojangCrypt
import java.io.UnsupportedEncodingException
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.lang.RuntimeException
import net.minestom.server.extras.optifine.OptifineSupport
import net.minestom.server.world.biomes.BiomeManager
import net.minestom.server.utils.NamespaceID
import net.minestom.server.utils.binary.Writeable
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.extras.query.event.QueryEvent
import net.minestom.server.extras.query.response.FullQueryResponse
import net.minestom.server.extras.query.response.BasicQueryResponse
import java.util.Locale
import net.minestom.server.extras.query.response.QueryKey
import net.minestom.server.utils.binary.BinaryWriter
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.lang.IllegalArgumentException
import net.minestom.server.extras.query.event.BasicQueryEvent
import net.minestom.server.extras.query.event.FullQueryEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.utils.binary.BinaryReader
import java.net.InetAddress
import net.minestom.server.entity.PlayerSkin
import org.slf4j.LoggerFactory
import java.security.*
import javax.crypto.*

object MojangCrypt {
    private val LOGGER = LoggerFactory.getLogger(MojangCrypt::class.java)
    @JvmStatic
    fun generateKeyPair(): KeyPair? {
        return try {
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(1024)
            keyGen.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            exceptionManager.handleException(e)
            LOGGER.error("Key pair generation failed!")
            null
        }
    }

    fun digestData(data: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray? {
        return try {
            digestData("SHA-1", data.toByteArray(charset("ISO_8859_1")), secretKey.encoded, publicKey.encoded)
        } catch (e: UnsupportedEncodingException) {
            exceptionManager.handleException(e)
            null
        }
    }

    private fun digestData(algorithm: String, vararg data: ByteArray): ByteArray? {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            for (bytes in data) {
                digest.update(bytes)
            }
            digest.digest()
        } catch (e: NoSuchAlgorithmException) {
            exceptionManager.handleException(e)
            null
        }
    }

    @JvmStatic
    fun decryptByteToSecretKey(privateKey: PrivateKey, bytes: ByteArray?): SecretKey {
        return SecretKeySpec(decryptUsingKey(privateKey, bytes), "AES")
    }

    @JvmStatic
    fun decryptUsingKey(key: Key, bytes: ByteArray?): ByteArray? {
        return cipherData(2, key, bytes)
    }

    private fun cipherData(mode: Int, key: Key, data: ByteArray?): ByteArray? {
        try {
            return setupCipher(mode, key.algorithm, key)!!.doFinal(data)
        } catch (var4: IllegalBlockSizeException) {
            exceptionManager.handleException(var4)
        } catch (var4: BadPaddingException) {
            exceptionManager.handleException(var4)
        }
        LOGGER.error("Cipher data failed!")
        return null
    }

    private fun setupCipher(mode: Int, transformation: String, key: Key): Cipher? {
        try {
            val cipher4 = Cipher.getInstance(transformation)
            cipher4.init(mode, key)
            return cipher4
        } catch (var4: InvalidKeyException) {
            exceptionManager.handleException(var4)
        } catch (var4: NoSuchAlgorithmException) {
            exceptionManager.handleException(var4)
        } catch (var4: NoSuchPaddingException) {
            exceptionManager.handleException(var4)
        }
        LOGGER.error("Cipher creation failed!")
        return null
    }

    @JvmStatic
    fun getCipher(mode: Int, key: Key): Cipher {
        return try {
            val cipher3 = Cipher.getInstance("AES/CFB8/NoPadding")
            cipher3.init(mode, key, IvParameterSpec(key.encoded))
            cipher3
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }
    }
}