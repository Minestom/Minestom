package net.minestom.server.instance.chunksystem.impl;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.chunksystem.ChunkAndClaim;
import net.minestom.server.instance.chunksystem.ChunkClaim;
import net.minestom.server.instance.chunksystem.ChunkManager;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public class ChunkManagerImpl implements ChunkManager {
    private final ChunkClaimManager chunkClaimManager;
    private int defaultPriority;

    public ChunkManagerImpl(@NotNull Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable IChunkLoader chunkLoader, @NotNull ChunkAccess chunkAccess) {
        this.chunkClaimManager = new ChunkClaimManager(instance, chunkSupplier, chunkLoader, chunkAccess);
    }

    @Override
    public @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return chunkClaimManager.getLoadedChunk(chunkX, chunkZ);
    }

    @Override
    public @UnmodifiableView @NotNull Collection<@NotNull Chunk> getLoadedChunks() {
        return chunkClaimManager.getLoadedChunks();
    }

    @Override
    public @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority, @NotNull ChunkClaim.Shape shape) {
        var chunkAndClaim = new ChunkAndClaim(new CompletableFuture<>(), new ChunkClaim(radius, priority, shape));
        this.chunkClaimManager.addClaim(chunkX, chunkZ, chunkAndClaim);
        return chunkAndClaim;
    }

    @Override
    public @NotNull CompletableFuture<Void> removeClaim(@NotNull ChunkClaim claim) {
        var future = new CompletableFuture<Void>();
        this.chunkClaimManager.removeClaim(claim, future);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstanceData() {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunks() {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstanceDataAndChunks() {
        return null;
    }

    @Override
    public void setChunkLoader(@NotNull IChunkLoader chunkLoader) {
        this.chunkClaimManager.setChunkLoader(Objects.requireNonNull(chunkLoader, "Chunk loader cannot be null"));
    }

    @Override
    public @NotNull IChunkLoader getChunkLoader() {
        return this.chunkClaimManager.getChunkLoader();
    }

    @Override
    public @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ) {
        return addClaim(chunkX, chunkZ, 0);
    }

    @Override
    public @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius) {
        return addClaim(chunkX, chunkZ, radius, ChunkClaim.Shape.SQUARE);
    }

    @Override
    public @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, @NotNull ChunkClaim.Shape shape) {
        return addClaim(chunkX, chunkZ, radius, this.getDefaultPriority(), shape);
    }

    @Override
    public @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority) {
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

    @Override
    public boolean isAutosaveEnabled() {
        return this.chunkClaimManager.isAutosaveEnabled();
    }

    @Override
    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.chunkClaimManager.setAutosaveEnabled(autosaveEnabled);
    }
}
