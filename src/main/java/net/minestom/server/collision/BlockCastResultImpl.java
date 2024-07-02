package net.minestom.server.collision;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

record BlockCastResultImpl(@NotNull List<BlockRayCollision> blockCollisions) implements BlockCastResult
{
    BlockCastResultImpl {
        blockCollisions = Collections.unmodifiableList(blockCollisions);
    }

    public boolean hasCollision() {
        return !blockCollisions.isEmpty();
    }

    public @NotNull CastResult.RayCollision firstCollision() {
        return blockCollisions.getFirst();
    }

    public @NotNull CastResult.RayCollision lastCollision() {
        return blockCollisions.getLast();
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
