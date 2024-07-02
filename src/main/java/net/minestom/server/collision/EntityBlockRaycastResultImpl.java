package net.minestom.server.collision;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

record EntityBlockRaycastResultImpl(@NotNull List<EntityRayCollision> entityCollisions, @NotNull List<BlockRayCollision> blockCollisions,
                                    @NotNull List<RayCollision> collisions) implements EntityBlockCastResult {
    EntityBlockRaycastResultImpl {
        blockCollisions = Collections.unmodifiableList(blockCollisions);
        entityCollisions = Collections.unmodifiableList(entityCollisions);
        collisions = Collections.unmodifiableList(collisions);
    }

    @Override
    public @NotNull List<EntityCastResult.EntityRayCollision> findEntitiesBeforeBlockCollision(int collisionThreshold) {
        return RaycastUtils.findEntitiesBeforeEntityCollision(collisions, collisionThreshold);
    }

    @Override
    public @NotNull List<EntityCastResult.EntityRayCollision> findEntitiesBeforeBlockCollision() {
        return findEntitiesBeforeBlockCollision(1);
    }

    @Override
    public @NotNull List<BlockCastResult.BlockRayCollision> findBlocksBeforeEntityCollision(int collisionThreshold) {
        return RaycastUtils.findBlocksBeforeEntityCollision(collisions, collisionThreshold);
    }

    @Override
    public @NotNull List<BlockCastResult.BlockRayCollision> findBlocksBeforeEntityCollision() {
        return findBlocksBeforeEntityCollision(1);
    }

    @Override
    public boolean hasCollision() {
        return !collisions.isEmpty();
    }

    @Override
    public @NotNull CastResult.RayCollision firstCollision() {
        return collisions.getFirst();
    }

    @Override
    public @NotNull CastResult.RayCollision lastCollision() {
        return collisions.getLast();
    }

    @Override
    public boolean hasEntityCollision() {
        return !entityCollisions.isEmpty();
    }

    @Override
    public @NotNull EntityCastResult.EntityRayCollision firstEntityCollision() {
        return entityCollisions.getFirst();
    }

    @Override
    public @NotNull EntityCastResult.EntityRayCollision lastEntityCollision() {
        return entityCollisions.getLast();
    }

    @Override
    public boolean hasBlockCollision() {
        return !blockCollisions.isEmpty();
    }

    @Override
    public @NotNull BlockRayCollision firstBlockCollision() {
        return blockCollisions.getFirst();
    }

    @Override
    public @NotNull BlockRayCollision lastBlockCollision() {
        return blockCollisions.getLast();
    }
}
