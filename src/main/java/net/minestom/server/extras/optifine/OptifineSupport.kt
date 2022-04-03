package net.minestom.server.extras.optifine

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
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import net.minestom.server.extras.mojangAuth.MojangCrypt
import java.security.PublicKey
import javax.crypto.SecretKey
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.PrivateKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.IllegalBlockSizeException
import javax.crypto.BadPaddingException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import java.security.GeneralSecurityException
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

/**
 * Hacky class for Optifine because of an issue making the client crash if biomes 'swamp' and 'swamp_hills'
 * are not registered.
 *
 *
 * Can be removed anytime, hope that it will be fixed.
 */
object OptifineSupport {
    @Volatile
    var isEnabled = false
        private set

    /**
     * Enables optifine support by registering the required biomes.
     *
     * @throws IllegalStateException if optifine support is already enabled
     */
    fun enable() {
        stateCondition(isEnabled, "Optifine support is already enabled!")
        isEnabled = true
        val biomeManager = biomeManager
        biomeManager.addBiome(builder().name(NamespaceID.from("minecraft:swamp")).build())
        biomeManager.addBiome(builder().name(NamespaceID.from("minecraft:swamp_hills")).build())
    }
}