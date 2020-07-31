package net.minestom.server.entity.fakeplayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.network.player.FakePlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.time.TimeUnit;

import java.util.UUID;
import java.util.function.Consumer;

public class FakePlayer extends Player {

    private FakePlayerOption option;
    private FakePlayerController fakePlayerController;

    private FakePlayer(UUID uuid, String username, FakePlayerOption option) {
        super(uuid, username, new FakePlayerConnection());

        this.option = option;

        this.fakePlayerController = new FakePlayerController(this);

        if (option.isRegistered()) {
            MinecraftServer.getConnectionManager().createPlayer(this);
        }
    }

    /**
     * Init a new FakePlayer
     *
     * @param uuid              the FakePlayer uuid
     * @param username          the FakePlayer username
     * @param scheduledCallback the callback called when the FakePlayer is finished logging
     *                          (1 tick after the {@link PlayerLoginEvent})
     *                          WARNING: it will be called in the
     *                          {@link net.minestom.server.timer.SchedulerManager} thread pool
     */
    public static void initPlayer(UUID uuid, String username, FakePlayerOption option, Consumer<FakePlayer> scheduledCallback) {
        final FakePlayer fakePlayer = new FakePlayer(uuid, username, option);

        fakePlayer.addEventCallback(PlayerLoginEvent.class, event -> {
            MinecraftServer.getSchedulerManager().buildTask(() -> scheduledCallback.accept(fakePlayer)).delay(1, TimeUnit.TICK).schedule();
        });
    }

    /**
     * Init a new FakePlayer without adding him in cache
     *
     * @param uuid              the FakePlayer uuid
     * @param username          the FakePlayer username
     * @param scheduledCallback the callback called when the FakePlayer is finished logging
     *                          WARNING: it will be called in the
     *                          {@link net.minestom.server.timer.SchedulerManager} thread pool
     */
    public static void initPlayer(UUID uuid, String username, Consumer<FakePlayer> scheduledCallback) {
        initPlayer(uuid, username, new FakePlayerOption(), scheduledCallback);
    }

    /**
     * Get the fake player option container
     *
     * @return the fake player option
     */
    public FakePlayerOption getOption() {
        return option;
    }

    public FakePlayerController getController() {
        return fakePlayerController;
    }

    @Override
    protected void showPlayer(PlayerConnection connection) {
        super.showPlayer(connection);
        if (!option.isInTabList()) {
            // Remove from tab-list
            MinecraftServer.getSchedulerManager().buildTask(() -> connection.sendPacket(getRemovePlayerToList())).delay(20, TimeUnit.TICK).schedule();
        }

    }
}
