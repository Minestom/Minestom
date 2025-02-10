package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

class ChunkManagerImpl implements ChunkManager {
    private final TaskSchedulerThread taskSchedulerThread;
    private int defaultPriority;

    public ChunkManagerImpl(@NotNull Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable IChunkLoader chunkLoader, @NotNull ChunkAccess chunkAccess) {
        this.taskSchedulerThread = new TaskSchedulerThread(instance, chunkSupplier, chunkLoader, chunkAccess);
    }

    @Override
    public @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return taskSchedulerThread.getLoadedChunk(chunkX, chunkZ);
    }

    @Override
    public @UnmodifiableView @NotNull Collection<@NotNull Chunk> getLoadedChunks() {
        return taskSchedulerThread.getLoadedChunks();
    }

    @Override
    public @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority, @NotNull ChunkClaim.Shape shape) {
        var chunkAndClaim = new ChunkAndClaim(new CompletableFuture<>(), new ChunkClaimImpl(radius, priority, shape));
        this.taskSchedulerThread.addClaimAsync(chunkX, chunkZ, chunkAndClaim);
        return chunkAndClaim;
    }

    @Override
    public @NotNull CompletableFuture<Void> removeClaim(@NotNull ChunkClaim claim) {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.removeClaimAsync(claim, future);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstanceData() {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveInstanceDataAsync(future);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveChunkAsync(chunk, future);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunks() {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveChunksAsync(future);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstanceDataAndChunks() {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveInstanceDataAndChunksAsync(future);
        return future;
    }

    @Override
    public void setChunkLoader(@NotNull IChunkLoader chunkLoader) {
        this.taskSchedulerThread.setChunkLoader(Objects.requireNonNull(chunkLoader, "Chunk loader cannot be null"));
    }

    @Override
    public @NotNull IChunkLoader getChunkLoader() {
        return this.taskSchedulerThread.getChunkLoader();
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
    public void setChunkSupplier(@NotNull ChunkSupplier supplier) {
        this.taskSchedulerThread.setChunkSupplier(supplier);
    }

    @Override
    public @NotNull ChunkSupplier getChunkSupplier() {
        return this.taskSchedulerThread.getChunkSupplier();
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        this.taskSchedulerThread.setGenerator(generator);
    }

    @Override
    public @Nullable Generator getGenerator() {
        return this.taskSchedulerThread.getGenerator();
    }

    @Override
    public boolean isAutosaveEnabled() {
        return this.taskSchedulerThread.isAutosaveEnabled();
    }

    @Override
    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.taskSchedulerThread.setAutosaveEnabled(autosaveEnabled);
    }
}
