package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;

/**
 * Called at player login, used to define his spawn instance
 * <p>
 * WARNING: defining the spawning instance is MANDATORY
 */
public class PlayerLoginEvent extends Event {

    private final Player player;
    private Instance spawningInstance;

    public PlayerLoginEvent(Player player) {
        this.player = player;
    }

    /**
     * Get the player who is logging
     *
     * @return the player who is logging
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the spawning instance of the player
     * <p>
     * WARNING: this must NOT be null, otherwise the player cannot spawn
     *
     * @return the spawning instance
     */
    public Instance getSpawningInstance() {
        return spawningInstance;
    }

    /**
     * Change the spawning instance
     *
     * @param instance the new spawning instance
     */
    public void setSpawningInstance(Instance instance) {
        this.spawningInstance = instance;
    }
}
