package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.StartConfigurationPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.debug.DebugUtils;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

/**
 * Manages the connected clients.
 */
public final class ConnectionManager {
    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;
    private static final Component TIMEOUT_TEXT = Component.text("Timeout", NamedTextColor.RED);


    // All players once their Player object has been instantiated.
    private final Map<PlayerConnection, Player> connectionPlayerMap = new ConcurrentHashMap<>();
    // Players waiting to be spawned (post configuration state)
    private final MessagePassingQueue<Player> waitingPlayers = new MpscUnboundedArrayQueue<>(64);
    // Players in configuration state
    private final Set<Player> configurationPlayers = new CopyOnWriteArraySet<>();
    // Players in play state
    private final Set<Player> playPlayers = new CopyOnWriteArraySet<>();


    // The uuid provider once a player login
    private volatile UuidProvider uuidProvider = (playerConnection, username) -> UUID.randomUUID();
    // The player provider to have your own Player implementation
    private volatile PlayerProvider playerProvider = Player::new;

    /**
     * Gets the number of "online" players, eg for the query response.
     *
     * @apiNote Only includes players in the play state, not players in configuration.
     */
    public int getOnlinePlayerCount() {
        return playPlayers.size();
    }

    /**
     * Gets players filtered by state.
     *
     * @param states The state(s) to return players, if empty all players (play and config) are returned.
     *               <b>Only</b> {@link ConnectionState#CONFIGURATION} and {@link ConnectionState#PLAY} are valid.
     *
     * @return An unmodifiable collection containing the filtered players.
     * @apiNote The returned collection has no defined update behavior relative to the state of the server,
     * so it should be refetched whenever used, rather than kept and reused.
     */
    public @NotNull Collection<@NotNull Player> getPlayers(@NotNull ConnectionState... states) {
        boolean play = false, config = false;
        for (var state : states) {
            switch (state) {
                case PLAY -> play = true;
                case CONFIGURATION -> config = true;
                default -> throw new IllegalArgumentException("Cannot fetch players in " + state + "!");
            }
        }

        if (config && !play) { // Only play
            return Collections.unmodifiableCollection(configurationPlayers);
        } else if (!config && play) { // Only configuration
            return Collections.unmodifiableCollection(playPlayers);
        } else { // Both or neither was specified
            final var players = new ArrayList<Player>(playPlayers.size() + configurationPlayers.size());
            players.addAll(configurationPlayers);
            players.addAll(playPlayers);
            return Collections.unmodifiableCollection(players);
        }
    }

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
     * Gets the first player which validate {@link String#equalsIgnoreCase(String)}.
     * <p>
     * This can cause issue if two or more players have the same username.
     *
     * @param username the player username (case-insensitive)
     * @param states The state(s) to return players, if empty all players (play and config) are returned.
     *               <b>Only</b> {@link ConnectionState#CONFIGURATION} and {@link ConnectionState#PLAY} are valid.
     * @return the first player who validate the username condition, null if none was found
     */
    public @Nullable Player getPlayer(@NotNull String username, @NotNull ConnectionState... states) {
        for (Player player : getPlayers(states)) {
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
     * @param states The state(s) to return players, if empty all players (play and config) are returned.
     *               <b>Only</b> {@link ConnectionState#CONFIGURATION} and {@link ConnectionState#PLAY} are valid.
     * @return the first player who validate the UUID condition, null if none was found
     */
    public @Nullable Player getPlayer(@NotNull UUID uuid, @NotNull ConnectionState... states) {
        for (Player player : getPlayers(states)) {
            if (player.getUuid().equals(uuid))
                return player;
        }
        return null;
    }

    /**
     * Finds the closest player matching a given username.
     *
     * @param username the player username (can be partial)
     * @param states The state(s) to return players, if empty all players (play and config) are returned.
     *               <b>Only</b> {@link ConnectionState#CONFIGURATION} and {@link ConnectionState#PLAY} are valid.
     * @return the closest match, null if no players are online
     */
    public @Nullable Player findPlayer(@NotNull String username, @NotNull ConnectionState... states) {
        Player exact = getPlayer(username);
        if (exact != null) return exact;
        final String username1 = username.toLowerCase(Locale.ROOT);

        Function<Player, Double> distanceFunction = player -> {
            final String username2 = player.getUsername().toLowerCase(Locale.ROOT);
            return StringUtils.jaroWinklerScore(username1, username2);
        };
        return getPlayers(states).stream()
                .min(Comparator.comparingDouble(distanceFunction::apply))
                .filter(player -> distanceFunction.apply(player) > 0)
                .orElse(null);
    }

    /**
     * Changes how {@link UUID} are attributed to players.
     * <p>
     * Shouldn't be override if already defined.
     * <p>
     * Be aware that it is possible for an UUID provider to be ignored, for example in the case of a proxy (eg: velocity).
     *
     * @param uuidProvider the new player connection uuid provider,
     *                     setting it to null would apply a random UUID for each player connection
     * @see #getPlayerConnectionUuid(PlayerConnection, String)
     */
    public void setUuidProvider(@Nullable UuidProvider uuidProvider) {
        this.uuidProvider = uuidProvider != null ? uuidProvider : (playerConnection, username) -> UUID.randomUUID();
    }

    /**
     * Computes the UUID of the specified connection.
     * Used in {@link ClientLoginStartPacket} in order
     * to give the player the right {@link UUID}.
     *
     * @param playerConnection the player connection
     * @param username         the username given by the connection
     * @return the uuid based on {@code playerConnection}
     * return a random UUID if no UUID provider is defined see {@link #setUuidProvider(UuidProvider)}
     */
    public @NotNull UUID getPlayerConnectionUuid(@NotNull PlayerConnection playerConnection, @NotNull String username) {
        return uuidProvider.provide(playerConnection, username);
    }

    /**
     * Changes the {@link Player} provider, to change which object to link to him.
     *
     * @param playerProvider the new {@link PlayerProvider}, can be set to null to apply the default provider
     */
    public void setPlayerProvider(@Nullable PlayerProvider playerProvider) {
        this.playerProvider = playerProvider != null ? playerProvider : Player::new;
    }

    /**
     * Retrieves the current {@link PlayerProvider}, can be the default one if none is defined.
     *
     * @return the current {@link PlayerProvider}
     */
    public @NotNull PlayerProvider getPlayerProvider() {
        return playerProvider;
    }


    /**
     * Creates a player object and begins the transition from the login state to the config state.
     */
    @ApiStatus.Internal
    public @NotNull Player createPlayer(@NotNull PlayerConnection connection, @NotNull UUID uuid, @NotNull String username) {
        final Player player = playerProvider.createPlayer(uuid, username, connection);
        this.connectionPlayerMap.put(connection, player);
        transitionLoginToConfig(player);
        return player;
    }

    @ApiStatus.Internal
    public void transitionLoginToConfig(@NotNull Player player) {
        AsyncUtils.runAsync(() -> {
            final PlayerConnection playerConnection = player.getPlayerConnection();

            // Compression
            if (playerConnection instanceof PlayerSocketConnection socketConnection) {
                final int threshold = MinecraftServer.getCompressionThreshold();
                if (threshold > 0) socketConnection.startCompression();
            }

            // Call pre login event
            AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent = new AsyncPlayerPreLoginEvent(player);
            EventDispatcher.call(asyncPlayerPreLoginEvent);
            if (!player.isOnline())
                return; // Player has been kicked

            // Change UUID/Username based on the event
            {
                final String eventUsername = asyncPlayerPreLoginEvent.getUsername();
                final UUID eventUuid = asyncPlayerPreLoginEvent.getPlayerUuid();
                if (!player.getUsername().equals(eventUsername)) {
                    player.setUsernameField(eventUsername);
                }
                if (!player.getUuid().equals(eventUuid)) {
                    player.setUuid(eventUuid);
                }
            }

            // Send login success packet (and switch to configuration phase)
            LoginSuccessPacket loginSuccessPacket = new LoginSuccessPacket(player.getUuid(), player.getUsername(), 0);
            playerConnection.sendPacket(loginSuccessPacket);
            configurationPlayers.add(player);
        });
    }

    @ApiStatus.Internal
    public void transitionPlayToConfig(@NotNull Player player) {
        player.sendPacket(new StartConfigurationPacket());
        configurationPlayers.add(player);
    }

    @ApiStatus.Internal
    public void transitionConfigToPlay(@NotNull Player player, boolean isFirstConfig) {
        // Call the event and spawn the player.
        AsyncUtils.runAsync(() -> {
            var event = new AsyncPlayerConfigurationEvent(player, isFirstConfig);
            EventDispatcher.call(event);

            final Instance spawningInstance = event.getSpawningInstance();
            Check.notNull(spawningInstance, "You need to specify a spawning instance in the AsyncPlayerConfigurationEvent");

            player.setPendingInstance(spawningInstance);
            this.waitingPlayers.relaxedOffer(player);
            player.sendPacket(new FinishConfigurationPacket());
        });
    }

    /**
     * Removes a {@link Player} from the players list.
     * <p>
     * Used during disconnection, you shouldn't have to do it manually.
     *
     * @param connection the player connection
     * @see PlayerConnection#disconnect() to properly disconnect a player
     */
    @ApiStatus.Internal
    public synchronized void removePlayer(@NotNull PlayerConnection connection) {
        final Player player = this.connectionPlayerMap.remove(connection);
        if (player == null) return;
        this.configurationPlayers.remove(player);
        this.playPlayers.remove(player);
    }

    /**
     * Shutdowns the connection manager by kicking all the currently connected players.
     */
    public synchronized void shutdown() {
        this.configurationPlayers.clear();
        this.playPlayers.clear();
        this.connectionPlayerMap.clear();
    }

    public void tick(long tickStart) {
        // Let waiting players into their instances
        updateWaitingPlayers();

        // Send keep alive packets
        handleKeepAlive(configurationPlayers, tickStart);
        handleKeepAlive(playPlayers, tickStart);

        // Interpret packets for configuration players
        configurationPlayers.forEach(Player::interpretPacketQueue);
    }

    /**
     * Connects waiting players.
     */
    private void updateWaitingPlayers() {
        this.waitingPlayers.drain(player -> {
            configurationPlayers.remove(player);
            playPlayers.add(player);

            // Spawn the player at Player#getRespawnPoint
            CompletableFuture<Void> spawnFuture = player.UNSAFE_init();

            // Required to get the exact moment the player spawns
            if (DebugUtils.INSIDE_TEST) spawnFuture.join();
        });
    }

    /**
     * Updates keep alive by checking the last keep alive packet and send a new one if needed.
     *
     * @param tickStart the time of the update in milliseconds, forwarded to the packet
     */
    private void handleKeepAlive(@NotNull Collection<Player> playerGroup, long tickStart) {
        final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(tickStart);
        for (Player player : playerGroup) {
            final long lastKeepAlive = tickStart - player.getLastKeepAlive();
            if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                player.refreshKeepAlive(tickStart);
                player.sendPacket(keepAlivePacket);
            } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                player.kick(TIMEOUT_TEXT);
            }
        }
    }
}
