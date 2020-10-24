package net.minestom.server.entity.fakeplayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.network.player.FakePlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

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
     * Init a new {@link FakePlayer}.
     *
     * @param uuid              the FakePlayer uuid
     * @param username          the FakePlayer username
     * @param scheduledCallback the callback called when the FakePlayer is finished logging
     *                          (1 tick after the {@link PlayerLoginEvent})
     *                          WARNING: it will be called in the
     *                          {@link net.minestom.server.timer.SchedulerManager} thread pool
     */
    public static void initPlayer(@NotNull UUID uuid, @NotNull String username,
                                  @NotNull FakePlayerOption option, @NotNull Consumer<FakePlayer> scheduledCallback) {
        final FakePlayer fakePlayer = new FakePlayer(uuid, username, option);

        fakePlayer.addEventCallback(PlayerLoginEvent.class, event -> MinecraftServer.getSchedulerManager().buildTask(() -> scheduledCallback.accept(fakePlayer)).delay(1, TimeUnit.TICK).schedule());
    }

    /**
     * Init a new {@link FakePlayer} without adding him in cache.
     *
     * @param uuid              the FakePlayer uuid
     * @param username          the FakePlayer username
     * @param scheduledCallback the callback called when the FakePlayer is finished logging
     *                          WARNING: it will be called in the
     *                          {@link net.minestom.server.timer.SchedulerManager} thread pool
     */
    public static void initPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull Consumer<FakePlayer> scheduledCallback) {
        initPlayer(uuid, username, new FakePlayerOption(), scheduledCallback);
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
