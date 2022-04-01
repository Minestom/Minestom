package net.minestom.server.extras.query

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
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
import net.minestom.server.utils.time.Cooldown
import net.minestom.server.extras.lan.OpenToLANConfig
import net.minestom.server.extras.lan.OpenToLAN
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
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.event.EventDispatcher
import net.minestom.server.timer.Task
import net.minestom.server.utils.time.TimeUnit
import org.slf4j.LoggerFactory
import java.net.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Utility class to manage responses to the GameSpy4 Query Protocol.
 *
 * @see [wiki.vg](https://wiki.vg/Query)
 */
class Query private constructor() {
    /**
     * Stops the query system.
     *
     * @return `true` if the query system was stopped, `false` if it was not running
     */
    fun stop(): Boolean {
        return if (!isStarted) {
            false
        } else {
            isStarted = false
            thread = null
            socket!!.close()
            socket = null
            task!!.cancel()
            CHALLENGE_TOKENS.clear()
            true
        }
    }

    companion object {
        val CHARSET = StandardCharsets.ISO_8859_1
        private val LOGGER = LoggerFactory.getLogger(Query::class.java)
        private val RANDOM = Random()
        private val CHALLENGE_TOKENS = Int2ObjectMaps.synchronize(Int2ObjectOpenHashMap<SocketAddress>())

        /**
         * Checks if the query system has been started.
         *
         * @return `true` if it has been started, `false` otherwise
         */
        @Volatile
        var isStarted = false
            get() = Companion.field
            private set

        @Volatile
        private var socket: DatagramSocket? = null

        @Volatile
        private var thread: Thread? = null

        @Volatile
        private var task: Task? = null

        /**
         * Starts the query system, responding to queries on a random port, logging if it could not be started.
         *
         * @return the port
         * @throws IllegalArgumentException if the system was already running
         */
        fun start(): Int {
            return if (socket != null) {
                throw IllegalArgumentException("System is already running")
            } else {
                val port = 0
                start(port)
                port
            }
        }

        /**
         * Starts the query system, responding to queries on a given port, logging if it could not be started.
         *
         * @param port the port
         * @return `true` if the query system started successfully, `false` otherwise
         */
        fun start(port: Int): Boolean {
            return if (socket != null) {
                false
            } else {
                try {
                    socket = DatagramSocket(port)
                } catch (e: SocketException) {
                    LOGGER.warn("Could not open the query port!", e)
                    return false
                }
                thread = Thread { run() }
                thread!!.start()
                isStarted = true
                task = schedulerManager
                    .buildTask { CHALLENGE_TOKENS.clear() }
                    .repeat(30, TimeUnit.SECOND)
                    .schedule()
                true
            }
        }

        private fun run() {
            val buffer = ByteArray(16)
            while (isStarted) {
                val packet = DatagramPacket(buffer, buffer.size)

                // try and receive the packet
                try {
                    socket!!.receive(packet)
                } catch (e: IOException) {
                    if (!isStarted) {
                        LOGGER.error("An error occurred whilst receiving a query packet.", e)
                        continue
                    } else {
                        return
                    }
                }

                // get the contents
                val data = ByteBuffer.wrap(packet.data)

                // check the magic field
                if (data.short and 0xFFFF != 0xFEFD) {
                    continue
                }

                // now check the query type
                val type = data.get()
                if (type.toInt() == 9) { // handshake
                    val sessionID = data.int
                    val challengeToken = RANDOM.nextInt()
                    CHALLENGE_TOKENS[challengeToken] = packet.socketAddress

                    // send the response
                    val response = BinaryWriter(32)
                    response.writeByte(9.toByte())
                    response.writeInt(sessionID)
                    response.writeNullTerminatedString(challengeToken.toString(), CHARSET)
                    try {
                        val responseData = response.toByteArray()
                        socket!!.send(DatagramPacket(responseData, responseData.size, packet.socketAddress))
                    } catch (e: IOException) {
                        if (!isStarted) {
                            LOGGER.error("An error occurred whilst sending a query handshake packet.", e)
                        } else {
                            return
                        }
                    }
                } else if (type.toInt() == 0) { // stat
                    val sessionID = data.int
                    val challengeToken = data.int
                    val sender = packet.socketAddress
                    if (CHALLENGE_TOKENS.containsKey(challengeToken) && CHALLENGE_TOKENS[challengeToken] == sender) {
                        val remaining = data.remaining()
                        if (remaining == 0) { // basic
                            val event = BasicQueryEvent(sender, sessionID)
                            EventDispatcher.callCancellable(event) {
                                sendResponse(
                                    event.queryResponse,
                                    sessionID,
                                    sender
                                )
                            }
                        } else if (remaining == 5) { // full
                            val event = FullQueryEvent(sender, sessionID)
                            EventDispatcher.callCancellable(event) {
                                sendResponse(
                                    event.queryResponse,
                                    sessionID,
                                    sender
                                )
                            }
                        }
                    }
                }
            }
        }

        private fun sendResponse(queryResponse: Writeable, sessionID: Int, sender: SocketAddress) {
            // header
            val response = BinaryWriter()
            response.writeByte(0.toByte())
            response.writeInt(sessionID)

            // payload
            queryResponse.write(response)

            // send!
            val responseData = response.toByteArray()
            try {
                socket!!.send(DatagramPacket(responseData, responseData.size, sender))
            } catch (e: IOException) {
                if (!isStarted) {
                    LOGGER.error("An error occurred whilst sending a query handshake packet.", e)
                }
            }
        }
    }
}