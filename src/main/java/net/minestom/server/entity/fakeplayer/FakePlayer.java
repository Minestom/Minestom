package net.minestom.server.entity.fakeplayer;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.FakePlayerConnection;

import java.util.UUID;

public class FakePlayer extends Player {

    private FakePlayerController fakePlayerController;

    public FakePlayer(UUID uuid, String username) {
        super(uuid, username, new FakePlayerConnection());
        FakePlayerConnection playerConnection = (FakePlayerConnection) getPlayerConnection();
        playerConnection.setFakePlayer(this);

        this.fakePlayerController = new FakePlayerController(this);
    }

    public FakePlayerController getController() {
        return fakePlayerController;
    }
}
