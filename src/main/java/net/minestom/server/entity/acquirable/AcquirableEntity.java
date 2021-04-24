package net.minestom.server.entity.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.thread.TickThread;
import net.minestom.server.utils.consumer.EntityConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents an {@link Entity entity} which can be acquired.
 * Used for synchronization purpose.
 */
public class AcquirableEntity {

    private static final ThreadLocal<Stream<Entity>> CURRENT_ENTITIES = ThreadLocal.withInitial(Stream::empty);

    /**
     * Gets all the {@link Entity entities} being ticked in the current thread.
     * <p>
     * Useful when you want to ensure that no acquisition is ever done.
     *
     * @return the entities ticked in the current thread
     */
    public static @NotNull Stream<@NotNull Entity> current() {
        return CURRENT_ENTITIES.get();
    }

    /**
     * Changes the stream returned by {@link #current()}.
     * <p>
     * Mostly for internal use, external calls are unrecommended as they could lead
     * to unexpected behavior.
     *
     * @param entities the new entity stream
     */
    @ApiStatus.Internal
    public static void refresh(@NotNull Stream<@NotNull Entity> entities) {
        CURRENT_ENTITIES.set(entities);
    }

    private final Entity entity;
    private final Handler handler;

    public AcquirableEntity(@NotNull Entity entity) {
        this.entity = entity;
        this.handler = new Handler();
    }

    public @NotNull Acquired<? extends Entity> acquire() {
        final TickThread elementThread = getHandler().getTickThread();
        return new Acquired<>(unwrap(), elementThread);
    }

    /**
     * Unwrap the contained object unsafely.
     * <p>
     * Should only be considered when thread-safety is not necessary (e.g. comparing positions)
     *
     * @return the unwrapped value
     */
    public @NotNull Entity unwrap() {
        return entity;
    }

    /**
     * Gets the {@link Handler} of this acquirable element,
     * containing the currently linked thread.
     * <p>
     * Mostly for internal use.
     *
     * @return this element handler
     */
    @ApiStatus.Internal
    public @NotNull Handler getHandler() {
        return handler;
    }

    public static class Handler {

        private volatile ThreadProvider.ChunkEntry chunkEntry;

        public ThreadProvider.ChunkEntry getChunkEntry() {
            return chunkEntry;
        }

        @ApiStatus.Internal
        public void refreshChunkEntry(@NotNull ThreadProvider.ChunkEntry chunkEntry) {
            this.chunkEntry = chunkEntry;
        }

        public TickThread getTickThread() {
            return chunkEntry != null ? chunkEntry.getThread() : null;
        }
    }

}
