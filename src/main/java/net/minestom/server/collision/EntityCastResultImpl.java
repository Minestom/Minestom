package net.minestom.server.collision;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

record EntityCastResultImpl(@NotNull List<EntityRayCollision> entityCollisions) implements EntityCastResult
{
    EntityCastResultImpl {
        entityCollisions = Collections.unmodifiableList(entityCollisions);
    }

    public boolean hasCollision() {
        return !entityCollisions.isEmpty();
    }

    public @NotNull CastResult.RayCollision firstCollision() {
        return entityCollisions.getFirst();
    }

    public @NotNull CastResult.RayCollision lastCollision() {
        return entityCollisions.getLast();
    }

    @Override
    public boolean hasEntityCollision() {
        return !entityCollisions.isEmpty();
    }

    @Override
    public @NotNull EntityRayCollision firstEntityCollision() {
        return entityCollisions.getFirst();
    }

    @Override
    public @NotNull EntityRayCollision lastEntityCollision() {
        return entityCollisions.getLast();
    }
}
