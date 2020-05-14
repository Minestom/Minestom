package net.minestom.server.event.animation;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;

public class AnimationEvent extends CancellableEvent {

    private Player player;
    private Player.Hand hand;

    public AnimationEvent(Player player, Player.Hand hand) {
        this.player = player;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public Player.Hand getHand() {
        return hand;
    }

    public void setHand(Player.Hand hand) {
        this.hand = hand;
    }
}
