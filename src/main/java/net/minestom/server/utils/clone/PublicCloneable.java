package net.minestom.server.utils.clone;

import org.jetbrains.annotations.NotNull;

/**
 * Convenient interface to expose {@link Object#clone()} publicly with a generic.
 *
 * @param <T> the type to clone
 */
public interface PublicCloneable<T> extends Cloneable {

    @NotNull
    T clone();
}
