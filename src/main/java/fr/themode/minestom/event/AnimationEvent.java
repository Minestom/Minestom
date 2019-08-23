package fr.themode.minestom.event;

import fr.themode.minestom.entity.Player;

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
