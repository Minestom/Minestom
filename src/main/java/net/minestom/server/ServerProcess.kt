package net.minestom.server

import net.minestom.server.utils.collection.MappedCollection.Companion.plainReferences
import net.minestom.server.MinecraftServer
import net.minestom.server.thread.TickSchedulerThread
import java.net.InetSocketAddress
import net.minestom.server.ServerProcess
import net.minestom.server.world.Difficulty
import net.minestom.server.ServerProcessImpl
import java.io.IOException
import java.lang.RuntimeException
import net.minestom.server.utils.PacketUtils
import net.minestom.server.network.packet.server.play.PluginMessagePacket
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.listener.manager.PacketListenerManager
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.block.BlockManager
import net.minestom.server.command.CommandManager
import net.minestom.server.recipe.RecipeManager
import net.minestom.server.scoreboard.TeamManager
import net.minestom.server.timer.SchedulerManager
import net.minestom.server.monitoring.BenchmarkManager
import net.minestom.server.exception.ExceptionManager
import net.minestom.server.adventure.bossbar.BossBarManager
import net.minestom.server.network.PacketProcessor
import net.minestom.server.world.DimensionTypeManager
import net.minestom.server.world.biomes.BiomeManager
import net.minestom.server.advancements.AdvancementManager
import net.minestom.server.extensions.ExtensionManager
import net.minestom.server.gamedata.tags.TagManager
import net.minestom.server.snapshot.Snapshotable
import net.minestom.server.thread.ThreadDispatcher
import java.util.concurrent.atomic.AtomicBoolean
import net.minestom.server.ServerProcessImpl.TickerImpl
import java.lang.IllegalStateException
import net.minestom.server.terminal.MinestomTerminal
import java.lang.Runnable
import net.minestom.server.snapshot.SnapshotUpdater
import net.minestom.server.snapshot.ServerSnapshot
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.snapshot.InstanceSnapshot
import net.minestom.server.snapshot.EntitySnapshot
import net.minestom.server.utils.collection.MappedCollection
import net.minestom.server.thread.Acquirable
import net.minestom.server.monitoring.TickMonitor
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.instance.Chunk
import net.minestom.server.network.ConnectionManager
import net.minestom.server.network.socket.Server
import org.jetbrains.annotations.ApiStatus
import java.net.SocketAddress

@ApiStatus.Experimental
@ApiStatus.NonExtendable
interface ServerProcess : Snapshotable {
    /**
     * Handles incoming connections/players.
     */
    fun connection(): ConnectionManager

    /**
     * Handles registered instances.
     */
    fun instance(): InstanceManager

    /**
     * Handles [block handlers][net.minestom.server.instance.block.BlockHandler]
     * and [placement rules][BlockPlacementRule].
     */
    fun block(): BlockManager

    /**
     * Handles registered commands.
     */
    fun command(): CommandManager

    /**
     * Handles registered recipes shown to clients.
     */
    fun recipe(): RecipeManager

    /**
     * Handles registered teams.
     */
    fun team(): TeamManager

    /**
     * Gets the global event handler.
     *
     *
     * Used to register event callback at a global scale.
     */
    fun eventHandler(): GlobalEventHandler

    /**
     * Main scheduler ticked at the server rate.
     */
    fun scheduler(): SchedulerManager
    fun benchmark(): BenchmarkManager

    /**
     * Handles registered dimensions.
     */
    fun dimension(): DimensionTypeManager

    /**
     * Handles registered biomes.
     */
    fun biome(): BiomeManager

    /**
     * Handles registered advancements.
     */
    fun advancement(): AdvancementManager

    /**
     * Handles registered boss bars.
     */
    fun bossBar(): BossBarManager

    /**
     * Loads and handle extensions.
     */
    fun extension(): ExtensionManager

    /**
     * Handles registry tags.
     */
    fun tag(): TagManager

    /**
     * Handles all thrown exceptions from the server.
     */
    fun exception(): ExceptionManager

    /**
     * Handles incoming packets.
     */
    fun packetListener(): PacketListenerManager

    /**
     * Gets the object handling the client packets processing.
     *
     *
     * Can be used if you want to convert a buffer to a client packet object.
     */
    fun packetProcessor(): PacketProcessor

    /**
     * Exposed socket server.
     */
    fun server(): Server

    /**
     * Dispatcher for tickable game objects.
     */
    fun dispatcher(): ThreadDispatcher<Chunk>

    /**
     * Handles the server ticks.
     */
    fun ticker(): Ticker
    fun start(socketAddress: SocketAddress)
    fun stop()
    val isAlive: Boolean

    @ApiStatus.NonExtendable
    interface Ticker {
        fun tick(nanoTime: Long)
    }
}