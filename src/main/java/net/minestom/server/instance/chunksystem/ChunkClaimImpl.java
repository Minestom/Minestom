package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.NotNull;

/**
 * This class may not be a record.
 * Instances matter for claims.
 */
@SuppressWarnings("ClassCanBeRecord")
final class ChunkClaimImpl implements ChunkClaim {
    private final int radius;
    private final int priority;
    private final Shape shape;

    ChunkClaimImpl(int radius, int priority, Shape shape) {
        this.radius = radius;
        this.priority = priority;
        this.shape = shape;
    }

    @Override
    public int radius() {
        return radius;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public @NotNull Shape shape() {
        return shape;
    }
}
