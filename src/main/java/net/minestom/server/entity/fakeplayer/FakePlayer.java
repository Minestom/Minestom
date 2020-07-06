package net.minestom.server.entity.fakeplayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.FakePlayerConnection;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;

import java.util.UUID;
import java.util.function.Consumer;

public class FakePlayer extends Player {

    private FakePlayerController fakePlayerController;
    private boolean registered;

    private FakePlayer(UUID uuid, String username, boolean addInCache) {
        super(uuid, username, new FakePlayerConnection());

        this.fakePlayerController = new FakePlayerController(this);

        this.registered = addInCache;

        if (registered) {
            MinecraftServer.getConnectionManager().createPlayer(this);
        }
    }

    /**
     * Init a new FakePlayer
     *
     * @param uuid              the FakePlayer uuid
     * @param username          the FakePlayer username
     * @param addInCache        should the player be registered internally
     *                          (gettable with {@link ConnectionManager#getOnlinePlayers()})
     * @param scheduledCallback the callback called when the FakePlayer is finished logging
     *                          (1 tick after the {@link PlayerLoginEvent})
     *                          WARNING: it will be called in the
     *                          {@link net.minestom.server.timer.SchedulerManager} thread pool
     */
    public static void initPlayer(UUID uuid, String username, boolean addInCache, Consumer<FakePlayer> scheduledCallback) {
        final FakePlayer fakePlayer = new FakePlayer(uuid, username, addInCache);

        fakePlayer.addEventCallback(PlayerLoginEvent.class, event -> {
            MinecraftServer.getSchedulerManager().addDelayedTask(new TaskRunnable() {
                @Override
                public void run() {
                    scheduledCallback.accept(fakePlayer);
                }
            }, new UpdateOption(1, TimeUnit.TICK));
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
        initPlayer(uuid, username, false, scheduledCallback);
    }

    public FakePlayerController getController() {
        return fakePlayerController;
    }

    /**
     * @return true if the player is registered in {@link ConnectionManager}, false otherwise
     */
    public boolean isRegistered() {
        return registered;
    }
}
