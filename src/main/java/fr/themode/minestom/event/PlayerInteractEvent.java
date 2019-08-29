package fr.themode.minestom.event;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.Player;

public class PlayerInteractEvent extends Event {

    private Entity entityTarget;
    private Player.Hand hand;

    public PlayerInteractEvent(Entity entityTarget, Player.Hand hand) {
        this.entityTarget = entityTarget;
        this.hand = hand;
    }

    public Entity getTarget() {
        return entityTarget;
    }

    public Player.Hand getHand() {
        return hand;
    }
}