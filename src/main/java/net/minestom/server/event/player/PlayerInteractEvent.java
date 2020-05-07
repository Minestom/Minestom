package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

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