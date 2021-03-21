package net.minestom.server;

import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an element which is ticked at a regular interval.
 * <p>
 * Each tickable element can also be a container for multiple elements using {@link #getTickableElements()}.
 */
public interface Tickable {

    /**
     * Ticks this element if {@link #getTickRate()} allows it.
     * <p>
     * Will execute this same method for every elements inside {@link #getTickableElements()}.
     *
     * @param time the time of the tick in milliseconds
     */
    void tick(long time);

    /**
     * Gets all the tickable elements linked to this.
     *
     * @return a modifiable {@link List} containing the linked tickable elements
     */
    @NotNull
    List<Tickable> getTickableElements();

    /**
     * Gets the current tick rate of this element
     *
     * @return the current tick rate
     */
    @NotNull
    UpdateOption getTickRate();

    /**
     * Changes the tick rate of this element.
     *
     * @param updateOption the new tick rate
     */
    void setTickRate(@NotNull UpdateOption updateOption);

    /**
     * Gets the last time this element has been ticked.
     *
     * @return the last tick time in milliseconds
     */
    long getLastTickTime();

}
