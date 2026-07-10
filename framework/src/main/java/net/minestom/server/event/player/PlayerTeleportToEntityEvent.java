package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

import java.util.Objects;

/// Called when a player teleports to another entity, through the spectator hotbar.
///
/// The target is not required to be in the same instance as the player.
public class PlayerTeleportToEntityEvent implements PlayerInstanceEvent {
    private final Player player;
    private final Entity target;

    public PlayerTeleportToEntityEvent(Player player, Entity target) {
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
