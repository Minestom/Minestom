package net.minestom.server.network;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.listener.manager.PacketConsumer;
import net.minestom.server.network.packet.client.login.LoginStartPacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages the connected clients.
 */
public final class ConnectionManager {

    private final Set<Player> players = new CopyOnWriteArraySet<>();
    private final Map<PlayerConnection, Player> connectionPlayerMap = Collections.synchronizedMap(new HashMap<>());

    // All the consumers to call once a packet is received
    private final List<PacketConsumer> receivePacketConsumers = new CopyOnWriteArrayList<>();
    // The uuid provider once a player login
    private UuidProvider uuidProvider;
    // The player provider to have your own Player implementation
    private PlayerProvider playerProvider;
    // The consumers to call once a player connect, mostly used to init events
    private final List<Consumer<Player>> playerInitializations = new CopyOnWriteArrayList<>();

    /**
     * Gets the {@link Player} linked to a {@link PlayerConnection}.
     *
     * @param connection the player connection
     * @return the player linked to the connection
     */
    public Player getPlayer(@NotNull PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    /**
     * Gets all online players.
     *
     * @return an unmodifiable collection containing all the online players
     */
    @NotNull
    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(players);
    }

    /**
     * Gets the first player which validate {@link String#equalsIgnoreCase(String)}.
     * <p>
     * This can cause issue if two or more players have the same username.
     *
     * @param username the player username (ignoreCase)
     * @return the first player who validate the username condition, null if none was found
     */
    @Nullable
    public Player getPlayer(@NotNull String username) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(username))
                return player;
        }
        return null;
    }

    /**
     * Gets the first player which validate {@link UUID#equals(Object)}.
     * <p>
     * This can cause issue if two or more players have the same UUID.
     *
     * @param uuid the player UUID
     * @return the first player who validate the UUID condition, null if none was found
     */
    @Nullable
    public Player getPlayer(@NotNull UUID uuid) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUuid().equals(uuid))
                return player;
        }
        return null;
    }

    /**
     * Sends a {@link JsonMessage} to all online players who validate the condition {@code condition}.
     *
     * @param jsonMessage the message to send, probably a {@link net.minestom.server.chat.ColoredText} or {@link net.minestom.server.chat.RichMessage}
     * @param condition   the condition to receive the message
     */
    public void broadcastMessage(@NotNull JsonMessage jsonMessage, @Nullable Function<Player, Boolean> condition) {
        final Collection<Player> recipients = getRecipients(condition);

        if (!recipients.isEmpty()) {
            final String jsonText = jsonMessage.toString();
            broadcastJson(jsonText, recipients);
        }
    }

    /**
     * Sends a {@link JsonMessage} to all online players.
     *
     * @param jsonMessage the message to send, probably a {@link net.minestom.server.chat.ColoredText} or {@link net.minestom.server.chat.RichMessage}
     */
    public void broadcastMessage(@NotNull JsonMessage jsonMessage) {
        broadcastMessage(jsonMessage, null);
    }

    private void broadcastJson(@NotNull String json, @NotNull Collection<Player> recipients) {
        ChatMessagePacket chatMessagePacket =
                new ChatMessagePacket(json, ChatMessagePacket.Position.SYSTEM_MESSAGE);
        PacketWriterUtils.writeAndSend(recipients, chatMessagePacket);
    }

    private Collection<Player> getRecipients(@Nullable Function<Player, Boolean> condition) {
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
     * Gets all the listeners which are called for each packet received.
     *
     * @return an unmodifiable list of packet's consumers
     */
    @NotNull
    public List<PacketConsumer> getReceivePacketConsumers() {
        return Collections.unmodifiableList(receivePacketConsumers);
    }

    /**
     * Adds a consumer to call once a packet is received.
     *
     * @param packetConsumer the packet consumer
     */
    public void onPacketReceive(@NotNull PacketConsumer packetConsumer) {
        this.receivePacketConsumers.add(packetConsumer);
    }

    /**
     * Changes how {@link UUID} are attributed to players.
     * <p>
     * Shouldn't be override if already defined.
     *
     * @param uuidProvider the new player connection uuid provider,
     *                     setting it to null would apply a random UUID for each player connection
     * @see #getPlayerConnectionUuid(PlayerConnection, String)
     */
    public void setUuidProvider(@Nullable UuidProvider uuidProvider) {
        this.uuidProvider = uuidProvider;
    }

    /**
     * Computes the UUID of the specified connection.
     * Used in {@link net.minestom.server.network.packet.client.login.LoginStartPacket} in order
     * to give the player the right {@link UUID}.
     *
     * @param playerConnection the player connection
     * @param username         the username given by the connection
     * @return the uuid based on {@code playerConnection}
     * return a random UUID if no UUID provider is defined see {@link #setUuidProvider(UuidProvider)}
     */
    @NotNull
    public UUID getPlayerConnectionUuid(@NotNull PlayerConnection playerConnection, @NotNull String username) {
        if (uuidProvider == null)
            return UUID.randomUUID();
        return uuidProvider.provide(playerConnection, username);
    }

    /**
     * Changes the {@link Player} provider, to change which object to link to him.
     *
     * @param playerProvider the new {@link PlayerProvider}, can be set to null to apply the default provider
     */
    public void setPlayerProvider(@Nullable PlayerProvider playerProvider) {
        this.playerProvider = playerProvider;
    }

    /**
     * Retrieves the current {@link PlayerProvider}, can be the default one if none is defined.
     *
     * @return the current {@link PlayerProvider}
     */
    @NotNull
    public PlayerProvider getPlayerProvider() {
        return playerProvider == null ? playerProvider = Player::new : playerProvider;
    }

    /**
     * Those are all the consumers called when a new {@link Player} join.
     *
     * @return an unmodifiable list containing all the {@link Player} initialization consumer
     */
    @NotNull
    public List<Consumer<Player>> getPlayerInitializations() {
        return Collections.unmodifiableList(playerInitializations);
    }

    /**
     * Adds a new player initialization consumer. Those are called when a {@link Player} join,
     * mainly to add event callbacks to the player.
     * <p>
     * This callback should be exclusively used to add event listeners since it is called directly after a
     * player join (before any chunk is sent) and the client behavior can therefore be unpredictable.
     * You can add your "init" code in {@link net.minestom.server.event.player.PlayerLoginEvent}
     * or even {@link net.minestom.server.event.player.PlayerPreLoginEvent}.
     *
     * @param playerInitialization the {@link Player} initialization consumer
     */
    public void addPlayerInitialization(@NotNull Consumer<Player> playerInitialization) {
        this.playerInitializations.add(playerInitialization);
    }

    /**
     * Adds a new {@link Player} in the players list.
     * Is currently used at
     * {@link LoginStartPacket#process(PlayerConnection)}
     * and in {@link FakePlayer#initPlayer(UUID, String, Consumer)}.
     *
     * @param player the player to add
     */
    public void createPlayer(@NotNull Player player) {
        this.players.add(player);
        this.connectionPlayerMap.put(player.getPlayerConnection(), player);
    }

    /**
     * Creates a {@link Player} object and register it.
     *
     * @param uuid       the new player uuid
     * @param username   the new player username
     * @param connection the new player connection
     */
    public void createPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection connection) {
        final Player player = getPlayerProvider().createPlayer(uuid, username, connection);
        createPlayer(player);
    }

    /**
     * Removes a {@link Player} from the players list.
     * <p>
     * Used during disconnection, you shouldn't have to do it manually.
     *
     * @param connection the player connection
     */
    public void removePlayer(@NotNull PlayerConnection connection) {
        final Player player = this.connectionPlayerMap.get(connection);
        if (player == null)
            return;

        this.players.remove(player);
        this.connectionPlayerMap.remove(connection);
    }
}
