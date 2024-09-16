package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.configuration.ResetChatPacket;
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket;
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.StartConfigurationPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Manages the connected clients.
 */
public final class ConnectionManager {
    private static final Component TIMEOUT_TEXT = Component.text("Timeout", NamedTextColor.RED);
    private CachedPacket defaultTags;

    private CachedPacket getDefaultTags() {
        var defaultTags = this.defaultTags;
        if (defaultTags == null) {
            final TagsPacket packet = MinecraftServer.getTagManager().packet();
            this.defaultTags = defaultTags = new CachedPacket(packet);
        }
        return defaultTags;
    }

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
     * Changes the {@link Player} provider, to change which object to link to him.
     *
     * @param playerProvider the new {@link PlayerProvider}, can be set to null to apply the default provider
     */
    public void setPlayerProvider(@Nullable PlayerProvider playerProvider) {
        this.playerProvider = playerProvider != null ? playerProvider : Player::new;
    }

    @ApiStatus.Internal
    public @NotNull Player createPlayer(@NotNull PlayerConnection connection, @NotNull GameProfile gameProfile) {
        assert ServerFlag.INSIDE_TEST || Thread.currentThread().isVirtual();
        final Player player = playerProvider.createPlayer(connection, gameProfile);
        this.connectionPlayerMap.put(connection, player);
        return player;
    }

    public GameProfile transitionLoginToConfig(@NotNull PlayerConnection connection, @NotNull GameProfile gameProfile) {
        assert ServerFlag.INSIDE_TEST || Thread.currentThread().isVirtual();
        // Compression
        if (connection instanceof PlayerSocketConnection socketConnection) {
            final int threshold = MinecraftServer.getCompressionThreshold();
            if (threshold > 0) socketConnection.startCompression();
        }
        // Call pre login event
        LoginPluginMessageProcessor pluginMessageProcessor = connection.loginPluginMessageProcessor();
        AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent = new AsyncPlayerPreLoginEvent(gameProfile, pluginMessageProcessor);
        EventDispatcher.call(asyncPlayerPreLoginEvent);
        if (!connection.isOnline()) return gameProfile; // Player has been kicked
        // Change UUID/Username based on the event
        gameProfile = asyncPlayerPreLoginEvent.getGameProfile();
        // Wait for pending login plugin messages
        try {
            pluginMessageProcessor.awaitReplies(ServerFlag.LOGIN_PLUGIN_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            connection.kick(LoginListener.INVALID_PROXY_RESPONSE);
            throw new RuntimeException("Error getting replies for login plugin messages", t);
        }
        // Send login success packet (and switch to configuration phase)
        connection.sendPacket(new LoginSuccessPacket(gameProfile, true));
        return gameProfile;
    }

    @ApiStatus.Internal
    public void transitionPlayToConfig(@NotNull Player player) {
        player.sendPacket(new StartConfigurationPacket());
        configurationPlayers.add(player);
    }

    /**
     * Return value exposed for testing
     */
    @ApiStatus.Internal
    public void doConfiguration(@NotNull Player player, boolean isFirstConfig) {
        assert ServerFlag.INSIDE_TEST || Thread.currentThread().isVirtual();
        if (isFirstConfig) {
            configurationPlayers.add(player);
            keepAlivePlayers.add(player);
        }
        player.sendPacket(PluginMessagePacket.brandPacket(MinecraftServer.getBrandName()));
        // Request known packs immediately, but don't wait for the response until required (sending registry data).
        final var knownPacksFuture = player.getPlayerConnection().requestKnownPacks(List.of(SelectKnownPacksPacket.MINECRAFT_CORE));

        var event = new AsyncPlayerConfigurationEvent(player, isFirstConfig);
        EventDispatcher.call(event);
        if (!player.isOnline()) return; // Player was kicked during config.

        // send player features that were enabled or disabled during async config event
        player.sendPacket(new UpdateEnabledFeaturesPacket(event.getFeatureFlags().stream().map(StaticProtocolObject::name).toList()));

        final Instance spawningInstance = event.getSpawningInstance();
        Check.notNull(spawningInstance, "You need to specify a spawning instance in the AsyncPlayerConfigurationEvent");

        if (event.willClearChat()) player.sendPacket(new ResetChatPacket());

        // Registry data (if it should be sent)
        if (event.willSendRegistryData()) {
            List<SelectKnownPacksPacket.Entry> knownPacks;
            try {
                knownPacks = knownPacksFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException e) {
                throw new RuntimeException("Client failed to respond to known packs request", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Error receiving known packs", e);
            }
            boolean excludeVanilla = knownPacks.contains(SelectKnownPacksPacket.MINECRAFT_CORE);

            var serverProcess = MinecraftServer.process();
            player.sendPacket(serverProcess.chatType().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.dimensionType().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.biome().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.damageType().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.trimMaterial().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.trimPattern().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.bannerPattern().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.wolfVariant().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.enchantment().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.paintingVariant().registryDataPacket(excludeVanilla));
            player.sendPacket(serverProcess.jukeboxSong().registryDataPacket(excludeVanilla));

            player.sendPacket(getDefaultTags());
        }

        // Wait for pending resource packs if any
        var packFuture = player.getResourcePackFuture();
        if (packFuture != null) packFuture.join();

        keepAlivePlayers.remove(player);
        player.setPendingOptions(spawningInstance, event.isHardcore());
        player.sendPacket(new FinishConfigurationPacket());
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
            if (!player.isOnline()) return; // Player disconnected while in queued to join
            playPlayers.add(player);
            keepAlivePlayers.add(player);

            // This fixes a bug with Geyser. They do not reply to keep alive during config, meaning that
            // `Player#didAnswerKeepAlive()` will always be false when entering the play state, so a new keep
            // alive will never be sent and they will disconnect themselves or we will kick them for not replying.
            player.refreshAnswerKeepAlive(true);

            // Spawn the player at Player#getRespawnPoint
            CompletableFuture<Void> spawnFuture = player.UNSAFE_init();

            // Required to get the exact moment the player spawns
            if (ServerFlag.INSIDE_TEST) spawnFuture.join();
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
            if (lastKeepAlive > ServerFlag.KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                player.refreshKeepAlive(tickStart);
                player.sendPacket(keepAlivePacket);
            } else if (lastKeepAlive >= ServerFlag.KEEP_ALIVE_KICK) {
                player.kick(TIMEOUT_TEXT);
            }
        }
    }
}
