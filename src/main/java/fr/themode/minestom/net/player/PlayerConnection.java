package fr.themode.minestom.net.player;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.server.Connection;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.server.ServerPacket;

import static fr.themode.minestom.net.protocol.MinecraftProtocol.PACKET_ID_IDENTIFIER;

public class PlayerConnection {

    private Connection connection;
    private ConnectionState connectionState;

    public PlayerConnection(Connection connection) {
        this.connection = connection;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    public void sendPacket(ServerPacket serverPacket) {
        Packet packet = Packet.create();
        Buffer buffer = packet.getPayload();
        serverPacket.write(buffer);
        packet.put(PACKET_ID_IDENTIFIER, serverPacket.getId());

        this.connection.sendPacket(packet);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
