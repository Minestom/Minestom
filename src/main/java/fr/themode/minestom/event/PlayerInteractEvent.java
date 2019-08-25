package fr.themode.minestom.event;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.Player;

public class PlayerInteractEvent extends Event {

    private Entity target;
    private Player.Hand hand;

    public PlayerInteractEvent(Entity target, Player.Hand hand) {
        this.target = target;
        this.hand = hand;
    }

    public Entity getTarget() {
        return target;
    }

    public Player.Hand getHand() {
        return hand;
    }
}