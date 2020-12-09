package net.minestom.server.utils.clone;

/**
 * Convenient interface to expose {@link Object#clone()} publicly with a generic.
 *
 * @param <T> the type to clone
 */
public interface PublicCloneable<T> extends Cloneable {
    T clone();
}
