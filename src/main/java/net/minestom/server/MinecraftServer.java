package net.minestom.server;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.benchmark.BenchmarkManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.data.DataManager;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.fluids.Fluid;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
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
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;

public class MinecraftServer {
    @Getter
    private final static Logger LOGGER = LoggerFactory.getLogger(MinecraftServer.class);

    public static final int PROTOCOL_VERSION = 736;

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_MAIN_UPDATE = "Ms-MainUpdate";

    public static final String THREAD_NAME_TICK = "Ms-Tick";

    public static final String THREAD_NAME_PACKET_WRITER = "Ms-PacketWriterPool";
    public static final int THREAD_COUNT_PACKET_WRITER = 2;

    public static final String THREAD_NAME_BLOCK_BATCH = "Ms-BlockBatchPool";
    public static final int THREAD_COUNT_BLOCK_BATCH = 2;

    public static final String THREAD_NAME_ENTITIES_PATHFINDING = "Ms-EntitiesPathFinding";
    public static final int THREAD_COUNT_ENTITIES_PATHFINDING = 2;

    public static final String THREAD_NAME_SCHEDULER = "Ms-SchedulerPool";
    public static final int THREAD_COUNT_SCHEDULER = 1;

    public static final String THREAD_NAME_PARALLEL_CHUNK_SAVING = "Ms-ParallelChunkSaving";
    public static final int THREAD_COUNT_PARALLEL_CHUNK_SAVING = 4;

    // Config
    public static final int CHUNK_VIEW_DISTANCE = 10;
    public static final int ENTITY_VIEW_DISTANCE = 5;
    public static final int COMPRESSION_THRESHOLD = 256;
    // Can be modified at performance cost when decreased
    private static final int MS_TO_SEC = 1000;
    public static final int TICK_MS = MS_TO_SEC / 20;
    public static final int TICK_PER_SECOND = MS_TO_SEC / TICK_MS;

    //Extras
    @Getter
    @Setter
    private static boolean fixLighting = true;

    // Networking
    private static PacketProcessor packetProcessor;
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
        Biome.values();
        Fluid.values();

        connectionManager = new ConnectionManager();
        packetProcessor = new PacketProcessor();
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

        updateManager = new UpdateManager();

        lootTableManager = new LootTableManager();
        tagManager = new TagManager();

        nettyServer = new NettyServer(packetProcessor);

        // Registry
        try {
            ResourceGatherer.ensureResourcesArePresent("1.16.1", null); // TODO: provide a way to give a path override, probably via launch arguments?
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
     * @param brandName
     */
    public static void setBrandName(String brandName) {
        Check.notNull(brandName, "The brand name cannot be null");
        MinecraftServer.brandName = brandName;

        PluginMessagePacket brandMessage = PluginMessagePacket.getBrandPacket();
        PacketWriterUtils.writeAndSend(connectionManager.getOnlinePlayers(), brandMessage);
    }

    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(Difficulty difficulty) {
        MinecraftServer.difficulty = difficulty;
        for (Player player : connectionManager.getOnlinePlayers()) {
            ServerDifficultyPacket serverDifficultyPacket = new ServerDifficultyPacket();
            serverDifficultyPacket.difficulty = difficulty;
            serverDifficultyPacket.locked = true;
            player.getPlayerConnection().sendPacket(serverDifficultyPacket);
        }
    }

    public static PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    public static NettyServer getNettyServer() {
        return nettyServer;
    }

    public static InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public static BlockManager getBlockManager() {
        return blockManager;
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public static StorageManager getStorageManager() {
        return storageManager;
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static TeamManager getTeamManager() {
        return teamManager;
    }

    public static SchedulerManager getSchedulerManager() {
        return schedulerManager;
    }

    public static BenchmarkManager getBenchmarkManager() {
        return benchmarkManager;
    }

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public static ResponseDataConsumer getResponseDataConsumer() {
        return responseDataConsumer;
    }

    public static LootTableManager getLootTableManager() {
        return lootTableManager;
    }

    public static DimensionTypeManager getDimensionTypeManager() {
        return dimensionTypeManager;
    }

    public static TagManager getTagManager() {
        return tagManager;
    }

    public void start(String address, int port, ResponseDataConsumer responseDataConsumer, boolean bungeecordEnabled) {
        LOGGER.info("Starting Minestom server.");
        MinecraftServer.responseDataConsumer = responseDataConsumer;
        updateManager.start(bungeecordEnabled);
        nettyServer.start(address, port);
        LOGGER.info("Minestom server started successfully.");
    }

    public void start(String address, int port) { start(address, port, null, false); }

    public void start(String address, int port, boolean bungeecordEnabled) {
        start(address, port, null, bungeecordEnabled);
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        LOGGER.info("Stopping Minestom server.");
        updateManager.stop();
        nettyServer.stop();
        schedulerManager.shutdown();
        storageManager.getLoadedFolders().forEach(StorageFolder::close);
        LOGGER.info("Shutting down all thread pools.");
        benchmarkManager.disable();
        commandManager.stopConsoleThread();
        MinestomThread.shutdownAll();
        LOGGER.info("Minestom server stopped successfully.");
    }

}
