package net.minestom.server.utils.callback;

import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.jetbrains.annotations.Nullable;

/**
 * Convenient class to execute callbacks which can be null.
 */
public class OptionalCallback {

    /**
     * Executes an optional {@link Runnable}.
     *
     * @param callback the optional runnable, can be null
     */
    public static void execute(@Nullable Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Executes an optional {@link ChunkCallback}.
     *
     * @param callback the optional chunk callback, can be null
     * @param chunk    the chunk to forward to the callback
     */
    public static void execute(@Nullable ChunkCallback callback, @Nullable Chunk chunk) {
        if (callback != null) {
            callback.accept(chunk);
        }
    }

}
