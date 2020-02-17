package fr.themode.minestom.net;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class ConnectionManager {

    private Set<Player> players = new CopyOnWriteArraySet<>();
    private Map<PlayerConnection, Player> connectionPlayerMap = Collections.synchronizedMap(new HashMap<>());

    private Consumer<Player> playerInitialization;

    public Player getPlayer(PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(players);
    }

    public Player getPlayer(String username) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(username))
                return player;
        }
        return null;
    }

    public Consumer<Player> getPlayerInitialization() {
        return playerInitialization;
    }

    public void setPlayerInitialization(Consumer<Player> playerInitialization) {
        this.playerInitialization = playerInitialization;
    }

    // Is only used at LoginStartPacket#process
    public void createPlayer(UUID uuid, String username, PlayerConnection connection) {
        Player player = new Player(uuid, username, connection);
        this.players.add(player);
        this.connectionPlayerMap.put(connection, player);
    }

    public void removePlayer(PlayerConnection connection) {
        Player player = this.connectionPlayerMap.get(connection);
        this.players.remove(player);
        this.connectionPlayerMap.remove(player);
    }
}
