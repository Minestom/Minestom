package net.minestom.server;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.attribute.AttributeManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.armor.TrimManager;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.snapshot.EntitySnapshot;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.ServerSnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.terminal.MinestomTerminal;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.PropertyUtils;
import net.minestom.server.utils.collection.MappedCollection;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ServerProcessImpl implements ServerProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProcessImpl.class);
    private static final Boolean SHUTDOWN_ON_SIGNAL = PropertyUtils.getBoolean("minestom.shutdown-on-signal", true);

    private final ExceptionManager exception;
    private final ExtensionManager extension;
    private final ConnectionManager connection;
    private final PacketListenerManager packetListener;
    private final PacketProcessor packetProcessor;
    private final InstanceManager instance;
    private final BlockManager block;
    private final CommandManager command;
    private final RecipeManager recipe;
    private final TeamManager team;
    private final GlobalEventHandler eventHandler;
    private final SchedulerManager scheduler;
    private final BenchmarkManager benchmark;
    private final DimensionTypeManager dimension;
    private final BiomeManager biome;
    private final AttributeManager attribute;
    private final AdvancementManager advancement;
    private final BossBarManager bossBar;
    private final TagManager tag;
    private final TrimManager trim;
    private final Server server;
    private final Metrics metrics;

    private final ThreadDispatcher<Chunk> dispatcher;
    private final Ticker ticker;

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean();

    private static boolean bstatsEnabled = System.getProperty("minestom.bstats.enabled") == null;


    public ServerProcessImpl() throws IOException {
        this.exception = new ExceptionManager();
        this.extension = new ExtensionManager(this);
        this.connection = new ConnectionManager();
        this.packetListener = new PacketListenerManager();
        this.packetProcessor = new PacketProcessor(packetListener);
        this.instance = new InstanceManager();
        this.block = new BlockManager();
        this.command = new CommandManager();
        this.recipe = new RecipeManager();
        this.team = new TeamManager();
        this.eventHandler = new GlobalEventHandler();
        this.scheduler = new SchedulerManager();
        this.benchmark = new BenchmarkManager();
        this.dimension = new DimensionTypeManager();
        this.biome = new BiomeManager();
        this.attribute = new AttributeManager();
        this.advancement = new AdvancementManager();
        this.bossBar = new BossBarManager();
        this.tag = new TagManager();
        this.trim = new TrimManager();
        this.server = new Server(packetProcessor);

        this.dispatcher = ThreadDispatcher.singleThread();
        this.ticker = new TickerImpl();
        this.metrics = new Metrics();
    }

    @Override
    public @NotNull ConnectionManager connection() {
        return connection;
    }

    @Override
    public @NotNull InstanceManager instance() {
        return instance;
    }

    @Override
    public @NotNull BlockManager block() {
        return block;
    }

    @Override
    public @NotNull CommandManager command() {
        return command;
    }

    @Override
    public @NotNull RecipeManager recipe() {
        return recipe;
    }

    @Override
    public @NotNull TeamManager team() {
        return team;
    }

    @Override
    public @NotNull GlobalEventHandler eventHandler() {
        return eventHandler;
    }

    @Override
    public @NotNull SchedulerManager scheduler() {
        return scheduler;
    }

    @Override
    public @NotNull BenchmarkManager benchmark() {
        return benchmark;
    }

    @Override
    public @NotNull DimensionTypeManager dimension() {
        return dimension;
    }

    @Override
    public @NotNull BiomeManager biome() {
        return biome;
    }

    @Override
    public @NotNull AdvancementManager advancement() {
        return advancement;
    }

    @Override
    public @NotNull BossBarManager bossBar() {
        return bossBar;
    }

    @Override
    public @NotNull ExtensionManager extension() {
        return extension;
    }

    @Override
    public @NotNull TagManager tag() {
        return tag;
    }

    @Override
    public @NotNull TrimManager trim() {
        return trim;
    }

    @Override
    public @NotNull ExceptionManager exception() {
        return exception;
    }

    @Override
    public @NotNull PacketListenerManager packetListener() {
        return packetListener;
    }

    @Override
    public @NotNull PacketProcessor packetProcessor() {
        return packetProcessor;
    }

    @Override
    public @NotNull Server server() {
        return server;
    }

    @Override
    public @NotNull ThreadDispatcher<Chunk> dispatcher() {
        return dispatcher;
    }

    @Override
    public @NotNull Ticker ticker() {
        return ticker;
    }

    @Override
    public @NotNull AttributeManager attribute() {
        return attribute;
    }

    @Override
    public void start(@NotNull SocketAddress socketAddress) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already started");
        }

        extension.start();
        extension.gotoPreInit();

        LOGGER.info("Starting " + MinecraftServer.getBrandName() + " server.");

        extension.gotoInit();

        // Init server
        try {
            server.init(socketAddress);
        } catch (IOException e) {
            exception.handleException(e);
            throw new RuntimeException(e);
        }

        // Start server
        server.start();

        extension.gotoPostInit();

        LOGGER.info(MinecraftServer.getBrandName() + " server started successfully.");

        if (ServerFlag.ATTRIBUTES_ENABLED) {
            attribute.loadVanillaAttributes();
        }
        LOGGER.info("Register Attributes({})", attribute.unmodifiableCollection().size());

        if (ServerFlag.BIOMES_ENABLED) {
            biome.loadVanillaBiomes();
        }
        LOGGER.info("Register Biomes({})", biome.unmodifiableCollection().size());

        if (ServerFlag.TERMINAL_ENABLED) {
            MinestomTerminal.start();
        }
        if (bstatsEnabled) {
            this.metrics.start();
        }
        // Stop the server on SIGINT
        if (SHUTDOWN_ON_SIGNAL) Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @Override
    public void stop() {
        if (!stopped.compareAndSet(false, true))
            return;
        LOGGER.info("Stopping " + MinecraftServer.getBrandName() + " server.");
        LOGGER.info("Unloading all extensions.");
        extension.shutdown();
        scheduler.shutdown();
        connection.shutdown();
        server.stop();
        LOGGER.info("Shutting down all thread pools.");
        benchmark.disable();
        MinestomTerminal.stop();
        dispatcher.shutdown();
        this.metrics.shutdown();
        LOGGER.info(MinecraftServer.getBrandName() + " server stopped successfully.");
    }

    @Override
    public boolean isAlive() {
        return started.get() && !stopped.get();
    }

    @Override
    public @NotNull ServerSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        List<AtomicReference<InstanceSnapshot>> instanceRefs = new ArrayList<>();
        Int2ObjectOpenHashMap<AtomicReference<EntitySnapshot>> entityRefs = new Int2ObjectOpenHashMap<>();
        for (Instance instance : instance.getInstances()) {
            instanceRefs.add(updater.reference(instance));
            for (Entity entity : instance.getEntities()) {
                entityRefs.put(entity.getEntityId(), updater.reference(entity));
            }
        }
        return new SnapshotImpl.Server(MappedCollection.plainReferences(instanceRefs), entityRefs);
    }

    private final class TickerImpl implements Ticker {
        @Override
        public void tick(long nanoTime) {
            final long msTime = System.currentTimeMillis();

            scheduler().processTick();

            // Connection tick (let waiting clients in, send keep alives, handle configuration players packets)
            connection().tick(msTime);

            // Server tick (chunks/entities)
            serverTick(msTime);

            scheduler().processTickEnd();

            // Flush all waiting packets
            PacketUtils.flush();

            // Server connection tick
            server().tick();

            // Monitoring
            {
                final double acquisitionTimeMs = Acquirable.resetAcquiringTime() / 1e6D;
                final double tickTimeMs = (System.nanoTime() - nanoTime) / 1e6D;
                final TickMonitor tickMonitor = new TickMonitor(tickTimeMs, acquisitionTimeMs);
                EventDispatcher.call(new ServerTickMonitorEvent(tickMonitor));
            }
        }

        private void serverTick(long tickStart) {
            // Tick all instances
            for (Instance instance : instance().getInstances()) {
                try {
                    instance.tick(tickStart);
                } catch (Exception e) {
                    exception().handleException(e);
                }
            }
            // Tick all chunks (and entities inside)
            dispatcher().updateAndAwait(tickStart);

            // Clear removed entities & update threads
            final long tickTime = System.currentTimeMillis() - tickStart;
            dispatcher().refreshThreads(tickTime);
        }
    }
}
