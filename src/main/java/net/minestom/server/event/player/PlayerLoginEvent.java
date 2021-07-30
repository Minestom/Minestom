package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called at player login, used to define his spawn instance.
 * <p>
 * Be aware that the player is not yet in a world when the event
 * is called, meaning that most player methods will not work.
 * You can use {@link PlayerSpawnEvent} and {@link PlayerSpawnEvent#isFirstSpawn()}
 * if needed.
 * <p>
 * WARNING: defining the spawning instance is MANDATORY.
 */
public class PlayerLoginEvent implements PlayerEvent {

    private final Player player;
    private Instance spawningInstance;

    public PlayerLoginEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Gets the spawning instance of the player.
     * <p>
     * WARNING: this must NOT be null, otherwise the player cannot spawn.
     *
     * @return the spawning instance, null if not already defined
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

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
