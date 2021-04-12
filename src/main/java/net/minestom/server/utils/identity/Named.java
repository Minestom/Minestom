package net.minestom.server.utils.identity;

import org.jetbrains.annotations.NotNull;

/**
 * An object with a name.
 *
 * @param <T> the type of the name
 */
@FunctionalInterface
public interface Named<T> {

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    @NotNull T getName();
}
