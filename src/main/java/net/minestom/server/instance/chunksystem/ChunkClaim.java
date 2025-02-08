package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ChunkClaim} is an object that can keep a chunk loaded/load new chunks.
 * {@link ChunkClaim}s are specific for a chunk, meaning it is for chunk (X,Z) and only for that chunk.
 * <p>
 * <b>{@link ChunkClaim} instances must NEVER be created manually.</b>
 * Instead, use the instance returned when adding a claim.
 * <p>
 * A chunk can have multiple identical {@link ChunkClaim}s, so that {@code claim1.equals(claim2)} equals {@code true}.
 * In case of claim removal, only one {@link ChunkClaim} will be removed from the chunk (per call).
 * <p>
 * The {@link Chunk} reference is not in this class, but rather in {@link ChunkAndClaim}.
 *
 * @param radius   the radius of this {@link ChunkClaim}. Use 0 for a single chunk.
 * @param priority the priority of this {@link ChunkClaim}. Higher priorities are loaded first.
 * @param shape    the shape used for this {@link ChunkClaim}. {@link net.minestom.server.entity.Player Players} may want to use {@link Shape#CIRCLE}
 */
public record ChunkClaim(int radius, int priority, @NotNull Shape shape) {
    @ApiStatus.Internal
    public ChunkClaim {
    }

    /**
     * The shape of a claim. Only matters if radius > 0
     */
    public interface Shape {
        @NotNull Shape CIRCLE = new CircleShape();
        @NotNull Shape SQUARE = new SquareShape();
        @NotNull Shape DIAMOND = new DiamondShape();

        boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz);
    }

    private record CircleShape() implements Shape {
        @Override
        public boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz) {
            var radiusSqX = radiusX * radiusX;
            var radiusSqZ = radiusZ * radiusZ;
            var dx = x - ox;
            var dz = z - oz;
            var dxSq = dx * dx;
            var dzSq = dz * dz;
            return (float) dxSq / radiusSqX + (float) dzSq / radiusSqZ <= 1;
        }
    }

    private record SquareShape() implements Shape {
        @Override
        public boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz) {
            var radiusSqX = radiusX * radiusX;
            var radiusSqZ = radiusZ * radiusZ;
            var dx = Math.abs(x - ox);
            var dz = Math.abs(z - oz);
            var dxSq = dx * dx;
            var dzSq = dz * dz;
            return dxSq <= radiusSqX && dzSq <= radiusSqZ;
        }
    }

    private record DiamondShape() implements Shape {
        @Override
        public boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz) {
            var dx = (float) Math.abs(x - ox);
            var dz = (float) Math.abs(z - oz);
            var d = dx / radiusX + dz / radiusZ;
            return d <= 1F;
        }
    }
}
