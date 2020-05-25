package net.minestom.server.entity.fakeplayer;

import net.minestom.server.network.player.PlayerConnection;

public class FakePlayerController {

    private FakePlayer fakePlayer;
    private PlayerConnection playerConnection;

    public FakePlayerController(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
        this.playerConnection = fakePlayer.getPlayerConnection();

        fakePlayer.setHeldItemSlot((short) 1);
    }
}
