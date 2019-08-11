package fr.themode.minestom;

import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.server.Connection;
import fr.adamaq01.ozao.net.server.Server;
import fr.adamaq01.ozao.net.server.ServerHandler;
import fr.adamaq01.ozao.net.server.backend.tcp.TCPServer;
import fr.themode.minestom.entity.EntityManager;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.packet.server.play.DestroyEntitiesPacket;
import fr.themode.minestom.net.packet.server.play.KeepAlivePacket;
import fr.themode.minestom.net.packet.server.play.PlayerInfoPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.net.protocol.MinecraftProtocol;

import java.lang.reflect.InvocationTargetException;

public class Main {

    // In-Game Manager
    private static EntityManager entityManager;

    // Others
    private static ConnectionManager connectionManager;
    private static PacketProcessor packetProcessor;
    private static Server server;

    public static void main(String[] args) {
        entityManager = new EntityManager();

        connectionManager = new ConnectionManager();
        packetProcessor = new PacketProcessor(connectionManager);

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
                        connectionManager.removePlayer(packetProcessor.getPlayerConnection(connection));

                        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);
                        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.RemovePlayer(player.getUuid()));
                        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
                        destroyEntitiesPacket.entityIds = new int[] {player.getEntityId()};
                        for (Player onlinePlayer : connectionManager.getOnlinePlayers()) {
                            if (!onlinePlayer.equals(player)) {
                                onlinePlayer.getPlayerConnection().sendPacket(destroyEntitiesPacket);
                                onlinePlayer.getPlayerConnection().sendPacket(playerInfoPacket);
                            }
                        }
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

        long lastTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - lastTime >= 50) {
                lastTime = System.currentTimeMillis();
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
            }
        }
    }

    public static Server getServer() {
        return server;
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
