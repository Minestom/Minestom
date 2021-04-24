package net.minestom.server.acquirable;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

class AcquirableImpl<T> implements Acquirable<T> {

    protected static final ThreadLocal<Stream<Entity>> CURRENT_ENTITIES = ThreadLocal.withInitial(Stream::empty);

    /**
     * Changes the stream returned by {@link #currentEntities()}.
     * <p>
     * Mostly for internal use, external calls are unrecommended as they could lead
     * to unexpected behavior.
     *
     * @param entities the new entity stream
     */
    @ApiStatus.Internal
    static void refreshEntities(@NotNull Stream<@NotNull Entity> entities) {
        AcquirableImpl.CURRENT_ENTITIES.set(entities);
    }

    private final T value;
    private final Acquirable.Handler handler;

    public AcquirableImpl(@NotNull T value) {
        this.value = value;
        this.handler = new Acquirable.Handler();
    }

    @Override
    public @NotNull T unwrap() {
        return value;
    }

    @Override
    public @NotNull Acquirable.Handler getHandler() {
        return handler;
    }
}
