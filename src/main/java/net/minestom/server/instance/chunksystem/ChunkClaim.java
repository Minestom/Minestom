package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ChunkClaim} is an object that can keep a chunk loaded/load new chunks.
 * {@link ChunkClaim}s are usually specific for a chunk, meaning it is for chunk (X,Z) and only for that chunk.
 * <p>
 * A chunk can have multiple identical {@link ChunkClaim}s, so that {@code claim1.equals(claim2)} equals {@code true}.
 * In case of explicit removal, only one {@link ChunkClaim} may be removed from the chunk (per call).
 * <p>
 * The {@link Chunk} reference is not in this class, but rather in {@link ChunkAndClaim}.
 *
 * @param radius   the radius of this {@link ChunkClaim}. Use 0 for a single chunk.
 * @param priority the priority of this {@link ChunkClaim}. Higher priorities are loaded first.
 */
public record ChunkClaim(int radius, int priority, @NotNull Shape shape) implements Comparable<ChunkClaim> {
    /**
     * Compares two ChunkTickets by their radius in order to quickly access the ChunkTicket with the highest radius from a sorted collection.
     * This will be used mostly by the chunk system to determine which radius to propagate to neighbouring chunks.
     */
    @Override
    public int compareTo(@NotNull ChunkClaim o) {
        return Integer.compare(radius, o.radius);
    }

    /**
     * The shape of a claim. Only matters if radius > 0
     */
    public enum Shape {
        CIRCLE(true) {
            @Override
            public final boolean isInRadius(int radiusSqX, int radiusSqZ, int x, int z, int ox, int oz) {
                var dx = x - ox;
                var dz = z - oz;
                var dxSq = dx * dx;
                var dzSq = dz * dz;
                return (float) dxSq / radiusSqX + (float) dzSq / radiusSqZ <= 1;
            }
        },
        SQUARE(true) {
            @Override
            public final boolean isInRadius(int radiusSqX, int radiusSqZ, int x, int z, int ox, int oz) {
                var dx = Math.abs(x - ox);
                var dz = Math.abs(z - oz);
                var dxSq = dx * dx;
                var dzSq = dz * dz;
                return dxSq <= radiusSqX && dzSq <= radiusSqZ;
            }
        },
        DIAMOND(false) {
            @Override
            public final boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz) {
                var dx = Math.abs(x - ox);
                var dz = Math.abs(z - oz);
                var d = (float) dx / radiusX + (float) dz / radiusZ;
                return d <= 1F;
            }
        };

        private final boolean wantSquared;

        Shape(boolean wantSquared) {
            this.wantSquared = wantSquared;
        }

        @ApiStatus.Internal
        public boolean doesWantSquared() {
            return wantSquared;
        }

        @ApiStatus.Internal
        public abstract boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz);
    }
}
