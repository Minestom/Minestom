package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *  The result of a {@link Ray#cast(Collection)}
 */
public interface EntityCastResult extends CastResult {
    /**
     * Whether this {@link EntityCastResult} has an entity collision or not
     *
     * @return true if there is an entity collision, otherwise false
     */
    boolean hasEntityCollision();

    /**
     * Returns the first entity collision from the proceeding {@link Ray#cast}
     *
     * @return                        the first entity collision
     * @throws NoSuchElementException when the entity cast result has no {@link EntityRayCollision}
     */
    @NotNull EntityCastResult.EntityRayCollision firstEntityCollision();

    /**
     * Returns the last entity collision from the proceeding {@link Ray#cast}
     *
     * @return                        the last entity collision
     * @throws NoSuchElementException when the entity cast result has no {@link EntityRayCollision}
     */
    @NotNull EntityCastResult.EntityRayCollision lastEntityCollision();

    /**
     * Returns an ordered immutable {@link EntityRayCollision} list from the proceeding {@link Ray#cast}.
     *
     * @return the list of entity collisions
     */
    @NotNull List<EntityRayCollision> entityCollisions();

    /**
     * @param entity the intersected entity
     * @see   CastResult.RayCollision
     */
    record EntityRayCollision(@NotNull Point entry, @NotNull Point exit, @Nullable Vec entrySurfaceNormal,
                              @Nullable Vec exitSurfaceNormal, @NotNull Entity entity) implements RayCollision {
        @Override
        public @NotNull Vec entrySurfaceNormal() {
            if (entrySurfaceNormal == null) {
                throw new NoSuchElementException("Cast ray with Ray.Configuration.computeSurfaceNormals = true");
            }
            return entrySurfaceNormal;
        }

        @Override
        public @NotNull Vec exitSurfaceNormal() {
            if (exitSurfaceNormal == null) {
                throw new NoSuchElementException("Cast ray with Ray.Configuration.computeSurfaceNormals = true");
            }
            return exitSurfaceNormal;
        }
    }
}
