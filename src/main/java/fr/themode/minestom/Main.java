package fr.themode.minestom;

import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.server.Connection;
import fr.adamaq01.ozao.net.server.Server;
import fr.adamaq01.ozao.net.server.ServerHandler;
import fr.adamaq01.ozao.net.server.backend.tcp.TCPServer;
import fr.themode.minestom.entity.EntityManager;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.BlockManager;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.instance.demo.StoneBlock;
import fr.themode.minestom.listener.PacketListenerManager;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.packet.server.play.KeepAlivePacket;
import fr.themode.minestom.net.protocol.MinecraftProtocol;

import java.lang.reflect.InvocationTargetException;

public class Main {

    public static final int TICK_MS = 50;

    // Networking
    private static ConnectionManager connectionManager;
    private static PacketProcessor packetProcessor;
    private static PacketListenerManager packetListenerManager;
    private static Server server;

    // In-Game Manager
    private static InstanceManager instanceManager;
    private static BlockManager blockManager;
    private static EntityManager entityManager;

    public static void main(String[] args) {
        connectionManager = new ConnectionManager();
        packetProcessor = new PacketProcessor();
        packetListenerManager = new PacketListenerManager();

        instanceManager = new InstanceManager();
        blockManager = new BlockManager();
        entityManager = new EntityManager();

        blockManager.registerBlock(StoneBlock::new);

        server = new TCPServer(new MinecraftProtocol()).addHandler(new ServerHandler() {
            @Override
            public void onConnect(Server server, Connection connection) {
                System.out.println("A connection");
            }

            @Override
            public void onDisconnect(Server server, Connection connection) {
                System.out.println("A DISCONNECTION");
                if (packetProcessor.hasPlayerConnection(connection)) {
                    if (connectionManager.getPlayer(packetProcessor.getPlayerConnection(connection)) != null) {
                        Player player = connectionManager.getPlayer(packetProcessor.getPlayerConnection(connection));
                        player.remove();

                        Instance instance = player.getInstance();
                        if (instance != null) {
                            instance.removeEntity(player);
                        }

                        connectionManager.removePlayer(packetProcessor.getPlayerConnection(connection));
                    }
                    packetProcessor.removePlayerConnection(connection);
                }
            }

            @Override
            public void onPacketReceive(Server server, Connection connection, Packet packet) {
                try {
                    packetProcessor.process(connection, packet);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onException(Server server, Connection connection, Throwable cause) {
                cause.printStackTrace();
            }
        });

        server.bind(25565);
        System.out.println("Server started");

        long tickDistance = TICK_MS * 1000000;
        long nextTick = System.nanoTime();
        long currentTime;
        while (true) {
            currentTime = System.nanoTime();
            if (currentTime >= nextTick) {
                // Tick

                // Keep Alive Handling
                server.getConnections().stream().filter(connection -> packetProcessor.hasPlayerConnection(connection) && connectionManager.getPlayer(packetProcessor.getPlayerConnection(connection)) != null && System.currentTimeMillis() - connectionManager.getPlayer(packetProcessor.getPlayerConnection(connection)).getLastKeepAlive() > 20000).map(connection -> connectionManager.getPlayer(packetProcessor.getPlayerConnection(connection))).forEach(player -> {
                    long id = System.currentTimeMillis();
                    player.refreshKeepAlive(id);
                    KeepAlivePacket keepAlivePacket = new KeepAlivePacket(id);
                    player.getPlayerConnection().sendPacket(keepAlivePacket);
                });

                // Entities update
                entityManager.update();

                // Set next tick update time
                currentTime = System.nanoTime();
                nextTick = currentTime + tickDistance - (currentTime - nextTick);
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

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
