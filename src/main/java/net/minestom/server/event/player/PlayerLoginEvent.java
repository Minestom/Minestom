package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called at player login, used to define his spawn World.
 * <p>
 * Be aware that the player is not yet in a world when the event
 * is called, meaning that most player methods will not work.
 * You can use {@link PlayerSpawnEvent} and {@link PlayerSpawnEvent#isFirstSpawn()}
 * if needed.
 * <p>
 * WARNING: defining the spawning World is MANDATORY.
 */
public class PlayerLoginEvent extends PlayerEvent {

    private World spawningWorld;

    public PlayerLoginEvent(@NotNull Player player) {
        super(player);
    }

    /**
     * Gets the spawning World of the player.
     * <p>
     * WARNING: this must NOT be null, otherwise the player cannot spawn.
     *
     * @return the spawning World, null if not already defined
     */
    @Nullable
    public World getSpawningWorld() {
        return spawningWorld;
    }

    /**
     * Changes the spawning World.
     *
     * @param world the new spawning World
     */
    public void setSpawningWorld(@NotNull World world) {
        this.spawningWorld = world;
    }
}
