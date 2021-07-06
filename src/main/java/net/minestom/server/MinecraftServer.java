package net.minestom.server;

import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.NettyServer;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.network.packet.server.play.UpdateViewDistancePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.statistic.StatisticType;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.terminal.MinestomTerminal;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The main server class used to start the server and retrieve all the managers.
 * <p>
 * The server needs to be initialized with {@link #init()} and started with {@link #start(String, int)}.
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
public final class MinecraftServer {

    public final static Logger LOGGER = LoggerFactory.getLogger(MinecraftServer.class);

    public static final String VERSION_NAME = "1.17";
    public static final int PROTOCOL_VERSION = 755;

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
    private static int rateLimit = 1000;
    private static int maxPacketSize = 30_000;
    // Network
    private static PacketListenerManager packetListenerManager;
    private static PacketProcessor packetProcessor;
    private static NettyServer nettyServer;
    private static int nettyThreadCount = Runtime.getRuntime().availableProcessors();
    private static boolean processNettyErrors = true;

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
    private static boolean stopping;

    private static int chunkViewDistance = 8;
    private static int entityViewDistance = 5;
    private static int compressionThreshold = 256;
    private static boolean packetCaching = true;
    private static boolean groupedPacket = true;
    private static boolean terminalEnabled = System.getProperty("minestom.terminal.disabled") == null;
    private static ResponseDataConsumer responseDataConsumer;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;
    private static LootTableManager lootTableManager;
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
        Block.values();
        Material.values();
        PotionType.values();
        PotionEffect.values();
        Enchantment.values();
        EntityType.values();
        SoundEvent.values();
        Particle.values();
        StatisticType.values();
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

        lootTableManager = new LootTableManager();
        tagManager = new TagManager();

        nettyServer = new NettyServer(packetProcessor);

        // Registry
        try {
            ResourceGatherer.ensureResourcesArePresent(VERSION_NAME);
        } catch (IOException e) {
            LOGGER.error("An error happened during resource gathering. Minestom will attempt to load anyway, but things may not work, and crashes can happen.", e);
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
        ServerDifficultyPacket serverDifficultyPacket = new ServerDifficultyPacket();
        serverDifficultyPacket.difficulty = difficulty;
        serverDifficultyPacket.locked = true; // Can only be modified on single-player
        PacketUtils.sendGroupedPacket(connectionManager.getOnlinePlayers(), serverDifficultyPacket);
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
     * Gets the netty server.
     *
     * @return the netty server
     */
    public static NettyServer getNettyServer() {
        checkInitStatus(nettyServer);
        return nettyServer;
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
     * Gets the manager handling {@link CustomBlock} and {@link BlockPlacementRule}.
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
     */
    public static void setChunkViewDistance(int chunkViewDistance) {
        Check.argCondition(!MathUtils.isBetween(chunkViewDistance, 2, 32),
                "The chunk view distance must be between 2 and 32");
        MinecraftServer.chunkViewDistance = chunkViewDistance;
        if (started) {

            for (final Player player : connectionManager.getOnlinePlayers()) {
                final Chunk playerChunk = player.getChunk();
                if (playerChunk != null) {

                    UpdateViewDistancePacket updateViewDistancePacket = new UpdateViewDistancePacket();
                    updateViewDistancePacket.viewDistance = player.getChunkRange();
                    player.getPlayerConnection().sendPacket(updateViewDistancePacket);

                    player.refreshVisibleChunks(playerChunk);
                }
            }
        }
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
     */
    public static void setEntityViewDistance(int entityViewDistance) {
        Check.argCondition(!MathUtils.isBetween(entityViewDistance, 0, 32),
                "The entity view distance must be between 0 and 32");
        MinecraftServer.entityViewDistance = entityViewDistance;
        if (started) {
            for (final Player player : connectionManager.getOnlinePlayers()) {
                final Chunk playerChunk = player.getChunk();
                if (playerChunk != null) {
                    player.refreshVisibleEntities(playerChunk);
                }
            }
        }
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
     * Gets if the packet caching feature is enabled.
     * <p>
     * This feature allows some packets (implementing the {@link net.minestom.server.utils.cache.CacheablePacket} to be cached
     * in order to do not have to be written and compressed over and over again), this is especially useful for chunk and light packets.
     * <p>
     * It is enabled by default and it is our recommendation,
     * you should only disable it if you want to focus on low memory usage
     * at the cost of many packet writing and compression.
     *
     * @return true if the packet caching feature is enabled, false otherwise
     */
    public static boolean hasPacketCaching() {
        return packetCaching;
    }

    /**
     * Enables or disable packet caching.
     *
     * @param packetCaching true to enable packet caching
     * @throws IllegalStateException if this is called after the server started
     * @see #hasPacketCaching()
     */
    public static void setPacketCaching(boolean packetCaching) {
        Check.stateCondition(started, "You cannot change the packet caching value after the server has been started.");
        MinecraftServer.packetCaching = packetCaching;
    }

    /**
     * Gets if the packet caching feature is enabled.
     * <p>
     * This features allow sending the exact same packet/buffer to multiple connections.
     * It does provide a great performance benefit by allocating and writing/compressing only once.
     * <p>
     * It is enabled by default and it is our recommendation,
     * you should only disable it if you want to modify packet per-players instead of sharing it.
     * Disabling the feature would result in performance decrease.
     *
     * @return true if the grouped packet feature is enabled, false otherwise
     */
    public static boolean hasGroupedPacket() {
        return groupedPacket;
    }

    /**
     * Enables or disable grouped packet.
     *
     * @param groupedPacket true to enable grouped packet
     * @throws IllegalStateException if this is called after the server started
     * @see #hasGroupedPacket()
     */
    public static void setGroupedPacket(boolean groupedPacket) {
        Check.stateCondition(started, "You cannot change the grouped packet value after the server has been started.");
        MinecraftServer.groupedPacket = groupedPacket;
    }

    /**
     * Gets if the built in Minestom terminal is enabled.
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
     * Gets the manager handling loot tables.
     *
     * @return the loot table manager
     */
    public static LootTableManager getLootTableManager() {
        checkInitStatus(lootTableManager);
        return lootTableManager;
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

    /**
     * Gets the number of threads used by Netty.
     * <p>
     * Is the number of vCPU by default.
     *
     * @return the number of netty threads
     */
    public static int getNettyThreadCount() {
        return nettyThreadCount;
    }

    /**
     * Changes the number of threads used by Netty.
     *
     * @param nettyThreadCount the number of threads
     * @throws IllegalStateException if the server is already started
     */
    public static void setNettyThreadCount(int nettyThreadCount) {
        Check.stateCondition(started, "Netty thread count can only be changed before the server starts!");
        MinecraftServer.nettyThreadCount = nettyThreadCount;
    }

    /**
     * Gets if the server should process netty errors and other unnecessary netty events.
     *
     * @return should process netty errors
     */
    public static boolean shouldProcessNettyErrors() {
        return processNettyErrors;
    }

    /**
     * Sets if the server should process netty errors and other unnecessary netty events.
     * false is faster
     *
     * @param processNettyErrors should process netty errors
     */
    public static void setShouldProcessNettyErrors(boolean processNettyErrors) {
        MinecraftServer.processNettyErrors = processNettyErrors;
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

        // Init & start the TCP server
        nettyServer.init();
        nettyServer.start(address, port);

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

        LOGGER.info("Minestom server started successfully.");

        if (terminalEnabled) {
            MinestomTerminal.start();
        }
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        stopping = true;
        LOGGER.info("Stopping Minestom server.");
        extensionManager.unloadAllExtensions();
        updateManager.stop();
        schedulerManager.shutdown();
        connectionManager.shutdown();
        nettyServer.stop();
        storageManager.getLoadedLocations().forEach(StorageLocation::close);
        LOGGER.info("Unloading all extensions.");
        extensionManager.shutdown();
        LOGGER.info("Shutting down all thread pools.");
        benchmarkManager.disable();
        MinestomTerminal.stop();
        MinestomThread.shutdownAll();
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
