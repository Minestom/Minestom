package net.minestom.server.network;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.RichMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.listener.manager.PacketConsumer;
import net.minestom.server.network.packet.client.login.LoginStartPacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ConnectionManager {

    private Set<Player> players = new CopyOnWriteArraySet<>();
    private Map<PlayerConnection, Player> connectionPlayerMap = Collections.synchronizedMap(new HashMap<>());

    // All the consumers to call once a packet is received
    private List<PacketConsumer> receivePacketConsumers = new CopyOnWriteArrayList<>();
    // The uuid provider once a player login
    private UuidProvider uuidProvider;
    // The player provider to have your own Player implementation
    private PlayerProvider playerProvider;
    // The consumers to call once a player connect, mostly used to init events
    private List<Consumer<Player>> playerInitializations = new CopyOnWriteArrayList<>();

    /**
     * Get the {@link Player} linked to a {@link PlayerConnection}
     *
     * @param connection the player connection
     * @return the player linked to the connection
     */
    public Player getPlayer(PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    /**
     * Get all online players
     *
     * @return an unmodifiable collection containing all the online players
     */
    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(players);
    }

    /**
     * Get the first player which validate {@link String#equalsIgnoreCase(String)}
     * <p>
     * This can cause issue if two or more players have the same username
     *
     * @param username the player username (ignoreCase)
     * @return the first player who validate the username condition
     */
    public Player getPlayer(String username) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(username))
                return player;
        }
        return null;
    }

    /**
     * Get the first player which validate {@link UUID#equals(Object)}
     * <p>
     * This can cause issue if two or more players have the same UUID
     *
     * @param uuid the player UUID
     * @return the first player who validate the UUID condition
     */
    public Player getPlayer(UUID uuid) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUuid().equals(uuid))
                return player;
        }
        return null;
    }

    /**
     * Send a rich message to all online players who validate the condition {@code condition}
     *
     * @param richMessage the rich message to send
     * @param condition   the condition to receive the message
     */
    public void broadcastMessage(RichMessage richMessage, Function<Player, Boolean> condition) {
        final Collection<Player> recipients = getRecipients(condition);

        if (!recipients.isEmpty()) {
            final String jsonText = richMessage.toString();
            broadcastJson(jsonText, recipients);
        }
    }

    /**
     * Send a rich message to all online players without exception
     *
     * @param richMessage the rich message to send
     */
    public void broadcastMessage(RichMessage richMessage) {
        broadcastMessage(richMessage, null);
    }

    /**
     * Send a message to all online players who validate the condition {@code condition}
     *
     * @param coloredText the message to send
     * @param condition   the condition to receive the message
     */
    public void broadcastMessage(ColoredText coloredText, Function<Player, Boolean> condition) {
        final Collection<Player> recipients = getRecipients(condition);

        if (!recipients.isEmpty()) {
            final String jsonText = coloredText.toString();
            broadcastJson(jsonText, recipients);
        }
    }

    /**
     * Send a message to all online players without exception
     *
     * @param coloredText the message to send
     */
    public void broadcastMessage(ColoredText coloredText) {
        broadcastMessage(coloredText, null);
    }

    private void broadcastJson(String json, Collection<Player> recipients) {
        ChatMessagePacket chatMessagePacket =
                new ChatMessagePacket(json, ChatMessagePacket.Position.SYSTEM_MESSAGE);
        PacketWriterUtils.writeAndSend(recipients, chatMessagePacket);
    }

    private Collection<Player> getRecipients(Function<Player, Boolean> condition) {
        Collection<Player> recipients;

        // Get the recipients
        if (condition == null) {
            recipients = getOnlinePlayers();
        } else {
            recipients = new ArrayList<>();
            getOnlinePlayers().forEach(player -> {
                final boolean result = condition.apply(player);
                if (result)
                    recipients.add(player);
            });
        }

        return recipients;
    }

    /**
     * Get all the listeners which are called for each packet received
     *
     * @return an unmodifiable list of packet's consumers
     */
    public List<PacketConsumer> getReceivePacketConsumers() {
        return Collections.unmodifiableList(receivePacketConsumers);
    }

    /**
     * Add a consumer to call once a packet is received
     *
     * @param packetConsumer the packet consumer
     */
    public void onPacketReceive(PacketConsumer packetConsumer) {
        this.receivePacketConsumers.add(packetConsumer);
    }

    /**
     * Shouldn't be override if already defined
     *
     * @param uuidProvider the new player connection uuid provider
     * @see #getPlayerConnectionUuid(PlayerConnection, String)
     */
    public void setUuidProvider(UuidProvider uuidProvider) {
        this.uuidProvider = uuidProvider;
    }

    /**
     * Compute the UUID of the specified connection
     * Used in {@link net.minestom.server.network.packet.client.login.LoginStartPacket} in order
     * to give the player the right UUID
     *
     * @param playerConnection the player connection
     * @return the uuid based on {@code playerConnection}
     * return a random UUID if no UUID provider is defined see {@link #setUuidProvider(UuidProvider)}
     */
    public UUID getPlayerConnectionUuid(PlayerConnection playerConnection, String username) {
        if (uuidProvider == null)
            return UUID.randomUUID();
        return uuidProvider.provide(playerConnection, username);
    }

    /**
     * Change the {@link Player} provider, to change which object to link to him
     *
     * @param playerProvider the new {@link PlayerProvider}, can be set to null to apply the default provider
     */
    public void setPlayerProvider(PlayerProvider playerProvider) {
        this.playerProvider = playerProvider;
    }

    /**
     * Retrieve the current {@link PlayerProvider}, can be the default one if none is defined
     *
     * @return the current {@link PlayerProvider}
     */
    public PlayerProvider getPlayerProvider() {
        return playerProvider == null ? playerProvider = Player::new : playerProvider;
    }

    /**
     * Those are all the consumers called when a new player join
     *
     * @return an unmodifiable list containing all the player initialization consumer
     */
    public List<Consumer<Player>> getPlayerInitializations() {
        return Collections.unmodifiableList(playerInitializations);
    }

    /**
     * Add a new player initialization consumer. Those are called when a player join,
     * mainly to add event callbacks to the player
     *
     * @param playerInitialization the player initialization consumer
     */
    public void addPlayerInitialization(Consumer<Player> playerInitialization) {
        this.playerInitializations.add(playerInitialization);
    }

    /**
     * Add a new player in the players list
     * Is currently used at
     * {@link LoginStartPacket#process(PlayerConnection)}
     * and in {@link FakePlayer#initPlayer(UUID, String, Consumer)}
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
        final Player player = getPlayerProvider().createPlayer(uuid, username, connection);
        createPlayer(player);
    }

    /**
     * Remove a player from the players list
     * used at player disconnection
     *
     * @param connection the player connection
     */
    public void removePlayer(PlayerConnection connection) {
        final Player player = this.connectionPlayerMap.get(connection);
        if (player == null)
            return;

        this.players.remove(player);
        this.connectionPlayerMap.remove(connection);
    }
}
