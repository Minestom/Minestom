package net.minestom.server.extras.query.response

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
import net.minestom.server.entity.Player
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.lang.IllegalArgumentException
import net.minestom.server.extras.query.event.BasicQueryEvent
import net.minestom.server.extras.query.event.FullQueryEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.utils.binary.BinaryReader
import java.net.InetAddress
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.extras.query.Query
import java.util.*

/**
 * A full query response containing a dynamic set of responses.
 */
class FullQueryResponse : Writeable {
    private var kv: MutableMap<String, String>
    private var players: MutableList<String>

    /**
     * Creates a new full query response with default values set.
     */
    init {
        kv = HashMap()

        // populate defaults
        for (key in QueryKey.Companion.VALUES) {
            kv[key.key] = key.value
        }
        players = connectionManager.onlinePlayers
            .stream()
            .map { player: Player -> PLAIN.serialize(player.getName()) }
            .toList()
    }

    /**
     * Puts a key-value mapping into the response.
     *
     * @param key   the key
     * @param value the value
     */
    fun put(key: QueryKey, value: String) {
        this.put(key.key, value)
    }

    /**
     * Puts a key-value mapping into the response.
     *
     * @param key   the key
     * @param value the value
     */
    fun put(key: String, value: String) {
        kv[key] = value
    }

    /**
     * Gets the map containing the key-value mappings.
     *
     * @return the map
     */
    val keyValuesMap: Map<String, String>
        get() = kv

    /**
     * Sets the map containing the key-value mappings.
     *
     * @param map the map
     */
    fun setKeyValuesMap(map: MutableMap<String, String>) {
        kv = Objects.requireNonNull(map, "map")
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     */
    fun addPlayers(vararg players: String) {
        Collections.addAll(this.players, *players)
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     */
    fun addPlayers(players: Collection<String>) {
        this.players.addAll(players)
    }

    /**
     * Gets the list of players.
     *
     * @return the list
     */
    fun getPlayers(): List<String> {
        return players
    }

    /**
     * Sets the list of players.
     *
     * @param players the players
     */
    fun setPlayers(players: MutableList<String>) {
        this.players = Objects.requireNonNull(players, "players")
    }

    override fun write(writer: BinaryWriter) {
        writer.writeBytes(PADDING_11)

        // key-values
        for ((key, value) in kv) {
            writer.writeNullTerminatedString(key, Query.Companion.CHARSET)
            writer.writeNullTerminatedString(value, Query.Companion.CHARSET)
        }
        writer.writeNullTerminatedString("", Query.Companion.CHARSET)
        writer.writeBytes(PADDING_10)

        // players
        for (player in players) {
            writer.writeNullTerminatedString(player, Query.Companion.CHARSET)
        }
        writer.writeNullTerminatedString("", Query.Companion.CHARSET)
    }

    companion object {
        private val PLAIN = PlainComponentSerializer.plain()
        private val PADDING_10 = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        private val PADDING_11 = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        /**
         * Generates the default plugins value. That being the server name and version followed
         * by the name and version for each extension.
         *
         * @return the string result
         */
        fun generatePluginsValue(): String {
            val builder = StringBuilder(brandName)
                .append(' ')
                .append(MinecraftServer.VERSION_NAME)
            if (!extensionManager.extensions.isEmpty()) {
                for (extension in extensionManager.extensions) {
                    builder.append(extension.origin.name)
                        .append(' ')
                        .append(extension.origin.version)
                        .append("; ")
                }
                builder.delete(builder.length - 2, builder.length)
            }
            return builder.toString()
        }
    }
}