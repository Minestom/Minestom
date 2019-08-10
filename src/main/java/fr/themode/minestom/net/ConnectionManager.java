package fr.themode.minestom.net;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.*;

public class ConnectionManager {

    private Set<PlayerConnection> connections = new HashSet<>();
    private Map<PlayerConnection, Player> connectionPlayerMap = new HashMap<>();

    public Player getPlayer(PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(connectionPlayerMap.values());
    }

    // Is only used at LoginStartPacket#process
    public void createPlayer(PlayerConnection connection) {
        this.connectionPlayerMap.put(connection, new Player(connection));
    }
}
