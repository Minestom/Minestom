package net.minestom.server.network;

import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketConsumer;
import net.minestom.server.network.player.PlayerConnection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectionManager {

    private Set<Player> players = new CopyOnWriteArraySet<>();
    private Map<PlayerConnection, Player> connectionPlayerMap = Collections.synchronizedMap(new HashMap<>());

    private List<PacketConsumer> packetConsumers = new CopyOnWriteArrayList<>();
    private List<Consumer<Player>> playerInitializations = new CopyOnWriteArrayList<>();

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

    public void broadcastMessage(String message, Function<Player, Boolean> condition) {
        if (condition == null) {
            getOnlinePlayers().forEach(player -> player.sendMessage(message));
        } else {
            getOnlinePlayers().forEach(player -> {
                boolean result = condition.apply(player);
                if (result)
                    player.sendMessage(message);
            });
        }
    }

    public void broadcastMessage(String message) {
        broadcastMessage(message, null);
    }

    public List<PacketConsumer> getPacketConsumers() {
        return Collections.unmodifiableList(packetConsumers);
    }

    public void addPacketConsumer(PacketConsumer packetConsumer) {
        this.packetConsumers.add(packetConsumer);
    }

    public List<Consumer<Player>> getPlayerInitializations() {
        return Collections.unmodifiableList(playerInitializations);
    }

    public void addPlayerInitialization(Consumer<Player> playerInitialization) {
        this.playerInitializations.add(playerInitialization);
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
