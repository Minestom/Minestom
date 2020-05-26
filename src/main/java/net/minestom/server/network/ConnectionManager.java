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
    private UuidProvider uuidProvider;
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

    /**
     * Shouldn't be override if not already defined
     *
     * @param uuidProvider the new player connection uuid provider
     */
    public void setUuidProvider(UuidProvider uuidProvider) {
        this.uuidProvider = uuidProvider;
    }

    /**
     * Compute the UUID of the specified connection
     * Used in {@link net.minestom.server.network.packet.client.login.LoginStartPacket} in order
     * to give the player the right UUID
     *
     * @param playerConnection
     * @return
     */
    public UUID getPlayerConnectionUuid(PlayerConnection playerConnection) {
        if (uuidProvider == null)
            return UUID.randomUUID();
        return uuidProvider.provide(playerConnection);
    }

    public List<Consumer<Player>> getPlayerInitializations() {
        return Collections.unmodifiableList(playerInitializations);
    }

    public void addPlayerInitialization(Consumer<Player> playerInitialization) {
        this.playerInitializations.add(playerInitialization);
    }

    /**
     * Add a new player in the players list
     * Is currently used at
     * {@link net.minestom.server.network.packet.client.login.LoginStartPacket#process(PlayerConnection, ConnectionManager)}
     * and in {@link net.minestom.server.entity.fakeplayer.FakePlayer#FakePlayer(UUID, String, boolean)}
     *
     * @param player the player to add
     */
    public void createPlayer(Player player) {
        this.players.add(player);
        this.connectionPlayerMap.put(player.getPlayerConnection(), player);
    }

    /**
     * Create a player object and register it
     *
     * @param uuid       the new player uuid
     * @param username   the new player username
     * @param connection the new player connection
     */
    public void createPlayer(UUID uuid, String username, PlayerConnection connection) {
        Player player = new Player(uuid, username, connection);
        createPlayer(player);
    }

    /**
     * Remove a player from the players list
     * used at player disconnection
     *
     * @param connection the player connection
     */
    public void removePlayer(PlayerConnection connection) {
        Player player = this.connectionPlayerMap.get(connection);
        if (player == null)
            return;

        this.players.remove(player);
        this.connectionPlayerMap.remove(player);
    }
}
