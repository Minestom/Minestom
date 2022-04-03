package net.minestom.server

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
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
import net.minestom.server.event.EventDispatcher
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
import org.jetbrains.annotations.UnknownNullability
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.SocketAddress
import java.util.ArrayList

internal class ServerProcessImpl : ServerProcess {
    private val exception: ExceptionManager
    private val extension: ExtensionManager
    private val connection: ConnectionManager
    private val packetProcessor: PacketProcessor
    private val packetListener: PacketListenerManager
    private val instance: InstanceManager
    private val block: BlockManager
    private val command: CommandManager
    private val recipe: RecipeManager
    private val team: TeamManager
    private val eventHandler: GlobalEventHandler
    private val scheduler: SchedulerManager
    private val benchmark: BenchmarkManager
    private val dimension: DimensionTypeManager
    private val biome: BiomeManager
    private val advancement: AdvancementManager
    private val bossBar: BossBarManager
    private val tag: TagManager
    private val server: Server
    private val dispatcher: ThreadDispatcher<Chunk>
    private val ticker: ServerProcess.Ticker
    private val started = AtomicBoolean()
    private val stopped = AtomicBoolean()

    init {
        exception = ExceptionManager()
        this.extension = ExtensionManager(this)
        connection = ConnectionManager()
        packetProcessor = PacketProcessor()
        packetListener = PacketListenerManager(this)
        instance = InstanceManager()
        block = BlockManager()
        command = CommandManager()
        recipe = RecipeManager()
        team = TeamManager()
        eventHandler = GlobalEventHandler()
        scheduler = SchedulerManager()
        benchmark = BenchmarkManager()
        dimension = DimensionTypeManager()
        biome = BiomeManager()
        advancement = AdvancementManager()
        bossBar = BossBarManager()
        tag = TagManager()
        server = Server(packetProcessor)
        dispatcher = ThreadDispatcher.singleThread()
        ticker = TickerImpl()
    }

    override fun connection(): ConnectionManager {
        return connection
    }

    override fun instance(): InstanceManager {
        return instance
    }

    override fun block(): BlockManager {
        return block
    }

    override fun command(): CommandManager {
        return command
    }

    override fun recipe(): RecipeManager {
        return recipe
    }

    override fun team(): TeamManager {
        return team
    }

    override fun eventHandler(): GlobalEventHandler {
        return eventHandler
    }

    override fun scheduler(): SchedulerManager {
        return scheduler
    }

    override fun benchmark(): BenchmarkManager {
        return benchmark
    }

    override fun dimension(): DimensionTypeManager {
        return dimension
    }

    override fun biome(): BiomeManager {
        return biome
    }

    override fun advancement(): AdvancementManager {
        return advancement
    }

    override fun bossBar(): BossBarManager {
        return bossBar
    }

    override fun extension(): ExtensionManager {
        return extension
    }

    override fun tag(): TagManager {
        return tag
    }

    override fun exception(): ExceptionManager {
        return exception
    }

    override fun packetListener(): PacketListenerManager {
        return packetListener
    }

    override fun packetProcessor(): PacketProcessor {
        return packetProcessor
    }

    override fun server(): Server {
        return server
    }

    override fun dispatcher(): ThreadDispatcher<Chunk> {
        return dispatcher
    }

    override fun ticker(): ServerProcess.Ticker {
        return ticker
    }

    override fun start(socketAddress: SocketAddress) {
        check(started.compareAndSet(false, true)) { "Server already started" }
        extension.start()
        extension.gotoPreInit()
        LOGGER.info("Starting Minestom server.")
        extension.gotoInit()

        // Init server
        try {
            server.init(socketAddress)
        } catch (e: IOException) {
            exception.handleException(e)
            throw RuntimeException(e)
        }

        // Start server
        server.start()
        extension.gotoPostInit()
        LOGGER.info("Minestom server started successfully.")
        if (MinecraftServer.Companion.isTerminalEnabled()) {
            MinestomTerminal.start()
        }
        // Stop the server on SIGINT
        Runtime.getRuntime().addShutdownHook(Thread { stop() })
    }

    override fun stop() {
        if (!stopped.compareAndSet(false, true)) return
        LOGGER.info("Stopping Minestom server.")
        LOGGER.info("Unloading all extensions.")
        extension.shutdown()
        scheduler.shutdown()
        connection.shutdown()
        server.stop()
        LOGGER.info("Shutting down all thread pools.")
        benchmark.disable()
        MinestomTerminal.stop()
        dispatcher.shutdown()
        LOGGER.info("Minestom server stopped successfully.")
    }

    override val isAlive: Boolean
        get() = started.get() && !stopped.get()

    override fun updateSnapshot(updater: SnapshotUpdater): ServerSnapshot {
        val instanceRefs: MutableList<AtomicReference<InstanceSnapshot>> = ArrayList()
        val entityRefs = Int2ObjectOpenHashMap<AtomicReference<EntitySnapshot>>()
        for (instance in instance.instances) {
            instanceRefs.add(updater.reference(instance))
            for (entity in instance.entities) {
                entityRefs[entity.entityId] = updater.reference(entity)
            }
        }
        return SnapshotImpl(
            plainReferences<AtomicReference<InstanceSnapshot>, InstanceSnapshot>(instanceRefs),
            entityRefs
        )
    }

    internal inner class SnapshotImpl : ServerSnapshot {
        override fun entities(): Collection<EntitySnapshot> {
            return plainReferences<AtomicReference<EntitySnapshot>, EntitySnapshot>(entityRefs.values)
        }

        override fun entity(id: Int): @UnknownNullability EntitySnapshot? {
            val ref: AtomicReference<EntitySnapshot> = entityRefs.get(id)
            return if (ref != null) ref.plain else null
        }
    }

    private inner class TickerImpl : ServerProcess.Ticker {
        override fun tick(nanoTime: Long) {
            val msTime = System.currentTimeMillis()
            scheduler().processTick()

            // Waiting players update (newly connected clients waiting to get into the server)
            connection().updateWaitingPlayers()

            // Keep Alive Handling
            connection().handleKeepAlive(msTime)

            // Server tick (chunks/entities)
            serverTick(msTime)

            // Flush all waiting packets
            PacketUtils.flush()

            // Monitoring
            run {
                val acquisitionTimeMs = Acquirable.resetAcquiringTime() / 1e6
                val tickTimeMs = (System.nanoTime() - nanoTime) / 1e6
                val tickMonitor = TickMonitor(tickTimeMs, acquisitionTimeMs)
                EventDispatcher.call(ServerTickMonitorEvent(tickMonitor))
            }
        }

        private fun serverTick(tickStart: Long) {
            // Tick all instances
            for (instance in instance().instances) {
                try {
                    instance.tick(tickStart)
                } catch (e: Exception) {
                    exception().handleException(e)
                }
            }
            // Tick all chunks (and entities inside)
            dispatcher().updateAndAwait(tickStart)

            // Clear removed entities & update threads
            val tickTime = System.currentTimeMillis() - tickStart
            dispatcher().refreshThreads(tickTime)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ServerProcessImpl::class.java)
    }
}