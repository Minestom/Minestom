package fr.themode.minestom;

import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.server.Connection;
import fr.adamaq01.ozao.net.server.Server;
import fr.adamaq01.ozao.net.server.ServerHandler;
import fr.adamaq01.ozao.net.server.backend.tcp.TCPServer;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.protocol.MinecraftProtocol;

import java.lang.reflect.InvocationTargetException;

public class Main {

    private static ConnectionManager connectionManager;
    private static PacketProcessor packetProcessor;

    public static void main(String[] args) {

        connectionManager = new ConnectionManager();
        packetProcessor = new PacketProcessor(connectionManager);

        Server server = new TCPServer(new MinecraftProtocol()).addHandler(new ServerHandler() {
            @Override
            public void onConnect(Server server, Connection connection) {
                System.out.println("A connection");
            }

            @Override
            public void onDisconnect(Server server, Connection connection) {
                System.out.println("A DISCONNECTION");
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

    }

}
