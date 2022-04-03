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
 * A basic query response containing a fixed set of responses.
 */
class BasicQueryResponse : Writeable {
    private var motd = "A Minestom Server"
    private var gametype = "SMP"
    private var map = "world"
    private var numPlayers: String
    private var maxPlayers: String

    /**
     * Creates a new basic query response with pre-filled default values.
     */
    init {
        numPlayers = connectionManager.onlinePlayers.size.toString()
        maxPlayers = (numPlayers.toInt() + 1).toString()
    }

    /**
     * Gets the MoTD.
     *
     * @return the motd
     */
    fun getMotd(): String {
        return motd
    }

    /**
     * Sets the MoTD.
     *
     * @param motd the motd
     */
    fun setMotd(motd: String) {
        this.motd = Objects.requireNonNull(motd, "motd")
    }

    /**
     * Gets the gametype.
     *
     * @return the gametype
     */
    fun getGametype(): String {
        return gametype
    }

    /**
     * Sets the gametype.
     *
     * @param gametype the gametype
     */
    fun setGametype(gametype: String) {
        this.gametype = Objects.requireNonNull(gametype, "gametype")
    }

    /**
     * Gets the map.
     *
     * @return the map
     */
    fun getMap(): String {
        return map
    }

    /**
     * Sets the map.
     *
     * @param map the map
     */
    fun setMap(map: String) {
        this.map = Objects.requireNonNull(map, "map")
    }

    /**
     * Gets the number of players.
     *
     * @return the number of players
     */
    fun getNumPlayers(): String {
        return numPlayers
    }

    /**
     * Sets the number of players.
     *
     * @param numPlayers the number of players
     */
    fun setNumPlayers(numPlayers: String) {
        this.numPlayers = Objects.requireNonNull(numPlayers, "numPlayers")
    }

    /**
     * Sets the number of players.
     * This method is just an overload for [.setNumPlayers].
     *
     * @param numPlayers the number of players
     */
    fun setNumPlayers(numPlayers: Int) {
        this.setNumPlayers(numPlayers.toString())
    }

    /**
     * Gets the max number of players.
     *
     * @return the max number of players
     */
    fun getMaxPlayers(): String {
        return maxPlayers
    }

    /**
     * Sets the max number of players.
     *
     * @param maxPlayers the max number of players
     */
    fun setMaxPlayers(maxPlayers: String) {
        this.maxPlayers = Objects.requireNonNull(maxPlayers, "maxPlayers")
    }

    /**
     * Sets the max number of players.
     * This method is just an overload for [.setMaxPlayers]
     *
     * @param maxPlayers the max number of players
     */
    fun setMaxPlayers(maxPlayers: Int) {
        this.setMaxPlayers(maxPlayers.toString())
    }

    override fun write(writer: BinaryWriter) {
        writer.writeNullTerminatedString(motd, Query.Companion.CHARSET)
        writer.writeNullTerminatedString(gametype, Query.Companion.CHARSET)
        writer.writeNullTerminatedString(map, Query.Companion.CHARSET)
        writer.writeNullTerminatedString(numPlayers, Query.Companion.CHARSET)
        writer.writeNullTerminatedString(maxPlayers, Query.Companion.CHARSET)
        writer.buffer.putShort(server.port.toShort()) // TODO little endian?
        writer.writeNullTerminatedString(Objects.requireNonNullElse(server.address, ""), Query.Companion.CHARSET)
    }
}