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
import net.minestom.server.network.ConnectionManager
import net.minestom.server.network.socket.Server
import net.minestom.server.utils.MathUtils
import net.minestom.server.utils.validate.Check
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.UnknownNullability
import org.slf4j.LoggerFactory
import java.net.SocketAddress

/**
 * The main server class used to start the server and retrieve all the managers.
 *
 *
 * The server needs to be initialized with [.init] and started with [.start].
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
class MinecraftServer {
    /**
     * Starts the server.
     *
     *
     * It should be called after [.init] and probably your own initialization code.
     *
     * @param address the server address
     * @throws IllegalStateException if called before [.init] or if the server is already running
     */
    fun start(address: SocketAddress) {
        serverProcess!!.start(address)
        TickSchedulerThread(serverProcess).start()
    }

    fun start(address: String, port: Int) {
        start(InetSocketAddress(address, port))
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(MinecraftServer::class.java)
        const val VERSION_NAME = "1.18.2"
        const val PROTOCOL_VERSION = 758

        // Threads
        const val THREAD_NAME_BENCHMARK = "Ms-Benchmark"
        const val THREAD_NAME_TICK_SCHEDULER = "Ms-TickScheduler"
        const val THREAD_NAME_TICK = "Ms-Tick"

        // Config
        // Can be modified at performance cost when increased
        val TICK_PER_SECOND = Integer.getInteger("minestom.tps", 20)
        val TICK_MS = 1000 / TICK_PER_SECOND
        /**
         * Gets the maximum number of packets a client can send over 1 second.
         *
         * @return the packet count limit over 1 second, 0 if not enabled
         */
        /**
         * Changes the number of packet a client can send over 1 second without being disconnected.
         *
         * @param rateLimit the number of packet, 0 to disable
         */
        // Network monitoring
        var rateLimit = 300
        /**
         * Gets the maximum packet size (in bytes) that a client can send without getting disconnected.
         *
         * @return the maximum packet size
         */
        /**
         * Changes the maximum packet size (in bytes) that a client can send without getting disconnected.
         *
         * @param maxPacketSize the new max packet size
         */
        var maxPacketSize = 30000

        // In-Game Manager
        @Volatile
        private var serverProcess: ServerProcess? = null
        /**
         * Gets the chunk view distance of the server.
         *
         * @return the chunk view distance
         */
        /**
         * Changes the chunk view distance of the server.
         *
         * @param chunkViewDistance the new chunk view distance
         * @throws IllegalArgumentException if `chunkViewDistance` is not between 2 and 32
         */
        @set:Deprecated("should instead be defined with a java property")
        var chunkViewDistance = Integer.getInteger("minestom.chunk-view-distance", 8)
            set(chunkViewDistance) {
                Check.stateCondition(
                    serverProcess!!.isAlive,
                    "You cannot change the chunk view distance after the server has been started."
                )
                Check.argCondition(
                    !MathUtils.isBetween(chunkViewDistance, 2, 32),
                    "The chunk view distance must be between 2 and 32"
                )
                field = chunkViewDistance
            }
        /**
         * Gets the entity view distance of the server.
         *
         * @return the entity view distance
         */
        /**
         * Changes the entity view distance of the server.
         *
         * @param entityViewDistance the new entity view distance
         * @throws IllegalArgumentException if `entityViewDistance` is not between 0 and 32
         */
        @set:Deprecated("should instead be defined with a java property")
        var entityViewDistance = Integer.getInteger("minestom.entity-view-distance", 5)
            set(entityViewDistance) {
                Check.stateCondition(
                    serverProcess!!.isAlive,
                    "You cannot change the entity view distance after the server has been started."
                )
                Check.argCondition(
                    !MathUtils.isBetween(entityViewDistance, 0, 32),
                    "The entity view distance must be between 0 and 32"
                )
                field = entityViewDistance
            }
        /**
         * Gets the compression threshold of the server.
         *
         * @return the compression threshold, 0 means that compression is disabled
         */
        /**
         * Changes the compression threshold of the server.
         *
         *
         * WARNING: this need to be called before [.start].
         *
         * @param compressionThreshold the new compression threshold, 0 to disable compression
         * @throws IllegalStateException if this is called after the server started
         */
        var compressionThreshold = 256
            set(compressionThreshold) {
                Check.stateCondition(
                    serverProcess!!.isAlive,
                    "The compression threshold cannot be changed after the server has been started."
                )
                field = compressionThreshold
            }
        /**
         * Gets if the built in Minestom terminal is enabled.
         *
         * @return true if the terminal is enabled
         */
        /**
         * Enabled/disables the built in Minestom terminal.
         *
         * @param enabled true to enable, false to disable
         */
        var isTerminalEnabled = System.getProperty("minestom.terminal.disabled") == null
            set(enabled) {
                Check.stateCondition(
                    serverProcess!!.isAlive,
                    "Terminal settings may not be changed after starting the server."
                )
                field = enabled
            }
        /**
         * Gets the current server brand name.
         *
         * @return the server brand name
         */
        /**
         * Changes the server brand name and send the change to all connected players.
         *
         * @param brandName the server brand name
         * @throws NullPointerException if `brandName` is null
         */
        var brandName = "Minestom"
            set(brandName) {
                field = brandName
                PacketUtils.broadcastPacket(PluginMessagePacket.getBrandPacket())
            }
        /**
         * Gets the server difficulty showed in game option.
         *
         * @return the server difficulty
         */
        /**
         * Changes the server difficulty and send the appropriate packet to all connected clients.
         *
         * @param difficulty the new server difficulty
         */
        var difficulty = Difficulty.NORMAL
            set(difficulty) {
                field = difficulty
                PacketUtils.broadcastPacket(ServerDifficultyPacket(difficulty, true))
            }

        fun init(): MinecraftServer {
            updateProcess()
            return MinecraftServer()
        }

        @ApiStatus.Internal
        fun updateProcess(): ServerProcess {
            val process: ServerProcess
            try {
                process = ServerProcessImpl()
                serverProcess = process
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return process
        }

        @ApiStatus.Experimental
        fun process(): @UnknownNullability ServerProcess? {
            return serverProcess
        }

        val globalEventHandler: GlobalEventHandler
            get() = serverProcess!!.eventHandler()
        val packetListenerManager: PacketListenerManager
            get() = serverProcess!!.packetListener()
        val instanceManager: InstanceManager
            get() = serverProcess!!.instance()
        val blockManager: BlockManager
            get() = serverProcess!!.block()
        val commandManager: CommandManager
            get() = serverProcess!!.command()
        val recipeManager: RecipeManager
            get() = serverProcess!!.recipe()
        val teamManager: TeamManager
            get() = serverProcess!!.team()
        val schedulerManager: SchedulerManager
            get() = serverProcess!!.scheduler()

        /**
         * Gets the manager handling server monitoring.
         *
         * @return the benchmark manager
         */
        val benchmarkManager: BenchmarkManager
            get() = serverProcess!!.benchmark()
        val exceptionManager: ExceptionManager
            get() = serverProcess!!.exception()
        val connectionManager: ConnectionManager
            get() = serverProcess!!.connection()
        val bossBarManager: BossBarManager
            get() = serverProcess!!.bossBar()
        val packetProcessor: PacketProcessor
            get() = serverProcess!!.packetProcessor()
        val isStarted: Boolean
            get() = serverProcess!!.isAlive
        val isStopping: Boolean
            get() = !isStarted
        val dimensionTypeManager: DimensionTypeManager
            get() = serverProcess!!.dimension()
        val biomeManager: BiomeManager
            get() = serverProcess!!.biome()
        val advancementManager: AdvancementManager
            get() = serverProcess!!.advancement()
        val extensionManager: ExtensionManager
            get() = serverProcess!!.extension()
        val tagManager: TagManager
            get() = serverProcess!!.tag()
        val server: Server
            get() = serverProcess!!.server()

        /**
         * Stops this server properly (saves if needed, kicking players, etc.)
         */
        fun stopCleanly() {
            serverProcess!!.stop()
        }
    }
}