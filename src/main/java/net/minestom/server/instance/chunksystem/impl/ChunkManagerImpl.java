package net.minestom.server.instance.chunksystem.impl;

import net.minestom.server.instance.chunksystem.ChunkAndClaim;
import net.minestom.server.instance.chunksystem.ChunkClaim;
import net.minestom.server.instance.chunksystem.ChunkManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ChunkManagerImpl implements ChunkManager {
    private int defaultPriority;


    @Override
    public @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius, int priority, @NotNull ChunkClaim.Shape shape) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> removeClaim(int chunkX, int chunkZ, @NotNull ChunkClaim claim) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ) {
        return addClaim(chunkX, chunkZ, 0);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius) {
        return addClaim(chunkX, chunkZ, radius, ChunkClaim.Shape.SQUARE);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius, @NotNull ChunkClaim.Shape shape) {
        return addClaim(chunkX, chunkZ, radius, this.getDefaultPriority(), shape);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius, int priority) {
        return addClaim(chunkX, chunkZ, radius, priority, ChunkClaim.Shape.SQUARE);
    }

    @Override
    public int getDefaultPriority() {
        return this.defaultPriority;
    }

    @Override
    public void setDefaultPriority(int priority) {
        this.defaultPriority = priority;
    }
}
