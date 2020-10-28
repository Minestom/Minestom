package net.minestom.server;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.benchmark.BenchmarkManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.EntityType;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.fluids.Fluid;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.netty.NettyServer;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.sound.Sound;
import net.minestom.server.stat.StatisticType;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;

/**
 * The main server class used to start the server and retrieve all the managers.
 * <p>
 * The server needs to be initialized with {@link #init()} and started with {@link #start(String, int)}.
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
public class MinecraftServer {

    @Getter
    private final static Logger LOGGER = LoggerFactory.getLogger(MinecraftServer.class);

    public static final String VERSION_NAME = "1.16.3";
    public static final int PROTOCOL_VERSION = 753;

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_MAIN_UPDATE = "Ms-MainUpdate";

    public static final String THREAD_NAME_TICK = "Ms-Tick";

    public static final String THREAD_NAME_PACKET_WRITER = "Ms-PacketWriterPool";
    public static final int THREAD_COUNT_PACKET_WRITER = 2;

    public static final String THREAD_NAME_BLOCK_BATCH = "Ms-BlockBatchPool";
    public static final int THREAD_COUNT_BLOCK_BATCH = 2;

    public static final String THREAD_NAME_SCHEDULER = "Ms-SchedulerPool";
    public static final int THREAD_COUNT_SCHEDULER = 1;

    public static final String THREAD_NAME_PARALLEL_CHUNK_SAVING = "Ms-ParallelChunkSaving";
    public static final int THREAD_COUNT_PARALLEL_CHUNK_SAVING = 4;

    // Config
    // Can be modified at performance cost when increased
    public static final int TICK_PER_SECOND = 20;
    private static final int MS_TO_SEC = 1000;
    public static final int TICK_MS = MS_TO_SEC / TICK_PER_SECOND;

    @Getter
    @Setter
    private static boolean hardcoreLook = false;

    //Extras
    @Getter
    @Setter
    private static boolean fixLighting = true;

    //Rate Limiting
    private static int rateLimit = 0;
    // TODO
    public static final int MAX_PACKET_SIZE = 300_000;

    private static PacketListenerManager packetListenerManager;
    private static NettyServer nettyServer;

    // In-Game Manager
    private static ConnectionManager connectionManager;
    private static InstanceManager instanceManager;
    private static BlockManager blockManager;
    private static EntityManager entityManager;
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

    private static ExtensionManager extensionManager;

    private static UpdateManager updateManager;
    private static MinecraftServer minecraftServer;

    // Data
    private static boolean initialized;
    private static boolean started;

    private static int chunkViewDistance = 10;
    private static int entityViewDistance = 5;
    private static int compressionThreshold = 256;
    private static ResponseDataConsumer responseDataConsumer;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;
    private static LootTableManager lootTableManager;
    private static TagManager tagManager;

    //Mojang Auth
    @Getter
    private static final KeyPair keyPair = MojangCrypt.generateKeyPair();
    @Getter
    private static final AuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
    @Getter
    private static final MinecraftSessionService sessionService = authService.createMinecraftSessionService();

    public static MinecraftServer init() {
        if (minecraftServer != null) // don't init twice
            return minecraftServer;
        extensionManager = new ExtensionManager();
        extensionManager.loadExtensions();

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
        Sound.values();
        Particle.values();
        StatisticType.values();
        Fluid.values();

        connectionManager = new ConnectionManager();
        // Networking
        final PacketProcessor packetProcessor = new PacketProcessor();
        packetListenerManager = new PacketListenerManager();

        instanceManager = new InstanceManager();
        blockManager = new BlockManager();
        entityManager = new EntityManager();
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
    public static String getBrandName() {
        return brandName;
    }

    /**
     * Changes the server brand name, update the name to all connected players.
     *
     * @param brandName the server brand name
     */
    public static void setBrandName(String brandName) {
        Check.notNull(brandName, "The brand name cannot be null");
        MinecraftServer.brandName = brandName;

        PluginMessagePacket brandMessage = PluginMessagePacket.getBrandPacket();
        PacketWriterUtils.writeAndSend(connectionManager.getOnlinePlayers(), brandMessage);
    }

    /**
     * Gets the max number of packets a client can send over 1 second.
     *
     * @return the packet count limit over 1 second
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
     * Gets the server difficulty showed in game option.
     *
     * @return the server difficulty
     */
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Changes the server difficulty and send the appropriate packet to all connected clients.
     *
     * @param difficulty the new server difficulty
     */
    public static void setDifficulty(Difficulty difficulty) {
        MinecraftServer.difficulty = difficulty;

        // The difficulty packet
        ServerDifficultyPacket serverDifficultyPacket = new ServerDifficultyPacket();
        serverDifficultyPacket.difficulty = difficulty;
        serverDifficultyPacket.locked = true; // Can only be modified on singleplayer
        // Send the packet to all online players
        PacketWriterUtils.writeAndSend(connectionManager.getOnlinePlayers(), serverDifficultyPacket);
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
     * Gets the manager handling waiting players.
     *
     * @return the entity manager
     */
    public static EntityManager getEntityManager() {
        checkInitStatus(entityManager);
        return entityManager;
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
     * Gets the manager handling server connections.
     *
     * @return the connection manager
     */
    public static ConnectionManager getConnectionManager() {
        checkInitStatus(connectionManager);
        return connectionManager;
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
     * Gets the chunk view distance of the server.
     *
     * @return the chunk view distance
     */
    public static int getChunkViewDistance() {
        return chunkViewDistance;
    }

    /**
     * Changes the chunk view distance of the server.
     * <p>
     * WARNING: this need to be called before {@link #start(String, int, ResponseDataConsumer)}.
     *
     * @param chunkViewDistance the new chunk view distance
     * @throws IllegalStateException if this is called after the server started
     */
    public static void setChunkViewDistance(int chunkViewDistance) {
        Check.stateCondition(started, "The chunk view distance cannot be changed after the server has been started.");
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
     * <p>
     * WARNING: this need to be called before {@link #start(String, int, ResponseDataConsumer)}.
     *
     * @param entityViewDistance the new entity view distance
     * @throws IllegalStateException if this is called after the server started
     */
    public static void setEntityViewDistance(int entityViewDistance) {
        Check.stateCondition(started, "The entity view distance cannot be changed after the server has been started.");
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
     * Gets the consumer executed to show server-list data.
     *
     * @return the response data consumer
     */
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
     * Starts the server.
     *
     * @param address              the server address
     * @param port                 the server port
     * @param responseDataConsumer the response data consumer, can be null
     */
    public void start(String address, int port, ResponseDataConsumer responseDataConsumer) {
        Check.stateCondition(!initialized, "#start can only be called after #init");
        Check.stateCondition(started, "The server is already started");

        LOGGER.info("Starting Minestom server.");
        MinecraftServer.responseDataConsumer = responseDataConsumer;
        updateManager.start();
        nettyServer.start(address, port);
        long t1 = -System.nanoTime();
        // Init extensions
        // TODO: Extensions should handle depending on each other and have a load-order.
        extensionManager.getExtensions().forEach(Extension::preInitialize);
        extensionManager.getExtensions().forEach(Extension::initialize);
        extensionManager.getExtensions().forEach(Extension::postInitialize);

        LOGGER.info("Extensions loaded in " + (t1 + System.nanoTime()) / 1_000_000D + "ms");
        LOGGER.info("Minestom server started successfully.");

        MinecraftServer.started = true;
    }

    /**
     * Starts the server.
     *
     * @param address the server address
     * @param port    the server port
     */
    public void start(String address, int port) {
        start(address, port, null);
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        LOGGER.info("Stopping Minestom server.");
        updateManager.stop();
        nettyServer.stop();
        schedulerManager.shutdown();
        storageManager.getLoadedLocations().forEach(StorageLocation::close);
        LOGGER.info("Shutting down all thread pools.");
        benchmarkManager.disable();
        commandManager.stopConsoleThread();
        MinestomThread.shutdownAll();
        LOGGER.info("Minestom server stopped successfully.");
    }

    private static void checkInitStatus(@Nullable Object object) {
        /*Check.stateCondition(Objects.isNull(object),
                "You cannot access the manager before MinecraftServer#init, " +
                        "if you are developing an extension be sure to retrieve them at least after Extension#preInitialize");*/
    }

}
