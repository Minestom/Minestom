package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntitySelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines how {@link Entity entities} are tracked within an {@link Instance instance}.
 * <p>
 * Implementations are expected to be thread-safe.
 */
public sealed interface EntityTracker extends EntitySelector.Finder<Entity> permits EntityTrackerImpl {
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

        /**
         * Entity should be visible.
         */
        void add(@NotNull Entity entity);

        /**
         * Entity should be invisible
         */
        void remove(@NotNull Entity entity);

        default void referenceUpdate(@NotNull Point point, @Nullable EntityTracker tracker) {
            // Empty
        }
    }
}
