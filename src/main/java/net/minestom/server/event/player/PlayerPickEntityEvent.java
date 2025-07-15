package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jspecify.annotations.Nullable;

/**
 * Called when a player tries to pick an entity (middle-click).
 */
public class PlayerPickEntityEvent implements PlayerInstanceEvent {

    private final Player player;

    private final Entity entityTarget;
    private final boolean includeData;

    public PlayerPickEntityEvent(Player player, @Nullable Entity entityTarget,
                                 boolean includeData) {
        this.player = player;

        this.entityTarget = entityTarget;
        this.includeData = includeData;
    }

    /**
     * Gets the entity which was picked. May be null if the entity is not known by the server (eg spawned with packets).
     *
     * @return the entity which was picked
     */
    public @Nullable Entity getTarget() {
        return entityTarget;
    }

    /**
     * Get if the entity data should be included in the result (control middle-click).
     *
     * @return if the entity data should be included.
     */
    public boolean isIncludeData() {
        return this.includeData;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
