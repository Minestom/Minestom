package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

@ApiStatus.Experimental
public interface EntityTracking {

    /**
     * Register an entity to be tracked.
     */
    void register(@NotNull Entity entity, @NotNull Point spawnPoint, @Nullable Update update);

    /**
     * Unregister an entity tracking.
     */
    void unregister(@NotNull Entity entity, @NotNull Point point, @Nullable Update update);

    /**
     * Called every time an entity move, you may want to verify if the new
     * position is in a different chunk.
     */
    void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, @Nullable Update update);

    /**
     * Gets the entities newly visible and invisible from one position to another.
     */
    void difference(@NotNull Point from, @NotNull Point to, @NotNull Update update);

    /**
     * Gets the entities within a range.
     */
    void nearbyEntities(@NotNull Point point, double range, @NotNull Query query);

    /**
     * Gets the entities present in the specified chunk.
     */
    void chunkEntities(@NotNull Point chunkPoint, @NotNull Query query);

    /**
     * Gets the entities present and in range of the specified chunk.
     * <p>
     * This is used for auto-viewable features.
     */
    void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query);

    /**
     * Gets all the entities tracked by this class.
     */
    @UnmodifiableView
    @NotNull Set<@NotNull Entity> entities();

    /**
     * Callback to know the newly visible entities and those to remove.
     */
    interface Update {
        void add(Entity entity);

        void remove(Entity entity);
    }

    /**
     * Query entities.
     * <p>
     * This is not a functional interface, we reserve the right to add other methods.
     */
    interface Query {
        void consume(Entity entity);
    }
}
