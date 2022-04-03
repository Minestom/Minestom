package net.minestom.server.extras.lan

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
import net.minestom.server.event.EventDispatcher
import net.minestom.server.timer.Task
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Utility class to manage opening the server to LAN. Note that this **doesn't** actually
 * open your server to LAN if it isn't already visible to anyone on your local network.
 * Instead it simply sends the packets needed to trick the Minecraft client into thinking
 * that this is a single-player world that has been opened to LANfor it to be displayed on
 * the bottom of the server list.
 *
 * @see [wiki.vg](https://wiki.vg/Server_List_Ping.Ping_via_LAN_.28Open_to_LAN_in_Singleplayer.29)
 */
object OpenToLAN {
    private val PING_ADDRESS = InetSocketAddress("224.0.2.60", 4445)
    private val LOGGER = LoggerFactory.getLogger(OpenToLAN::class.java)

    @Volatile
    private var eventCooldown: Cooldown? = null

    @Volatile
    private var socket: DatagramSocket? = null

    @Volatile
    private var packet: DatagramPacket? = null

    @Volatile
    private var task: Task? = null
    /**
     * Opens the server to LAN.
     *
     * @param config the configuration
     * @return `true` if it was opened successfully, `false` otherwise
     */
    /**
     * Opens the server to LAN with the default config.
     *
     * @return `true` if it was opened successfully, `false` otherwise
     */
    @JvmOverloads
    fun open(config: OpenToLANConfig = OpenToLANConfig()): Boolean {
        Objects.requireNonNull(config, "config")
        if (socket != null) return false
        try {
            socket = DatagramSocket(config.port)
        } catch (e: SocketException) {
            LOGGER.warn("Could not bind to the port!", e)
            return false
        }
        eventCooldown = Cooldown(config.delayBetweenEvent)
        task = schedulerManager.buildTask(Runnable { obj: OpenToLAN? -> ping() })
            .repeat(config.delayBetweenPings)
            .schedule()
        return true
    }

    /**
     * Closes the server to LAN.
     *
     * @return `true` if it was closed, `false` if it was already closed
     */
    fun close(): Boolean {
        if (socket == null) return false
        task!!.cancel()
        socket!!.close()
        task = null
        socket = null
        return true
    }

    /**
     * Checks if the server is currently opened to LAN.
     *
     * @return `true` if it is, `false` otherwise
     */
    val isOpen: Boolean
        get() = socket != null

    /**
     * Performs the ping.
     */
    private fun ping() {
        if (!server.isOpen) return
        if (packet == null || eventCooldown!!.isReady(System.currentTimeMillis())) {
            val event = ServerListPingEvent(ServerListPingType.OPEN_TO_LAN)
            EventDispatcher.call(event)
            val data = ServerListPingType.OPEN_TO_LAN.getPingResponse(event.responseData).toByteArray(
                StandardCharsets.UTF_8
            )
            packet = DatagramPacket(data, data.size, PING_ADDRESS)
            eventCooldown!!.refreshLastUpdate(System.currentTimeMillis())
        }
        try {
            socket!!.send(packet)
        } catch (e: IOException) {
            LOGGER.warn("Could not send Open to LAN packet!", e)
        }
    }
}