package fr.themode.minestom;

import com.github.simplenet.Server;
import fr.themode.minestom.benchmark.BenchmarkManager;
import fr.themode.minestom.command.CommandManager;
import fr.themode.minestom.data.DataManager;
import fr.themode.minestom.entity.EntityManager;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.listener.manager.PacketListenerManager;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionUtils;
import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.status.LegacyServerListPingPacket;
import fr.themode.minestom.net.packet.server.play.KeepAlivePacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.recipe.RecipeManager;
import fr.themode.minestom.registry.RegistryMain;
import fr.themode.minestom.scoreboard.TeamManager;
import fr.themode.minestom.timer.SchedulerManager;
import fr.themode.minestom.utils.Utils;

public class MinecraftServer {

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_PACKET_WRITER = "Ms-PacketWriterPool";
    public static final int THREAD_COUNT_PACKET_WRITER = 2;

    public static final String THREAD_NAME_IO = "Ms-IOPool";
    public static final int THREAD_COUNT_IO = 2;

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
    public static final int CHUNK_VIEW_DISTANCE = 5;
    public static final int ENTITY_VIEW_DISTANCE = 2;
    // Can be modified at performance cost when decreased
    private static final int MS_TO_SEC = 1000;
    public static final int TICK_MS = MS_TO_SEC / 20;
    public static final int TICK_PER_SECOND = MS_TO_SEC / TICK_MS;

    // Networking
    private static ConnectionManager connectionManager;
    private static PacketProcessor packetProcessor;
    private static PacketListenerManager packetListenerManager;
    private static Server server;

    // In-Game Manager
    private static InstanceManager instanceManager;
    private static BlockManager blockManager;
    private static EntityManager entityManager;
    private static CommandManager commandManager;
    private static RecipeManager recipeManager;
    private static DataManager dataManager;
    private static TeamManager teamManager;
    private static SchedulerManager schedulerManager;
    private static BenchmarkManager benchmarkManager;

    private static MinecraftServer minecraftServer;

    public static MinecraftServer init() {
        connectionManager = new ConnectionManager();
        packetProcessor = new PacketProcessor();
        packetListenerManager = new PacketListenerManager();

        instanceManager = new InstanceManager();
        blockManager = new BlockManager();
        entityManager = new EntityManager();
        commandManager = new CommandManager();
        recipeManager = new RecipeManager();
        dataManager = new DataManager();
        teamManager = new TeamManager();
        schedulerManager = new SchedulerManager();
        benchmarkManager = new BenchmarkManager();

        server = new Server();

        // Registry
        RegistryMain.registerBlocks();
        RegistryMain.registerItems();
        RegistryMain.registerEntities();
        RegistryMain.registerSounds();
        RegistryMain.registerParticles();

        minecraftServer = new MinecraftServer();

        return minecraftServer;
    }

    public static PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    public static Server getServer() {
        return server;
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

    public void start(String address, int port) {
        initClients();
        server.bind(address, port);
        initUpdates();
    }

    private void initClients() {
        server.onConnect(client -> {
            System.out.println("CONNECTION");

            client.preDisconnect(() -> {
                System.out.println("DISCONNECTION");
                PlayerConnection playerConnection = packetProcessor.getPlayerConnection(client);
                if (playerConnection != null) {
                    playerConnection.refreshOnline(false);
                    Player player = connectionManager.getPlayer(playerConnection);
                    if (player != null) {
                        player.remove();

                        connectionManager.removePlayer(playerConnection);
                    }
                    packetProcessor.removePlayerConnection(client);
                }
            });

            ConnectionUtils.readVarIntAlways(client, length -> {
                if (length == 0xFE) { // Legacy server ping
                    LegacyServerListPingPacket legacyServerListPingPacket = new LegacyServerListPingPacket();
                    legacyServerListPingPacket.read(new PacketReader(client, 0, 0), () -> {
                        legacyServerListPingPacket.process(null, null);
                    });
                } else {
                    final int varIntLength = Utils.lengthVarInt(length);
                    ConnectionUtils.readVarInt(client, packetId -> {
                        int offset = varIntLength + Utils.lengthVarInt(packetId);
                        packetProcessor.process(client, packetId, length, offset);
                    });
                }
            });
        });
    }

    private void initUpdates() {
        final long tickDistance = TICK_MS * 1000000;
        long currentTime;
        while (true) {
            currentTime = System.nanoTime();

            // Keep Alive Handling
            for (Player player : getConnectionManager().getOnlinePlayers()) {
                long time = currentTime / 1_000_000;
                if (time - player.getLastKeepAlive() > 20000) {
                    player.refreshKeepAlive(time);
                    KeepAlivePacket keepAlivePacket = new KeepAlivePacket(time);
                    player.getPlayerConnection().sendPacket(keepAlivePacket);
                }
            }

            // Entities update
            entityManager.update();

            // Blocks update
            instanceManager.updateBlocks();

            // Scheduler
            schedulerManager.update();

            // TODO miscellaneous update (scoreboard)

            // Sleep until next tick
            long sleepTime = (tickDistance - (System.nanoTime() - currentTime)) / 1000000;
            sleepTime = Math.max(1, sleepTime);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
