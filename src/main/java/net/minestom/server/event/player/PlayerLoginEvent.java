package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called at player login, used to define his spawn instance.
 * <p>
 * WARNING: defining the spawning instance is MANDATORY.
 */
public class PlayerLoginEvent extends Event {

    private final Player player;
    private Instance spawningInstance;

    public PlayerLoginEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Gets the player who is logging.
     *
     * @return the player who is logging
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the spawning instance of the player.
     * <p>
     * WARNING: this must NOT be null, otherwise the player cannot spawn.
     *
     * @return the spawning instance
     */
    @Nullable
    public Instance getSpawningInstance() {
        return spawningInstance;
    }

    /**
     * Changes the spawning instance.
     *
     * @param instance the new spawning instance
     */
    public void setSpawningInstance(@NotNull Instance instance) {
        this.spawningInstance = instance;
    }
}
