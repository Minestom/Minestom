package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ChunkClaim} is an object that can keep a chunk loaded/load new chunks.
 * {@link ChunkClaim}s are specific for a chunk, meaning it is for chunk (X,Z) and only for that chunk.
 * <p>
 * The {@link Chunk} reference is not in this class, but rather in {@link ChunkAndClaim}.
 */
public sealed interface ChunkClaim permits ChunkClaimImpl {
    /**
     * Get the x coordinate of the chunk this claim originates from
     *
     * @return the x coordinate
     */
    int chunkX();

    /**
     * Get the z coordinate of the chunk this claim originates from
     *
     * @return the z coordinate
     */
    int chunkZ();

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
     * Get the callbacks for this claim. Null if no callbacks were specified
     *
     * @return the callbacks
     */
    @Nullable ClaimCallbacks callbacks();

    /**
     * Check if given chunk coordinates contained in this claim.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return if the coordinates are contained in this claim
     */
    default boolean contains(int chunkX, int chunkZ) {
        return shape().isInRadius(this, chunkX, chunkZ);
    }

    /**
     * The shape of a claim. Only matters if radius > 0.
     * <p>
     * Must return true for radius = 0
     */
    @FunctionalInterface
    interface Shape {
        @NotNull Shape CIRCLE = (int radiusX, int radiusZ, int x, int z, int ox, int oz) -> {
            var radiusSqX = radiusX * radiusX;
            var radiusSqZ = radiusZ * radiusZ;
            var dx = x - ox;
            var dz = z - oz;
            var dxSq = dx * dx;
            var dzSq = dz * dz;
            return (float) dxSq / radiusSqX + (float) dzSq / radiusSqZ <= 1;
        };
        @NotNull Shape SQUARE = (int radiusX, int radiusZ, int x, int z, int ox, int oz) -> {
            var dx = Math.abs(x - ox);
            var dz = Math.abs(z - oz);
            return dx <= radiusX && dz <= radiusZ;
        };
        @NotNull Shape DIAMOND = (int radiusX, int radiusZ, int x, int z, int ox, int oz) -> {
            var dx = (float) Math.abs(x - ox);
            var dz = (float) Math.abs(z - oz);
            var d = dx / radiusX + dz / radiusZ;
            return d <= 1F;
        };

        default boolean isInRadius(@NotNull ChunkClaim claim, int x, int z) {
            return isInRadius(claim.radius(), claim.radius(), claim.chunkX(), claim.chunkZ(), x, z);
        }

        boolean isInRadius(int radiusX, int radiusZ, int x, int z, int ox, int oz);
    }
}
