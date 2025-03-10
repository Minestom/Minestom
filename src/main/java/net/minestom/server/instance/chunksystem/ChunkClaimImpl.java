package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class may not be a record.
 * Instances matter for claims.
 */
@SuppressWarnings("ClassCanBeRecord")
final class ChunkClaimImpl implements ChunkClaim {
    private final int chunkX;
    private final int chunkZ;
    private final int radius;
    private final int priority;
    private final Shape shape;
    private final ClaimCallbacks callbacks;

    ChunkClaimImpl(int chunkX, int chunkZ, int radius, int priority, Shape shape, ClaimCallbacks callbacks) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.radius = radius;
        this.priority = priority;
        this.shape = shape;
        this.callbacks = callbacks;
    }

    @Override
    public int chunkX() {
        return chunkX;
    }

    @Override
    public int chunkZ() {
        return chunkZ;
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

    @Override
    public @Nullable ClaimCallbacks callbacks() {
        return callbacks;
    }
}
