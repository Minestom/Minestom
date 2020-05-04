package net.minestom.server;

import net.minestom.server.benchmark.BenchmarkManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.data.DataManager;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.netty.NettyServer;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.registry.RegistryMain;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.world.Difficulty;

import java.io.IOException;

public class MinecraftServer {

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_MAIN_UPDATE = "Ms-MainUpdate";
    public static final int THREAD_COUNT_MAIN_UPDATE = 1; // Keep it to 1

    public static final String THREAD_NAME_PACKET_WRITER = "Ms-PacketWriterPool";
    public static final int THREAD_COUNT_PACKET_WRITER = 2;

    public static final String THREAD_NAME_BLOCK_BATCH = "Ms-BlockBatchPool";
    public static final int THREAD_COUNT_BLOCK_BATCH = 2;

    public static final String THREAD_NAME_BLOCK_UPDATE = "Ms-BlockUpdatePool";
    public static final int THREAD_COUNT_BLOCK_UPDATE = 2;

    public static final String THREAD_NAME_ENTITIES = "Ms-EntitiesPool";
    public static final int THREAD_COUNT_ENTITIES = 2;

    public static final String THREAD_NAME_ENTITIES_PATHFINDING = "Ms-EntitiesPathFinding";
    public static final int THREAD_COUNT_ENTITIES_PATHFINDING = 2;

    public static final String THREAD_NAME_PLAYERS_ENTITIES = "Ms-PlayersPool";
    public static final int THREAD_COUNT_PLAYERS_ENTITIES = 2;

    public static final String THREAD_NAME_SCHEDULER = "Ms-SchedulerPool";
    public static final int THREAD_COUNT_SCHEDULER = 1;

    // Config
    public static final int CHUNK_VIEW_DISTANCE = 10;
    public static final int ENTITY_VIEW_DISTANCE = 5;
    // Can be modified at performance cost when decreased
    private static final int MS_TO_SEC = 1000;
    public static final int TICK_MS = MS_TO_SEC / 20;
    public static final int TICK_PER_SECOND = MS_TO_SEC / TICK_MS;

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

    private static UpdateManager updateManager;
    private static MinecraftServer minecraftServer;

    // Data
    private static ResponseDataConsumer responseDataConsumer;
    private static Difficulty difficulty = Difficulty.NORMAL;

    public static MinecraftServer init() {
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

        updateManager = new UpdateManager();

        nettyServer = new NettyServer(packetProcessor);

        // Registry
        try {
            ResourceGatherer.ensureResourcesArePresent(null); // TODO: provide a way to give a path override, probably via launch arguments?
        } catch (IOException e) {
            System.err.println("An error happened during resource gathering. Minestom will attempt to load anyway, but things may not work, and crashes can happen.");
            e.printStackTrace();
        }
        RegistryMain.registerBlocks();
        RegistryMain.registerItems();
        RegistryMain.registerEntities();
        RegistryMain.registerSounds();
        RegistryMain.registerParticles();
        RegistryMain.registerStats();
        RegistryMain.registerEnchantments();
        RegistryMain.registerPotions();

        minecraftServer = new MinecraftServer();

        return minecraftServer;
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

    public void start(String address, int port, ResponseDataConsumer responseDataConsumer) {
        MinecraftServer.responseDataConsumer = responseDataConsumer;
        updateManager.start();
        nettyServer.start(address, port);
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public static void stopCleanly() {
        updateManager.stop();
        nettyServer.stop();
        schedulerManager.shutdown();
        storageManager.getLoadedFolders().forEach(storageFolder -> storageFolder.close());
    }

}
