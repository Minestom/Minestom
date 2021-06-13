package net.minestom.server.event.world;

import net.minestom.server.event.WorldEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a World processes a tick.
 */
public class WorldTickEvent extends WorldEvent {

    private final int duration;

    public WorldTickEvent(@NotNull World world, long time, long lastTickAge) {
        super(world);
        this.duration = (int) (time - lastTickAge);
    }

    /**
     * Gets the duration of the tick in ms.
     *
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }
}