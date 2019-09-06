package fr.themode.minestom.net.player;

import com.github.simplenet.Client;
import com.github.simplenet.packet.Packet;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.PacketUtils;

public class PlayerConnection {

    private Client client;
    private ConnectionState connectionState;
    private boolean online;

    public PlayerConnection(Client client) {
        this.client = client;
        this.connectionState = ConnectionState.UNKNOWN;
        this.online = true;
    }

    public void sendPacket(Packet packet) {
        if (isOnline())
            packet.queueAndFlush(client);
    }

    public void writeUnencodedPacket(Packet packet) {
        packet.queue(client);
    }

    public void sendPacket(ServerPacket serverPacket) {
        if (isOnline())
            PacketUtils.writePacket(serverPacket, packet -> sendPacket(packet));
    }

    public void flush() {
        client.flush();
    }

    public Client getClient() {
        return client;
    }

    public boolean isOnline() {
        return online;
    }

    public void refreshOnline(boolean online) {
        this.online = online;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
