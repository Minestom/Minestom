package fr.themode.minestom;

import com.github.simplenet.Server;
import fr.themode.minestom.data.DataManager;
import fr.themode.minestom.entity.EntityManager;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.demo.StoneBlock;
import fr.themode.minestom.listener.PacketListenerManager;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionUtils;
import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.status.LegacyServerListPingPacket;
import fr.themode.minestom.net.packet.server.play.KeepAlivePacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Utils;

public class Main {

    // Thread number
    public static final int THREAD_COUNT_PACKET_WRITER = 2;
    public static final int THREAD_COUNT_IO = 2;
    public static final int THREAD_COUNT_BLOCK_BATCH = 2;
    public static final int THREAD_COUNT_ENTITIES = 2;
    public static final int THREAD_COUNT_PLAYERS_ENTITIES = 2;

    public static final int TICK_MS = 50;
    public static final int TICK_PER_SECOND = 1000 / TICK_MS;

    // Config
    public static final int CHUNK_VIEW_DISTANCE = 5;
    public static final int ENTITY_VIEW_DISTANCE = 2;

    // Networking
    private static ConnectionManager connectionManager;
    private static PacketProcessor packetProcessor;
    private static PacketListenerManager packetListenerManager;
    private static Server server;

    // In-Game Manager
    private static InstanceManager instanceManager;
    private static BlockManager blockManager;
    private static EntityManager entityManager;
    private static DataManager dataManager;

    public static void main(String[] args) {
        connectionManager = new ConnectionManager();
        packetProcessor = new PacketProcessor();
        packetListenerManager = new PacketListenerManager();

        instanceManager = new InstanceManager();
        blockManager = new BlockManager();
        entityManager = new EntityManager();
        dataManager = new DataManager();

        blockManager.registerBlock(StoneBlock::new);

        server = new Server(136434);

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

        server.bind("localhost", 25565);
        System.out.println("Server started");

        long tickDistance = TICK_MS * 1000000;
        long currentTime;
        while (true) {
            currentTime = System.nanoTime();

            // Keep Alive Handling
            for (Player player : getConnectionManager().getOnlinePlayers()) {
                long time = System.currentTimeMillis();
                if (time - player.getLastKeepAlive() > 20000) {
                    player.refreshKeepAlive(time);
                    KeepAlivePacket keepAlivePacket = new KeepAlivePacket(time);
                    player.getPlayerConnection().sendPacket(keepAlivePacket);
                }
            }

            // Entities update
            entityManager.update();

            // Blocks update
            blockManager.update();

            // TODO miscellaneous update (scoreboard)

            // Sleep until next tick
            long sleepTime = (tickDistance - (System.nanoTime() - currentTime)) / 1000000;
            sleepTime = Math.max(1, sleepTime);

            //String perfMessage = "Online: " + getConnectionManager().getOnlinePlayers().size() + " Tick time: " + (TICK_MS - sleepTime) + " ms";
            //getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(perfMessage));

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
