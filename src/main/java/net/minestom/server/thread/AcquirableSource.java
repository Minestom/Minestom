package net.minestom.server.thread;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An object that is a source of {@link Acquirable} objects, and can be synchronized within a {@link ThreadDispatcher}.
 *
 * @param <T> the type of the acquired object
 */
@ApiStatus.Experimental
public interface AcquirableSource<T> {
    /**
     * Obtains an {@link Acquirable}. To safely perform operations on this object, the user must call
     * {@link Acquirable#sync(Consumer)} or {@link Acquirable#lock()} (followed by
     * a subsequent unlock) on the Acquirable instance.
     *
     * @return an Acquirable which can be used to synchronize access to this object
     */
    @NotNull Acquirable<? extends T> acquirable();
}
