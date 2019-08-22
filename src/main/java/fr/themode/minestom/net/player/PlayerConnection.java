package fr.themode.minestom.net.player;

import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.server.Connection;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.PacketUtils;

public class PlayerConnection {

    private Connection connection;
    private ConnectionState connectionState;

    public PlayerConnection(Connection connection) {
        this.connection = connection;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    public void sendPacket(Packet packet) {
        this.connection.sendPacket(packet);
    }

    public void sendPacket(ServerPacket serverPacket) {
        sendPacket(PacketUtils.writePacket(serverPacket));
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
