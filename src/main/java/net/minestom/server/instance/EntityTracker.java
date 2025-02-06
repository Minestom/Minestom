package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines how {@link Entity entities} are tracked within an {@link Instance instance}.
 * <p>
 * Implementations are expected to be thread-safe.
 */
public sealed interface EntityTracker extends EntityQuery.Finder permits EntityTrackerImpl {
    static @NotNull EntityTracker newTracker() {
        return new EntityTrackerImpl();
    }

    void register(@NotNull Entity entity, @NotNull Point point, @Nullable Update update);

    void unregister(@NotNull Entity entity, @Nullable Update update);

    void move(@NotNull Entity entity, @NotNull Point newPoint, @Nullable Update update);

    /**
     * Callback to know the newly visible entities and those to remove.
     */
    interface Update {
        void add(@NotNull Entity entity);

        void remove(@NotNull Entity entity);

        default void referenceUpdate(@NotNull Point point, @Nullable EntityTracker tracker) {
            // Empty
        }
    }
}
