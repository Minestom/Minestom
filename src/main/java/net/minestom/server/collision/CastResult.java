package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 *  The result of any {@link Ray#cast}
 */
public interface CastResult {
    /**
     * Returns this {@link CastResult} has a collision or not.
     *
     * @return true if there is any collision, otherwise false
     */
    boolean hasCollision();

    /**
     * Returns the first collision from the proceeding {@link Ray#cast}.
     *
     * @return                        the first collision
     * @throws NoSuchElementException when the cast result has no {@link RayCollision}
     */
    @NotNull RayCollision firstCollision();

    /**
     * Returns the last collision from the proceeding {@link Ray#cast}.
     *
     * @return                        the last collision
     * @throws NoSuchElementException when the cast result has no {@link RayCollision}
     */
    @NotNull RayCollision lastCollision();

    /**
     * Represents a pair of intersection entry & exit points
     * along with their surface normals.
     */
    sealed interface RayCollision permits EntityCastResult.EntityRayCollision, BlockCastResult.BlockRayCollision {
        /**
         * The {@link Point} where the ray entered the bounding box.
         *
         * @return the entry point
         */
        @NotNull Point entry();

        /**
         * The {@link Point} where the ray exited the bounding box.
         *
         * @return the exit point
         */
        @NotNull Point exit();

        /**
         * The surface normal corresponding to the face of the bounding box that
         * the ray entered
         *
         * @throws NoSuchElementException when the {@link Ray} was not configured with
         *                                {@link Ray.Configuration#computeSurfaceNormals()}
         * @return                        the entry surface normal
         */
        @NotNull Vec entrySurfaceNormal();

        /**
         * The surface normal corresponding to the face of the bounding box that
         * the ray exited
         *
         * @throws NoSuchElementException when the {@link Ray} was not configured with
         *                                {@link Ray.Configuration#computeSurfaceNormals()}
         * @return                        the exit surface normal
         */
        @NotNull Vec exitSurfaceNormal();
    }
}
