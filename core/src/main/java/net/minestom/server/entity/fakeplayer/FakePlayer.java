package net.minestom.server.entity.fakeplayer;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.FakePlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A fake player will behave exactly the same way as would do a {@link Player} backed by a netty connection
 * (events, velocity, gravity, player list, etc...) with the exception that you need to control it server-side
 * using a {@link FakePlayerController} (see {@link #getController()}).
 * <p>
 * You can create one using {@link #initPlayer(UUID, String, Consumer)}. Be aware that this really behave exactly like a player
 * and this is a feature not a bug, you will need to check at some place if the player is a fake one or not (instanceof) if you want to change it.
 */
public class FakePlayer extends Player implements NavigableEntity {

    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    private final FakePlayerOption option;
    private final FakePlayerController fakePlayerController;

    private final Navigator navigator = new Navigator(this);

    /**
     * Initializes a new {@link FakePlayer} with the given {@code uuid}, {@code username} and {@code option}'s.
     *
     * @param uuid     The unique identifier for the fake player.
     * @param username The username for the fake player.
     * @param option   Any option for the fake player.
     */
    protected FakePlayer(@NotNull UUID uuid, @NotNull String username,
                         @NotNull FakePlayerOption option,
                         @Nullable Consumer<FakePlayer> spawnCallback) {
        super(uuid, username, new FakePlayerConnection());

        this.option = option;

        this.fakePlayerController = new FakePlayerController(this);

        if (spawnCallback != null) {
            addEventCallback(PlayerSpawnEvent.class,
                    event -> {
                        if (event.isFirstSpawn()) {
                            spawnCallback.accept(this);
                        }
                    });
        }

        CONNECTION_MANAGER.startPlayState(this, option.isRegistered());
    }

    /**
     * Initializes a new {@link FakePlayer}.
     *
     * @param uuid          the FakePlayer uuid
     * @param username      the FakePlayer username
     * @param spawnCallback the optional callback called when the fake player first spawn
     */
    public static void initPlayer(@NotNull UUID uuid, @NotNull String username,
                                  @NotNull FakePlayerOption option, @Nullable Consumer<FakePlayer> spawnCallback) {
        new FakePlayer(uuid, username, option, spawnCallback);
    }

    /**
     * Initializes a new {@link FakePlayer} without adding it in cache.
     * <p>
     * If you want the fake player to be obtainable with the {@link net.minestom.server.network.ConnectionManager}
     * you need to specify it in a {@link FakePlayerOption} and use {@link #initPlayer(UUID, String, FakePlayerOption, Consumer)}.
     *
     * @param uuid          the FakePlayer uuid
     * @param username      the FakePlayer username
     * @param spawnCallback the optional callback called when the fake player first spawn
     */
    public static void initPlayer(@NotNull UUID uuid, @NotNull String username, @Nullable Consumer<FakePlayer> spawnCallback) {
        initPlayer(uuid, username, new FakePlayerOption(), spawnCallback);
    }

    /**
     * Gets the fake player option container.
     *
     * @return the fake player option
     */
    @NotNull
    public FakePlayerOption getOption() {
        return option;
    }

    /**
     * Retrieves the controller for the fake player.
     *
     * @return The fake player's controller.
     */
    @NotNull
    public FakePlayerController getController() {
        return fakePlayerController;
    }

    @Override
    public void update(long time) {
        super.update(time);

        // Path finding
        this.navigator.tick(getAttributeValue(Attribute.MOVEMENT_SPEED));
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
        this.navigator.setPathFinder(new HydrazinePathFinder(navigator.getPathingEntity(), instance.getInstanceSpace()));

        super.setInstance(instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void showPlayer(@NotNull PlayerConnection connection) {
        super.showPlayer(connection);
        if (!option.isInTabList()) {
            // Remove from tab-list
            MinecraftServer.getSchedulerManager().buildTask(() -> connection.sendPacket(getRemovePlayerToList())).delay(20, TimeUnit.TICK).schedule();
        }

    }

    @NotNull
    @Override
    public Navigator getNavigator() {
        return navigator;
    }
}
