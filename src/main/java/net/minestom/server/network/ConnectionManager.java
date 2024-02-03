package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
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
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

/**
 * Manages the connected clients.
 */
public final class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

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

    // The players who need keep alive ticks. This was added because we may not send a keep alive in
    // the time after sending finish configuration but before receiving configuration end (to swap to play).
    // I(mattw) could not come up with a better way to express this besides completely splitting client/server
    // states. Perhaps there will be an improvement in the future.
    private final Set<Player> keepAlivePlayers = new CopyOnWriteArraySet<>();

    private final Set<Player> unmodifiableConfigurationPlayers = Collections.unmodifiableSet(configurationPlayers);
    private final Set<Player> unmodifiablePlayPlayers = Collections.unmodifiableSet(playPlayers);


    // The uuid provider once a player login
    private volatile UuidProvider uuidProvider = (playerConnection, username) -> UUID.randomUUID();
    // The player provider to have your own Player implementation
    private volatile PlayerProvider playerProvider = Player::new;

    /**
     * Gets the number of "online" players, eg for the query response.
     *
     * <p>Only includes players in the play state, not players in configuration.</p>
     */
    public int getOnlinePlayerCount() {
        return playPlayers.size();
    }

    /**
     * Returns an unmodifiable set containing the players currently in the play state.
     */
    public @NotNull Collection<@NotNull Player> getOnlinePlayers() {
        return unmodifiablePlayPlayers;
    }

    /**
     * Returns an unmodifiable set containing the players currently in the configuration state.
     */
    public @NotNull Collection<@NotNull Player> getConfigPlayers() {
        return unmodifiableConfigurationPlayers;
    }

    /**
     * Gets the {@link Player} linked to a {@link PlayerConnection}.
     *
     * <p>The player will be returned whether they are in the play or config state,
     * so be sure to check before sending packets to them.</p>
     *
     * @param connection the player connection
     * @return the player linked to the connection
     */
    public Player getPlayer(@NotNull PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    /**
     * Gets the first player in the play state which validates {@link String#equalsIgnoreCase(String)}.
     * <p>
     * This can cause issue if two or more players have the same username.
     *
     * @param username the player username (case-insensitive)
     * @return the first player who validate the username condition, null if none was found
     */
    public @Nullable Player getOnlinePlayerByUsername(@NotNull String username) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(username))
                return player;
        }
        return null;
    }

    /**
     * Gets the first player in the play state which validates {@link UUID#equals(Object)}.
     * <p>
     * This can cause issue if two or more players have the same UUID.
     *
     * @param uuid the player UUID
     * @return the first player who validate the UUID condition, null if none was found
     */
    public @Nullable Player getOnlinePlayerByUuid(@NotNull UUID uuid) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUuid().equals(uuid))
                return player;
        }
        return null;
    }

    /**
     * Finds the closest player in the play state matching a given username.
     *
     * @param username the player username (can be partial)
     * @return the closest match, null if no players are online
     */
    public @Nullable Player findOnlinePlayer(@NotNull String username) {
        Player exact = getOnlinePlayerByUsername(username);
        if (exact != null) return exact;
        final String username1 = username.toLowerCase(Locale.ROOT);

        Function<Player, Double> distanceFunction = player -> {
            final String username2 = player.getUsername().toLowerCase(Locale.ROOT);
            return StringUtils.jaroWinklerScore(username1, username2);
        };
        return getOnlinePlayers().stream()
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
     * Creates a player object and begins the transition from the login state to the config state.
     */
    @ApiStatus.Internal
    public @NotNull Player createPlayer(@NotNull PlayerConnection connection, @NotNull UUID uuid, @NotNull String username) {
        final Player player = playerProvider.createPlayer(uuid, username, connection);
        this.connectionPlayerMap.put(connection, player);
        var future = transitionLoginToConfig(player);
        if (DebugUtils.INSIDE_TEST) future.join();
        return player;
    }

    @ApiStatus.Internal
    public @NotNull CompletableFuture<Void> transitionLoginToConfig(@NotNull Player player) {
        return AsyncUtils.runAsync(() -> {
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
        });
    }

    @ApiStatus.Internal
    public void transitionPlayToConfig(@NotNull Player player) {
        player.sendPacket(new StartConfigurationPacket());
        configurationPlayers.add(player);
    }

    @ApiStatus.Internal
    public void doConfiguration(@NotNull Player player, boolean isFirstConfig) {
        if (isFirstConfig) {
            configurationPlayers.add(player);
            keepAlivePlayers.add(player);
        }

        player.getPlayerConnection().setConnectionState(ConnectionState.CONFIGURATION);
        CompletableFuture<Void> configFuture = AsyncUtils.runAsync(() -> {
            player.sendPacket(PluginMessagePacket.getBrandPacket());

            var event = new AsyncPlayerConfigurationEvent(player, isFirstConfig);
            EventDispatcher.call(event);

            final Instance spawningInstance = event.getSpawningInstance();
            Check.notNull(spawningInstance, "You need to specify a spawning instance in the AsyncPlayerConfigurationEvent");

            // Registry data (if it should be sent)
            if (event.willSendRegistryData()) {
                var registry = new HashMap<String, NBT>();
                registry.put("minecraft:chat_type", Messenger.chatRegistry());
                registry.put("minecraft:dimension_type", MinecraftServer.getDimensionTypeManager().toNBT());
                registry.put("minecraft:worldgen/biome", MinecraftServer.getBiomeManager().toNBT());
                registry.put("minecraft:damage_type", DamageType.getNBT());
                registry.put("minecraft:trim_material", MinecraftServer.getTrimManager().getTrimMaterialNBT());
                registry.put("minecraft:trim_pattern", MinecraftServer.getTrimManager().getTrimPatternNBT());
                player.sendPacket(new RegistryDataPacket(NBT.Compound(registry)));

                player.sendPacket(TagsPacket.DEFAULT_TAGS);
            }

            // Wait for pending resource packs if any
            var packFuture = player.getResourcePackFuture();
            if (packFuture != null) packFuture.join();

            keepAlivePlayers.remove(player);
            player.setPendingOptions(spawningInstance, event.isHardcore());
            player.sendPacket(new FinishConfigurationPacket());
        });
        if (DebugUtils.INSIDE_TEST) configFuture.join();
    }

    @ApiStatus.Internal
    public void transitionConfigToPlay(@NotNull Player player) {
        this.waitingPlayers.relaxedOffer(player);
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
        this.keepAlivePlayers.remove(player);
    }

    /**
     * Shutdowns the connection manager by kicking all the currently connected players.
     */
    public synchronized void shutdown() {
        this.configurationPlayers.clear();
        this.playPlayers.clear();
        this.keepAlivePlayers.clear();
        this.connectionPlayerMap.clear();
    }

    public void tick(long tickStart) {
        // Let waiting players into their instances
        updateWaitingPlayers();

        // Send keep alive packets
        handleKeepAlive(keepAlivePlayers, tickStart);

        // Interpret packets for configuration players
        configurationPlayers.forEach(Player::interpretPacketQueue);
    }

    /**
     * Connects waiting players.
     */
    @ApiStatus.Internal
    public void updateWaitingPlayers() {
        this.waitingPlayers.drain(player -> {
            player.getPlayerConnection().setConnectionState(ConnectionState.PLAY);
            playPlayers.add(player);
            keepAlivePlayers.add(player);

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
