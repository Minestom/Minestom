package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface EntityTracking {

    /**
     * Register an entity to be tracked.
     */
    void register(Entity entity, Point spawnPoint);

    /**
     * Unregister an entity tracking.
     */
    void unregister(Entity entity, Point point);

    /**
     * Called every time an entity move, you may want to verify if the new
     * position is in a different chunk.
     */
    void move(Entity entity, Point oldPoint, Point newPoint, Update update);

    /**
     * Gets the entities newly visible and invisible from one position to another.
     */
    void difference(Point from, Point to, Update update);

    /**
     * Gets the entities within a range.
     */
    void nearbyEntities(Point point, double range, Query query);

    /**
     * Gets the entities present in the specified chunk.
     */
    void chunkEntities(Point chunkPoint, Query query);

    /**
     * Gets the entities present and in range of the specified chunk.
     * <p>
     * This is used for auto-viewable features.
     */
    void chunkRangeEntities(Point chunkPoint, int range, Query query);

    interface Update {
        void add(Entity entity);

        void remove(Entity entity);
    }

    interface Query {
        void consume(Entity entity);
    }
}
