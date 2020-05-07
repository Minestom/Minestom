package net.minestom.server.event.animation;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;

public class AnimationEvent extends CancellableEvent {

    private Player.Hand hand;

    public AnimationEvent(Player.Hand hand) {
        this.hand = hand;
    }

    public Player.Hand getHand() {
        return hand;
    }

    public void setHand(Player.Hand hand) {
        this.hand = hand;
    }
}
