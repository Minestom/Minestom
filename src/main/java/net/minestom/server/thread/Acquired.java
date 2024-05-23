package net.minestom.server.thread;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that has been safely acquired and can be freed again.
 * <p>
 * This class should not be shared, and it is recommended to call {@link #unlock()}
 * once the acquisition goal has been fulfilled to limit blocking time.
 *
 * @param <T> the type of the acquired object
 */
public sealed interface Acquired<T> permits AcquiredImpl {
    @NotNull T get();

    void unlock();
}
