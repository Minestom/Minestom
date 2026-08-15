package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

import java.util.Objects;

/// Called when a Player tries to spectate an entity (start looking through its eyes).
///
/// Only called when the Player is in the spectator gamemode and if the target is reachable from the current instance.
@SuppressWarnings("ClassCanBeRecord")
public class PlayerSpectateEntityEvent implements PlayerInstanceEvent {
    private final Player player;
    private final Entity target;

    public PlayerSpectateEntityEvent(Player player, Entity target) {
        this.player = Objects.requireNonNull(player, "player");
        this.target = Objects.requireNonNull(target, "target");
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
