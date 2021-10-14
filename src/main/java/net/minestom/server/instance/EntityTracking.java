package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

import static net.minestom.server.utils.chunk.ChunkUtils.forChunksInRange;

/**
 * Defines how {@link Entity entities} are tracked within an {@link Instance instance}.
 * <p>
 * Implementations are expected to be thread-safe, {@link #synchronize(EntityTracking)} can be used to synchronize every call.
 */
@ApiStatus.Experimental
public interface EntityTracking {

    static @NotNull EntityTracking synchronize(@NotNull EntityTracking entityTracking) {
        return entityTracking instanceof EntityTrackingImpl.Synchronized ?
                entityTracking : new EntityTrackingImpl.Synchronized(entityTracking);
    }

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
     * Gets the entities present in the specified chunk.
     */
    void chunkEntities(int chunkX, int chunkZ, @NotNull Query query);

    default void chunkEntities(@NotNull Point point, @NotNull Query query) {
        chunkEntities(point.chunkX(), point.chunkZ(), query);
    }

    /**
     * Gets the entities present and in range of the specified chunk.
     * <p>
     * This is used for auto-viewable features.
     */
    default void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query) {
        forChunksInRange(chunkPoint, range, (chunkX, chunkZ) -> chunkEntities(chunkX, chunkZ, query));
    }

    /**
     * Gets the entities within a range.
     */
    default void nearbyEntities(@NotNull Point point, double range, @NotNull Query query) {
        final int chunkRange = Math.abs((int) (range / Chunk.CHUNK_SECTION_SIZE)) + 1;
        final double squaredRange = range * range;
        ChunkUtils.forChunksInRange(point, chunkRange, (chunkX, chunkZ) ->
                chunkEntities(chunkX, chunkZ, entity -> {
                    if (point.distanceSquared(entity.getPosition()) < squaredRange) {
                        query.consume(entity);
                    }
                }));
    }

    /**
     * Gets all the entities tracked by this class.
     */
    @UnmodifiableView
    @NotNull Set<@NotNull Entity> entities();

    /**
     * Gets all the players tracked by this class.
     */
    @UnmodifiableView
    @NotNull Set<@NotNull Player> players();

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
