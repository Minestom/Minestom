package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called just before the on ground state of the player is changed.
 */
public class PlayerOnGroundChangeEvent implements PlayerInstanceEvent {

    private final Player player;
    private final boolean onGround;

    public PlayerOnGroundChangeEvent(Player player, boolean onGround) {
        this.player = player;
        this.onGround = onGround;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public boolean isOnGround() {
        return onGround;
    }

}
