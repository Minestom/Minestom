package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.DisconnectPacket;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    private final MessagePassingQueue<Player> waitingPlayers = new MpscUnboundedArrayQueue<>(64);
    private final Set<Player> players = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiablePlayers = Collections.unmodifiableSet(players);
    private final Map<PlayerConnection, Player> connectionPlayerMap = new ConcurrentHashMap<>();

    // The uuid provider once a player login
    private volatile UuidProvider uuidProvider = (playerConnection, username) -> UUID.randomUUID();
    // The player provider to have your own Player implementation
    private volatile PlayerProvider playerProvider = Player::new;

    private Component shutdownText = Component.text("The server is shutting down.", NamedTextColor.RED);

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
    public @NotNull Collection<@NotNull Player> getOnlinePlayers() {
        return unmodifiablePlayers;
    }

    /**
     * Finds the closest player matching a given username.
     *
     * @param username the player username (can be partial)
     * @return the closest match, null if no players are online
     */
    public @Nullable Player findPlayer(@NotNull String username) {
        Player exact = getPlayer(username);
        if (exact != null) return exact;
        final String username1 = username.toLowerCase(Locale.ROOT);

        Function<Player, Double> distanceFunction = player -> {
            final String username2 = player.getUsername().toLowerCase(Locale.ROOT);
            return StringUtils.jaroWinklerScore(username1, username2);
        };
        return getOnlinePlayers()
                .stream()
                .min(Comparator.comparingDouble(distanceFunction::apply))
                .filter(player -> distanceFunction.apply(player) > 0)
                .orElse(null);
    }

    /**
     * Gets the first player which validate {@link String#equalsIgnoreCase(String)}.
     * <p>
     * This can cause issue if two or more players have the same username.
     *
     * @param username the player username (ignoreCase)
     * @return the first player who validate the username condition, null if none was found
     */
    public @Nullable Player getPlayer(@NotNull String username) {
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
    public @Nullable Player getPlayer(@NotNull UUID uuid) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUuid().equals(uuid))
                return player;
        }
        return null;
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
     * Used in {@link net.minestom.server.network.packet.client.login.LoginStartPacket} in order
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
     * Gets the kick reason when the server is shutdown using {@link MinecraftServer#stopCleanly()}.
     *
     * @return the kick reason in case on a shutdown
     */
    public @NotNull Component getShutdownText() {
        return shutdownText;
    }

    /**
     * Changes the kick reason in case of a shutdown.
     *
     * @param shutdownText the new shutdown kick reason
     * @see #getShutdownText()
     */
    public void setShutdownText(@NotNull Component shutdownText) {
        this.shutdownText = shutdownText;
    }

    public synchronized void registerPlayer(@NotNull Player player) {
        this.players.add(player);
        this.connectionPlayerMap.put(player.getPlayerConnection(), player);
    }

    /**
     * Removes a {@link Player} from the players list.
     * <p>
     * Used during disconnection, you shouldn't have to do it manually.
     *
     * @param connection the player connection
     * @see PlayerConnection#disconnect() to properly disconnect a player
     */
    public synchronized void removePlayer(@NotNull PlayerConnection connection) {
        final Player player = this.connectionPlayerMap.remove(connection);
        if (player == null) return;
        this.players.remove(player);
    }

    /**
     * Calls the player initialization callbacks and the event {@link AsyncPlayerPreLoginEvent}.
     * <p>
     * Sends a {@link LoginSuccessPacket} if successful (not kicked)
     * and change the connection state to {@link ConnectionState#PLAY}.
     *
     * @param player   the player
     * @param register true to register the newly created player in {@link ConnectionManager} lists
     */
    public void startPlayState(@NotNull Player player, boolean register) {
        AsyncUtils.runAsync(() -> {
            final PlayerConnection playerConnection = player.getPlayerConnection();
            // Call pre login event
            AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent = new AsyncPlayerPreLoginEvent(player);
            EventDispatcher.call(asyncPlayerPreLoginEvent);
            // Close the player channel if he has been disconnected (kick)
            if (!player.isOnline()) {
                playerConnection.flush();
                //playerConnection.disconnect();
                return;
            }
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
            // Send login success packet
            LoginSuccessPacket loginSuccessPacket = new LoginSuccessPacket(player.getUuid(), player.getUsername());
            if (playerConnection instanceof PlayerSocketConnection socketConnection) {
                socketConnection.writeAndFlush(loginSuccessPacket);
            } else {
                playerConnection.sendPacket(loginSuccessPacket);
            }
            playerConnection.setConnectionState(ConnectionState.PLAY);
            if (register) registerPlayer(player);
            this.waitingPlayers.relaxedOffer(player);
        });
    }

    /**
     * Creates a {@link Player} using the defined {@link PlayerProvider}
     * and execute {@link #startPlayState(Player, boolean)}.
     *
     * @return the newly created player object
     * @see #startPlayState(Player, boolean)
     */
    public @NotNull Player startPlayState(@NotNull PlayerConnection connection,
                                          @NotNull UUID uuid, @NotNull String username,
                                          boolean register) {
        final Player player = playerProvider.createPlayer(uuid, username, connection);
        startPlayState(player, register);
        return player;
    }

    /**
     * Shutdowns the connection manager by kicking all the currently connected players.
     */
    public synchronized void shutdown() {
        DisconnectPacket disconnectPacket = new DisconnectPacket(shutdownText);
        for (Player player : getOnlinePlayers()) {
            final PlayerConnection playerConnection = player.getPlayerConnection();
            playerConnection.sendPacket(disconnectPacket);
            playerConnection.flush();
            player.remove();
            playerConnection.disconnect();
        }
        this.players.clear();
        this.connectionPlayerMap.clear();
    }

    /**
     * Connects waiting players.
     */
    public void updateWaitingPlayers() {
        this.waitingPlayers.drain(waitingPlayer -> {
            PlayerLoginEvent loginEvent = new PlayerLoginEvent(waitingPlayer);
            EventDispatcher.call(loginEvent);
            final Instance spawningInstance = loginEvent.getSpawningInstance();
            Check.notNull(spawningInstance, "You need to specify a spawning instance in the PlayerLoginEvent");
            // Spawn the player at Player#getRespawnPoint
            waitingPlayer.UNSAFE_init(spawningInstance);
        });
    }

    /**
     * Updates keep alive by checking the last keep alive packet and send a new one if needed.
     *
     * @param tickStart the time of the update in milliseconds, forwarded to the packet
     */
    public void handleKeepAlive(long tickStart) {
        final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(tickStart);
        for (Player player : getOnlinePlayers()) {
            final long lastKeepAlive = tickStart - player.getLastKeepAlive();
            if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                final PlayerConnection playerConnection = player.getPlayerConnection();
                player.refreshKeepAlive(tickStart);
                playerConnection.sendPacket(keepAlivePacket);
            } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                player.kick(TIMEOUT_TEXT);
            }
        }
    }
}
