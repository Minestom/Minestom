package net.minestom.server.lock;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an element that have a {@link Acquirable} linked to it.
 * <p>
 * Useful if you want to provide an access point to an object without risking to compromise
 * the thread-safety of your code.
 */
public interface LockedElement {

    /**
     * Gets the {@link Acquirable} of this locked element.
     * <p>
     * Should be a constant.
     *
     * @return the acquirable element linked to this object
     */
    <T> @NotNull Acquirable<T> getAcquiredElement();

}