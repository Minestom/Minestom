package net.minestom.server.entity.fakeplayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.FakePlayerConnection;

import java.util.UUID;

public class FakePlayer extends Player {

    private FakePlayerController fakePlayerController;
    private boolean registered;

    /**
     * @param uuid       the player uuid
     * @param username   the player username
     * @param addInCache should the player be registered internally
     *                   (gettable with {@link ConnectionManager#getOnlinePlayers()}
     *                   and {@link ConnectionManager#getPlayer(String)})
     */
    public FakePlayer(UUID uuid, String username, boolean addInCache) {
        super(uuid, username, new FakePlayerConnection());

        this.registered = addInCache;

        if (registered) {
            MinecraftServer.getConnectionManager().createPlayer(this);
        }
    }

    public FakePlayer(UUID uuid, String username) {
        this(uuid, username, false);
    }

    @Override
    protected void playerConnectionInit() {
        FakePlayerConnection playerConnection = (FakePlayerConnection) getPlayerConnection();
        playerConnection.setFakePlayer(this);
        this.fakePlayerController = new FakePlayerController(this);
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
