package net.minestom.server.entity.fakeplayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
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
public class FakePlayer extends Player {

    private final FakePlayerOption option;
    private final FakePlayerController fakePlayerController;

    private FakePlayer(@NotNull UUID uuid, @NotNull String username, @NotNull FakePlayerOption option) {
        super(uuid, username, new FakePlayerConnection());

        this.option = option;

        this.fakePlayerController = new FakePlayerController(this);

        if (option.isRegistered()) {
            MinecraftServer.getConnectionManager().createPlayer(this);
        }
    }

    /**
     * Inits a new {@link FakePlayer}.
     *
     * @param uuid          the FakePlayer uuid
     * @param username      the FakePlayer username
     * @param spawnCallback the optional callback called when the fake player first spawn
     */
    public static void initPlayer(@NotNull UUID uuid, @NotNull String username,
                                  @NotNull FakePlayerOption option, @Nullable Consumer<FakePlayer> spawnCallback) {
        final FakePlayer fakePlayer = new FakePlayer(uuid, username, option);

        if (spawnCallback != null) {
            fakePlayer.addEventCallback(PlayerSpawnEvent.class,
                    event -> {
                        if (event.isFirstSpawn()) {
                            spawnCallback.accept(fakePlayer);
                        }
                    });
        }
    }

    /**
     * Inits a new {@link FakePlayer} without adding it in cache.
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

    @NotNull
    public FakePlayerController getController() {
        return fakePlayerController;
    }

    @Override
    protected void showPlayer(@NotNull PlayerConnection connection) {
        super.showPlayer(connection);
        if (!option.isInTabList()) {
            // Remove from tab-list
            MinecraftServer.getSchedulerManager().buildTask(() -> connection.sendPacket(getRemovePlayerToList())).delay(20, TimeUnit.TICK).schedule();
        }

    }
}
