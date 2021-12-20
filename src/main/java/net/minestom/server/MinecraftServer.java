package net.minestom.server;

import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.network.socket.Server;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.terminal.MinestomTerminal;
import net.minestom.server.thread.MinestomThreadPool;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * The main server class used to start the server and retrieve all the managers.
 * <p>
 * The server needs to be initialized with {@link #init()} and started with {@link #start(String, int)}.
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
public final class MinecraftServer {

    public final static Logger LOGGER = LoggerFactory.getLogger(MinecraftServer.class);

    public static final String VERSION_NAME = "1.17.1";
    public static final int PROTOCOL_VERSION = 756;

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_TICK_SCHEDULER = "Ms-TickScheduler";
    public static final String THREAD_NAME_TICK = "Ms-Tick";

    public static final String THREAD_NAME_BLOCK_BATCH = "Ms-BlockBatchPool";
    public static final int THREAD_COUNT_BLOCK_BATCH = getThreadCount("minestom.block-thread-count",
            Runtime.getRuntime().availableProcessors() / 2);

    public static final String THREAD_NAME_SCHEDULER = "Ms-SchedulerPool";
    public static final int THREAD_COUNT_SCHEDULER = getThreadCount("minestom.scheduler-thread-count",
            Runtime.getRuntime().availableProcessors() / 2);

    public static final String THREAD_NAME_PARALLEL_CHUNK_SAVING = "Ms-ParallelChunkSaving";
    public static final int THREAD_COUNT_PARALLEL_CHUNK_SAVING = getThreadCount("minestom.save-thread-count", 2);

    // Config
    // Can be modified at performance cost when increased
    public static final int TICK_PER_SECOND = Integer.getInteger("minestom.tps", 20);
    public static final int TICK_MS = 1000 / TICK_PER_SECOND;

    // Network monitoring
    private static int rateLimit = 300;
    private static int maxPacketSize = 30_000;
    // Network
    private static PacketListenerManager packetListenerManager;
    private static PacketProcessor packetProcessor;
    private static Server server;

    private static ExceptionManager exceptionManager;

    // In-Game Manager
    private static ConnectionManager connectionManager;
    private static InstanceManager instanceManager;
    private static BlockManager blockManager;
    private static CommandManager commandManager;
    private static RecipeManager recipeManager;
    private static StorageManager storageManager;
    private static DataManager dataManager;
    private static TeamManager teamManager;
    private static SchedulerManager schedulerManager;
    private static BenchmarkManager benchmarkManager;
    private static DimensionTypeManager dimensionTypeManager;
    private static BiomeManager biomeManager;
    private static AdvancementManager advancementManager;
    private static BossBarManager bossBarManager;

    private static ExtensionManager extensionManager;

    private static final GlobalEventHandler GLOBAL_EVENT_HANDLER = new GlobalEventHandler();

    private static UpdateManager updateManager;
    private static MinecraftServer minecraftServer;

    // Data
    private static boolean initialized;
    private static boolean started;
    private static volatile boolean stopping;

    private static int chunkViewDistance = Integer.getInteger("minestom.chunk-view-distance", 8);
    private static int entityViewDistance = Integer.getInteger("minestom.entity-view-distance", 5);
    private static int compressionThreshold = 256;
    private static boolean terminalEnabled = System.getProperty("minestom.terminal.disabled") == null;
    private static ResponseDataConsumer responseDataConsumer;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;
    private static TagManager tagManager;

    public static MinecraftServer init() {
        if (minecraftServer != null) // don't init twice
            return minecraftServer;

        // Initialize the ExceptionManager at first
        exceptionManager = new ExceptionManager();

        extensionManager = new ExtensionManager();

        // warmup/force-init registries
        // without this line, registry types that are not loaded explicitly will have an internal empty registry in Registries
        // That can happen with PotionType for instance, if no code tries to access a PotionType field
        // TODO: automate (probably with code generation)
        Fluid.values();

        connectionManager = new ConnectionManager();
        // Networking
        packetProcessor = new PacketProcessor();
        packetListenerManager = new PacketListenerManager();

        instanceManager = new InstanceManager();
        blockManager = new BlockManager();
        commandManager = new CommandManager();
        recipeManager = new RecipeManager();
        storageManager = new StorageManager();
        dataManager = new DataManager();
        teamManager = new TeamManager();
        schedulerManager = new SchedulerManager();
        benchmarkManager = new BenchmarkManager();
        dimensionTypeManager = new DimensionTypeManager();
        biomeManager = new BiomeManager();
        advancementManager = new AdvancementManager();
        bossBarManager = new BossBarManager();

        updateManager = new UpdateManager();

        tagManager = new TagManager();

        try {
            server = new Server(packetProcessor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialized = true;

        minecraftServer = new MinecraftServer();

        return minecraftServer;
    }

    /**
     * Gets the current server brand name.
     *
     * @return the server brand name
     */
    @NotNull
    public static String getBrandName() {
        return brandName;
    }

    /**
     * Changes the server brand name and send the change to all connected players.
     *
     * @param brandName the server brand name
     * @throws NullPointerException if {@code brandName} is null
     */
    public static void setBrandName(@NotNull String brandName) {
        MinecraftServer.brandName = brandName;

        PacketUtils.sendGroupedPacket(connectionManager.getOnlinePlayers(), PluginMessagePacket.getBrandPacket());
    }

    /**
     * Gets the maximum number of packets a client can send over 1 second.
     *
     * @return the packet count limit over 1 second, 0 if not enabled
     */
    public static int getRateLimit() {
        return rateLimit;
    }

    /**
     * Changes the number of packet a client can send over 1 second without being disconnected.
     *
     * @param rateLimit the number of packet, 0 to disable
     */
    public static void setRateLimit(int rateLimit) {
        MinecraftServer.rateLimit = rateLimit;
    }

    /**
     * Gets the maximum packet size (in bytes) that a client can send without getting disconnected.
     *
     * @return the maximum packet size
     */
    public static int getMaxPacketSize() {
        return maxPacketSize;
    }

    /**
     * Changes the maximum packet size (in bytes) that a client can send without getting disconnected.
     *
     * @param maxPacketSize the new max packet size
     */
    public static void setMaxPacketSize(int maxPacketSize) {
        MinecraftServer.maxPacketSize = maxPacketSize;
    }

    /**
     * Gets the server difficulty showed in game option.
     *
     * @return the server difficulty
     */
    @NotNull
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Changes the server difficulty and send the appropriate packet to all connected clients.
     *
     * @param difficulty the new server difficulty
     */
    public static void setDifficulty(@NotNull Difficulty difficulty) {
        MinecraftServer.difficulty = difficulty;
        // Send the packet to all online players
        PacketUtils.sendGroupedPacket(connectionManager.getOnlinePlayers(), new ServerDifficultyPacket(difficulty, true));
    }

    /**
     * Gets the global event handler.
     * <p>
     * Used to register event callback at a global scale.
     *
     * @return the global event handler
     */
    public static @NotNull GlobalEventHandler getGlobalEventHandler() {
        return GLOBAL_EVENT_HANDLER;
    }

    /**
     * Gets the manager handling all incoming packets
     *
     * @return the packet listener manager
     */
    public static PacketListenerManager getPacketListenerManager() {
        checkInitStatus(packetListenerManager);
        return packetListenerManager;
    }

    /**
     * Gets the manager handling all registered instances.
     *
     * @return the instance manager
     */
    public static InstanceManager getInstanceManager() {
        checkInitStatus(instanceManager);
        return instanceManager;
    }

    /**
     * Gets the manager handling {@link net.minestom.server.instance.block.BlockHandler block handlers}
     * and {@link BlockPlacementRule placement rules}.
     *
     * @return the block manager
     */
    public static BlockManager getBlockManager() {
        checkInitStatus(blockManager);
        return blockManager;
    }

    /**
     * Gets the manager handling commands.
     *
     * @return the command manager
     */
    public static CommandManager getCommandManager() {
        checkInitStatus(commandManager);
        return commandManager;
    }

    /**
     * Gets the manager handling recipes show to the clients.
     *
     * @return the recipe manager
     */
    public static RecipeManager getRecipeManager() {
        checkInitStatus(recipeManager);
        return recipeManager;
    }

    /**
     * Gets the manager handling storage.
     *
     * @return the storage manager
     */
    public static StorageManager getStorageManager() {
        checkInitStatus(storageManager);
        return storageManager;
    }

    /**
     * Gets the manager handling {@link DataType} used by {@link SerializableData}.
     *
     * @return the data manager
     */
    @Deprecated
    public static DataManager getDataManager() {
        checkInitStatus(dataManager);
        return dataManager;
    }

    /**
     * Gets the manager handling teams.
     *
     * @return the team manager
     */
    public static TeamManager getTeamManager() {
        checkInitStatus(teamManager);
        return teamManager;
    }

    /**
     * Gets the manager handling scheduled tasks.
     *
     * @return the scheduler manager
     */
    public static SchedulerManager getSchedulerManager() {
        checkInitStatus(schedulerManager);
        return schedulerManager;
    }

    /**
     * Gets the manager handling server monitoring.
     *
     * @return the benchmark manager
     */
    public static BenchmarkManager getBenchmarkManager() {
        checkInitStatus(benchmarkManager);
        return benchmarkManager;
    }

    /**
     * Gets the exception manager for exception handling.
     *
     * @return the exception manager
     */
    public static ExceptionManager getExceptionManager() {
        checkInitStatus(exceptionManager);
        return exceptionManager;
    }

    /**
     * Gets the manager handling server connections.
     *
     * @return the connection manager
     */
    public static ConnectionManager getConnectionManager() {
        checkInitStatus(connectionManager);
        return connectionManager;
    }

    /**
     * Gets the boss bar manager.
     *
     * @return the boss bar manager
     */
    public static BossBarManager getBossBarManager() {
        checkInitStatus(bossBarManager);
        return bossBarManager;
    }

    /**
     * Gets the object handling the client packets processing.
     * <p>
     * Can be used if you want to convert a buffer to a client packet object.
     *
     * @return the packet processor
     */
    public static PacketProcessor getPacketProcessor() {
        checkInitStatus(packetProcessor);
        return packetProcessor;
    }

    /**
     * Gets if the server is up and running.
     *
     * @return true if the server is started
     */
    public static boolean isStarted() {
        return started;
    }

    /**
     * Gets if the server is currently being shutdown using {@link #stopCleanly()}.
     *
     * @return true if the server is being stopped
     */
    public static boolean isStopping() {
        return stopping;
    }

    /**
     * Gets the chunk view distance of the server.
     *
     * @return the chunk view distance
     */
    public static int getChunkViewDistance() {
        return chunkViewDistance;
    }

    /**
     * Changes the chunk view distance of the server.
     *
     * @param chunkViewDistance the new chunk view distance
     * @throws IllegalArgumentException if {@code chunkViewDistance} is not between 2 and 32
     * @deprecated should instead be defined with a java property
     */
    @Deprecated
    public static void setChunkViewDistance(int chunkViewDistance) {
        Check.stateCondition(started, "You cannot change the chunk view distance after the server has been started.");
        Check.argCondition(!MathUtils.isBetween(chunkViewDistance, 2, 32),
                "The chunk view distance must be between 2 and 32");
        MinecraftServer.chunkViewDistance = chunkViewDistance;
    }

    /**
     * Gets the entity view distance of the server.
     *
     * @return the entity view distance
     */
    public static int getEntityViewDistance() {
        return entityViewDistance;
    }

    /**
     * Changes the entity view distance of the server.
     *
     * @param entityViewDistance the new entity view distance
     * @throws IllegalArgumentException if {@code entityViewDistance} is not between 0 and 32
     * @deprecated should instead be defined with a java property
     */
    @Deprecated
    public static void setEntityViewDistance(int entityViewDistance) {
        Check.stateCondition(started, "You cannot change the entity view distance after the server has been started.");
        Check.argCondition(!MathUtils.isBetween(entityViewDistance, 0, 32),
                "The entity view distance must be between 0 and 32");
        MinecraftServer.entityViewDistance = entityViewDistance;
    }

    /**
     * Gets the compression threshold of the server.
     *
     * @return the compression threshold, 0 means that compression is disabled
     */
    public static int getCompressionThreshold() {
        return compressionThreshold;
    }

    /**
     * Changes the compression threshold of the server.
     * <p>
     * WARNING: this need to be called before {@link #start(String, int, ResponseDataConsumer)}.
     *
     * @param compressionThreshold the new compression threshold, 0 to disable compression
     * @throws IllegalStateException if this is called after the server started
     */
    public static void setCompressionThreshold(int compressionThreshold) {
        Check.stateCondition(started, "The compression threshold cannot be changed after the server has been started.");
        MinecraftServer.compressionThreshold = compressionThreshold;
    }

    /**
     * Gets if the built in Minestom terminal is enabled.
     *
     * @return true if the terminal is enabled
     */
    public static boolean isTerminalEnabled() {
        return terminalEnabled;
    }

    /**
     * Enabled/disables the built in Minestom terminal.
     *
     * @param enabled true to enable, false to disable
     */
    public static void setTerminalEnabled(boolean enabled) {
        Check.stateCondition(started, "Terminal settings may not be changed after starting the server.");
        MinecraftServer.terminalEnabled = enabled;
    }

    /**
     * Gets the consumer executed to show server-list data.
     *
     * @return the response data consumer
     * @deprecated listen to the {@link net.minestom.server.event.server.ServerListPingEvent} instead
     */
    @Deprecated
    public static ResponseDataConsumer getResponseDataConsumer() {
        checkInitStatus(responseDataConsumer);
        return responseDataConsumer;
    }

    /**
     * Gets the manager handling dimensions.
     *
     * @return the dimension manager
     */
    public static DimensionTypeManager getDimensionTypeManager() {
        checkInitStatus(dimensionTypeManager);
        return dimensionTypeManager;
    }

    /**
     * Gets the manager handling biomes.
     *
     * @return the biome manager
     */
    public static BiomeManager getBiomeManager() {
        checkInitStatus(biomeManager);
        return biomeManager;
    }

    /**
     * Gets the manager handling advancements.
     *
     * @return the advancement manager
     */
    public static AdvancementManager getAdvancementManager() {
        checkInitStatus(advancementManager);
        return advancementManager;
    }

    /**
     * Get the manager handling {@link Extension}.
     *
     * @return the extension manager
     */
    public static ExtensionManager getExtensionManager() {
        checkInitStatus(extensionManager);
        return extensionManager;
    }

    /**
     * Gets the manager handling tags.
     *
     * @return the tag manager
     */
    public static TagManager getTagManager() {
        checkInitStatus(tagManager);
        return tagManager;
    }

    /**
     * Gets the manager handling the server ticks.
     *
     * @return the update manager
     */
    public static UpdateManager getUpdateManager() {
        checkInitStatus(updateManager);
        return updateManager;
    }

    public static Server getServer() {
        checkInitStatus(server);
        return server;
    }

    /**
     * Starts the server.
     * <p>
     * It should be called after {@link #init()} and probably your own initialization code.
     *
     * @param address              the server address
     * @param port                 the server port
     * @param responseDataConsumer the response data consumer, can be null
     * @throws IllegalStateException if called before {@link #init()} or if the server is already running
     * @deprecated use {@link #start(String, int)} and listen to the {@link net.minestom.server.event.server.ServerListPingEvent} event instead of ResponseDataConsumer
     */
    @Deprecated
    public void start(@NotNull String address, int port, @Nullable ResponseDataConsumer responseDataConsumer) {
        MinecraftServer.responseDataConsumer = responseDataConsumer;
        start(address, port);
    }

    /**
     * Starts the server.
     * <p>
     * It should be called after {@link #init()} and probably your own initialization code.
     *
     * @param address the server address
     * @param port    the server port
     * @throws IllegalStateException if called before {@link #init()} or if the server is already running
     */
    public void start(@NotNull String address, int port) {
        Check.stateCondition(!initialized, "#start can only be called after #init");
        Check.stateCondition(started, "The server is already started");

        MinecraftServer.started = true;

        LOGGER.info("Starting Minestom server.");

        updateManager.start();

        // Init server
        try {
            server.init(new InetSocketAddress(address, port));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (extensionManager.shouldLoadOnStartup()) {
            final long loadStartTime = System.nanoTime();
            // Load extensions
            extensionManager.loadExtensions();
            // Init extensions
            extensionManager.getExtensions().forEach(Extension::preInitialize);
            extensionManager.getExtensions().forEach(Extension::initialize);
            extensionManager.getExtensions().forEach(Extension::postInitialize);

            final double loadTime = MathUtils.round((System.nanoTime() - loadStartTime) / 1_000_000D, 2);
            LOGGER.info("Extensions loaded in {}ms", loadTime);
        } else {
            LOGGER.warn("Extension loadOnStartup option is set to false, extensions are therefore neither loaded or initialized.");
        }

        // Start server
        server.start();

        LOGGER.info("Minestom server started successfully.");

        if (terminalEnabled) {
            MinestomTerminal.start();
        }

        // Stop the server on SIGINT
        Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        if (stopping) return;
        stopping = true;
        LOGGER.info("Stopping Minestom server.");
        extensionManager.unloadAllExtensions();
        updateManager.stop();
        schedulerManager.shutdown();
        connectionManager.shutdown();
        server.stop();
        storageManager.getLoadedLocations().forEach(StorageLocation::close);
        LOGGER.info("Unloading all extensions.");
        extensionManager.shutdown();
        LOGGER.info("Shutting down all thread pools.");
        benchmarkManager.disable();
        MinestomTerminal.stop();
        MinestomThreadPool.shutdownAll();
        LOGGER.info("Minestom server stopped successfully.");
    }

    private static void checkInitStatus(@Nullable Object object) {
        /*Check.stateCondition(Objects.isNull(object),
                "You cannot access the manager before MinecraftServer#init, " +
                        "if you are developing an extension be sure to retrieve them at least after Extension#preInitialize");*/
    }

    private static int getThreadCount(@NotNull String property, int count) {
        return Integer.getInteger(property, Math.max(1, count));
    }
}
