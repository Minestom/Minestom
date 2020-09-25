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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;

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
    public static final int CHUNK_VIEW_DISTANCE = 10;
    public static final int ENTITY_VIEW_DISTANCE = 5;
    public static final int COMPRESSION_THRESHOLD = 256;
    // TODO
    public static final int MAX_PACKET_SIZE = 300_000;
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
    private static ResponseDataConsumer responseDataConsumer;
    private static String brandName = "Minestom";
    private static Difficulty difficulty = Difficulty.NORMAL;
    private static LootTableManager lootTableManager;
    private static TagManager tagManager;

    //Mojang Auth
    @Getter
    private static KeyPair keyPair = MojangCrypt.generateKeyPair();
    @Getter
    private static AuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
    @Getter
    private static MinecraftSessionService sessionService = authService.createMinecraftSessionService();

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

        minecraftServer = new MinecraftServer();

        return minecraftServer;
    }

    /**
     * Get the current server brand name
     *
     * @return the server brand name
     */
    public static String getBrandName() {
        return brandName;
    }

    /**
     * Change the server brand name, update the name to all connected players
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
     * Get the max number of packets a client can send over 1 second
     *
     * @return the packet count limit over 1 second
     */
    public static int getRateLimit() {
        return rateLimit;
    }

    /**
     * Change the number of packet a client can send over 1 second without being disconnected
     *
     * @param rateLimit the number of packet, 0 to disable
     */
    public static void setRateLimit(int rateLimit) {
        MinecraftServer.rateLimit = rateLimit;
    }

    /**
     * Get the server difficulty showed in game option
     *
     * @return the server difficulty
     */
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Change the server difficulty and send the appropriate packet to all connected clients
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
     * Get the manager handling all incoming packets
     *
     * @return the packet listener manager
     */
    public static PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    /**
     * Get the netty server
     *
     * @return the netty server
     */
    public static NettyServer getNettyServer() {
        return nettyServer;
    }

    /**
     * Get the manager handling all registered instances
     *
     * @return the instance manager
     */
    public static InstanceManager getInstanceManager() {
        return instanceManager;
    }

    /**
     * Get the manager handling {@link CustomBlock} and {@link BlockPlacementRule}
     *
     * @return the block manager
     */
    public static BlockManager getBlockManager() {
        return blockManager;
    }

    /**
     * Get the manager handling waiting players
     *
     * @return the entity manager
     */
    public static EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get the manager handling commands
     *
     * @return the command manager
     */
    public static CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Get the manager handling recipes show to the clients
     *
     * @return the recipe manager
     */
    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * Get the manager handling storage
     *
     * @return the storage manager
     */
    public static StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Get the manager handling {@link DataType} used by {@link SerializableData}
     *
     * @return the data manager
     */
    public static DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Get the manager handling teams
     *
     * @return the team manager
     */
    public static TeamManager getTeamManager() {
        return teamManager;
    }

    /**
     * Get the manager handling scheduled tasks
     *
     * @return the scheduler manager
     */
    public static SchedulerManager getSchedulerManager() {
        return schedulerManager;
    }

    /**
     * Get the manager handling server monitoring
     *
     * @return the benchmark manager
     */
    public static BenchmarkManager getBenchmarkManager() {
        return benchmarkManager;
    }

    /**
     * Get the manager handling server connections
     *
     * @return the connection manager
     */
    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Get the consumer executed to show server-list data
     *
     * @return the response data consumer
     */
    public static ResponseDataConsumer getResponseDataConsumer() {
        return responseDataConsumer;
    }

    /**
     * Get the manager handling loot tables
     *
     * @return the loot table manager
     */
    public static LootTableManager getLootTableManager() {
        return lootTableManager;
    }

    /**
     * Get the manager handling dimensions
     *
     * @return the dimension manager
     */
    public static DimensionTypeManager getDimensionTypeManager() {
        return dimensionTypeManager;
    }

    public static BiomeManager getBiomeManager() {
        return biomeManager;
    }

    /**
     * Get the manager handling advancements
     *
     * @return the advancement manager
     */
    public static AdvancementManager getAdvancementManager() {
        return advancementManager;
    }

    /**
     * Get the manager handling tags
     *
     * @return the tag manager
     */
    public static TagManager getTagManager() {
        return tagManager;
    }

    /**
     * Get the manager handling the server ticks
     *
     * @return the update manager
     */
    public static UpdateManager getUpdateManager() {
        return updateManager;
    }

    /**
     * Start the server
     *
     * @param address              the server address
     * @param port                 the server port
     * @param responseDataConsumer the response data consumer, can be null
     */
    public void start(String address, int port, ResponseDataConsumer responseDataConsumer) {
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
    }

    /**
     * Start the server
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

}
