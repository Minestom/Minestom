package fr.themode.minestom.net;

import com.github.simplenet.Client;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.client.handler.ClientLoginPacketsHandler;
import fr.themode.minestom.net.packet.client.handler.ClientPlayPacketsHandler;
import fr.themode.minestom.net.packet.client.handler.ClientStatusPacketsHandler;
import fr.themode.minestom.net.packet.client.handshake.HandshakePacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketProcessor {

    private Map<Client, PlayerConnection> connectionPlayerConnectionMap = new ConcurrentHashMap<>();

    private ConnectionManager connectionManager;

    // Protocols
    private ClientStatusPacketsHandler statusPacketsHandler;
    private ClientLoginPacketsHandler loginPacketsHandler;
    private ClientPlayPacketsHandler playPacketsHandler;

    public PacketProcessor() {
        this.connectionManager = Main.getConnectionManager();

        this.statusPacketsHandler = new ClientStatusPacketsHandler();
        this.loginPacketsHandler = new ClientLoginPacketsHandler();
        this.playPacketsHandler = new ClientPlayPacketsHandler();
    }

    private List<Integer> printBlackList = Arrays.asList(17, 18, 19);

    public void process(Client client, int id, int length, int offset) {
        PlayerConnection playerConnection = connectionPlayerConnectionMap.computeIfAbsent(client, c -> new PlayerConnection(client));
        ConnectionState connectionState = playerConnection.getConnectionState();
        /*if (!printBlackList.contains(id)) {
            System.out.println("RECEIVED ID: 0x" + Integer.toHexString(id) + " State: " + connectionState);
        }*/

        PacketReader packetReader = new PacketReader(client, length, offset);

        if (connectionState == ConnectionState.UNKNOWN) {
            // Should be handshake packet
            if (id == 0) {
                HandshakePacket handshakePacket = new HandshakePacket();
                handshakePacket.read(packetReader, () -> handshakePacket.process(playerConnection, connectionManager));
            }
            return;
        }

        switch (connectionState) {
            case PLAY:
                Player player = connectionManager.getPlayer(playerConnection);
                ClientPlayPacket playPacket = (ClientPlayPacket) playPacketsHandler.getPacketInstance(id);
                playPacket.read(packetReader, () -> player.addPacketToQueue(playPacket));
                break;
            case LOGIN:
                ClientPreplayPacket loginPacket = (ClientPreplayPacket) loginPacketsHandler.getPacketInstance(id);
                loginPacket.read(packetReader, () -> loginPacket.process(playerConnection, connectionManager));
                break;
            case STATUS:
                ClientPreplayPacket statusPacket = (ClientPreplayPacket) statusPacketsHandler.getPacketInstance(id);
                statusPacket.read(packetReader, () -> statusPacket.process(playerConnection, connectionManager));
                break;
            case UNKNOWN:
                // Ignore packet (unexpected)
                break;
        }
    }

    public PlayerConnection getPlayerConnection(Client client) {
        return connectionPlayerConnectionMap.get(client);
    }

    public boolean hasPlayerConnection(Client client) {
        return connectionPlayerConnectionMap.containsKey(client);
    }

    public void removePlayerConnection(Client client) {
        connectionPlayerConnectionMap.remove(client);
    }
}
