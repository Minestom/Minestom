package fr.themode.minestom.net.player;

import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.PacketUtils;
import simplenet.Client;
import simplenet.packet.Packet;

public class PlayerConnection {

    private Client client;
    private ConnectionState connectionState;

    public PlayerConnection(Client client) {
        this.client = client;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    public void sendPacket(Packet packet) {
        packet.writeAndFlush(client);
    }

    public void writeUnencodedPacket(Packet packet) {
        packet.write(client);
    }

    public void sendPacket(ServerPacket serverPacket) {
        PacketUtils.writePacket(serverPacket, packet -> sendPacket(packet));
        //PacketWriterUtils.writeAndSend(this, serverPacket);
    }

    public void flush() {
        client.flush();
    }

    public Client getClient() {
        return client;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
