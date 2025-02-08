package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ChunkClaim} is an object that can keep a chunk loaded/load new chunks.
 * {@link ChunkClaim}s are specific for a chunk, meaning it is for chunk (X,Z) and only for that chunk.
 * <p>
 * The {@link Chunk} reference is not in this class, but rather in {@link ChunkAndClaim}.
 */
public sealed interface ChunkClaim permits ChunkClaimImpl {
    /**
     * Get the radius of this claim. 0 is a single chunk,
     * 1 is 3x3 area in shape of {@link #shape()}
     *
     * @return the radius of this claim
     */
    int radius();

    /**
     * Get the priority of this claim.
     * Higher priorities are loaded first.
     *
     * @return the priority of this claim
     */
    int priority();

    /**
     * Get the shape of this claim.
     * {@link net.minestom.server.entity.Player Players} would use {@link Shape#CIRCLE}
     *
     * @return the shape of this claim
     */
    @NotNull Shape shape();

    /**
     * The shape of a claim. Only matters if radius > 0
     */
    interface Shape {
        @NotNull Shape CIRCLE = new CircleShape();
        @NotNull Shape SQUARE = new SquareShape();
        @NotNull Shape DIAMOND = new DiamondShape();

        boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz);
    }

    /**
     * Obtain an instance by {@link Shape#CIRCLE}
     */
    final class CircleShape implements Shape {
        private CircleShape() {
        }

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

    /**
     * Obtain an instance by {@link Shape#SQUARE}
     */
    final class SquareShape implements Shape {
        private SquareShape() {
        }

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

    /**
     * Obtain an instance by {@link Shape#DIAMOND}
     */
    final class DiamondShape implements Shape {
        private DiamondShape() {
        }

        @Override
        public boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz) {
            var dx = (float) Math.abs(x - ox);
            var dz = (float) Math.abs(z - oz);
            var d = dx / radiusX + dz / radiusZ;
            return d <= 1F;
        }
    }
}
