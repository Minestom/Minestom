package net.minestom.server.event;

import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

public class WorldEvent extends Event {

    protected final World world;

    public WorldEvent(@NotNull World world) {
        this.world = world;
    }

    /**
     * Gets the World involved in this event.
     *
     * @return the World involved
     */
    @NotNull
    public World getWorld() {
        return world;
    }
}