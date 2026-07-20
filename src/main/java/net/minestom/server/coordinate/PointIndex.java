package net.minestom.server.coordinate;

import it.unimi.dsi.fastutil.ints.IntCollection;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.function.IntConsumer;

public interface PointIndex {

    void add(int id, Point point);

    /**
     * Removes {@code id}.
     *
     * @return the last known position, or {@code null} if {@code id} was not present
     */
    @Nullable Point remove(int id);

    /**
     * Updates the position of {@code id}.
     *
     * @return the previous position, or {@code null} if {@code id} was not present
     */
    @Nullable Point move(int id, Point newPoint);

    /**
     * @return the current position of {@code id}, or {@code null} if not present
     */
    @Nullable Point get(int id);

    default boolean contains(int id) {
        return get(id) != null;
    }

    int size();

    @Unmodifiable IntCollection all();

    /**
     * Returns the id closest to {@code point}, or {@code -1} if the index is empty.
     */
    int nearest(Point point);

    /**
     * Invokes {@code consumer} for every id within {@code radius} of {@code point}.
     */
    void forEachWithin(Point point, double radius, IntConsumer consumer);

    default int count(Point point, double radius) {
        int[] n = {0};
        forEachWithin(point, radius, id -> n[0]++);
        return n[0];
    }

    void forEachInChunk(int chunkX, int chunkZ, IntConsumer consumer);

    /**
     * Live, unmodifiable view of the ids in chunk {@code (chunkX, chunkZ)}.
     * The collection reflects mutations to the index.
     */
    @Unmodifiable IntCollection inChunk(int chunkX, int chunkZ);

    default void forEachInChunkRange(Point point, int chunkRange, IntConsumer consumer) {
        final int centerX = point.chunkX();
        final int centerZ = point.chunkZ();
        for (int x = centerX - chunkRange; x <= centerX + chunkRange; x++) {
            for (int z = centerZ - chunkRange; z <= centerZ + chunkRange; z++) {
                forEachInChunk(x, z, consumer);
            }
        }
    }

    void forEachInChunkRangeDiffering(Point oldPoint, Point newPoint, int chunkRange,
                                      IntConsumer added, IntConsumer removed);

    static PointIndex create() {
        return new PointIndexImpl();
    }

    static PointIndex createConcurrent() {
        return new PointIndexConcurrentImpl();
    }
}
