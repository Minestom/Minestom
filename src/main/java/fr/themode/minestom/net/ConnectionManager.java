package fr.themode.minestom.net;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.*;

public class ConnectionManager {

    private volatile Set<Player> players = new HashSet<>();
    private volatile Map<PlayerConnection, Player> connectionPlayerMap = new HashMap<>();

    public Player getPlayer(PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(players);
    }

    // Is only used at LoginStartPacket#process
    public void createPlayer(PlayerConnection connection) {
        Player player = new Player(connection);
        this.players.add(player);
        this.connectionPlayerMap.put(connection, player);
    }
}
