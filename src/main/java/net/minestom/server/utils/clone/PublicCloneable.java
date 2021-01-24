package net.minestom.server.utils.clone;

import org.jetbrains.annotations.NotNull;

/**
 * Convenient interface to expose {@link Object#clone()} publicly with a generic.
 *
 * @param <T> the type to clone
 */
public interface PublicCloneable<T> extends Cloneable {

    /**
     * Creates and returns a copy of this object.
     *
     * @return A clone of this instance.
     */
    @NotNull
    T clone();
}
