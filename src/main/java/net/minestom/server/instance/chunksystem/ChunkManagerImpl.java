package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.chunksystem.ChunkClaim.Shape;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

class ChunkManagerImpl implements ChunkManager {
    private final Instance instance;
    private final TaskSchedulerThread taskSchedulerThread;
    private int defaultPriority;

    public ChunkManagerImpl(Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable ChunkLoader chunkLoader) {
        this.instance = instance;
        this.taskSchedulerThread = new TaskSchedulerThread(instance, chunkSupplier, chunkLoader);
    }

    @Override
    public @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return taskSchedulerThread.getLoadedChunk(chunkX, chunkZ);
    }

    @Override
    public @Nullable Chunk getLoadedChunkManaged(int chunkX, int chunkZ) {
        return taskSchedulerThread.getLoadedChunkManaged(chunkX, chunkZ);
    }

    @Override
    public @UnmodifiableView Collection<Chunk> getLoadedChunks() {
        return taskSchedulerThread.getLoadedChunks();
    }

    @Override
    public @UnmodifiableView Collection<Chunk> getLoadedChunksManaged() {
        return taskSchedulerThread.getLoadedChunksManaged();
    }

    @Override
    public ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority, Shape shape, @Nullable ClaimCallbacks callbacks) {
        var chunkAndClaim = new ChunkAndClaim(new CompletableFuture<>(), new ChunkClaimImpl(chunkX, chunkZ, radius, priority, shape, callbacks));
        this.taskSchedulerThread.addClaimAsync(chunkAndClaim);
        return chunkAndClaim;
    }

    @Override
    public CompletableFuture<Void> removeClaim(ChunkClaim claim) {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.removeClaimAsync(claim, future);
        return future;
    }

    @Override
    public CompletableFuture<Void> saveInstanceData() {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveInstanceDataAsync(future);
        return future;
    }

    @Override
    public CompletableFuture<Void> saveChunk(Chunk chunk) {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveChunkAsync(chunk, future);
        return future;
    }

    @Override
    public CompletableFuture<Void> saveChunks() {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveChunksAsync(future);
        return future;
    }

    @Override
    public CompletableFuture<Void> saveInstanceDataAndChunks() {
        var future = new CompletableFuture<Void>();
        this.taskSchedulerThread.saveInstanceDataAndChunksAsync(future);
        return future;
    }

    @Override
    public void setChunkLoader(ChunkLoader chunkLoader) {
        this.taskSchedulerThread.setChunkLoader(Objects.requireNonNull(chunkLoader, "Chunk loader cannot be null"));
    }

    @Override
    public ChunkLoader getChunkLoader() {
        return this.taskSchedulerThread.getChunkLoader();
    }

    @Override
    public Pair<ChunkManager, Collection<ChunkAndClaim>> singleClaimCopy(Instance targetInstance) {
        var copy = baseCopy(targetInstance);
        var claims = this.taskSchedulerThread.singleClaimCopy(copy.taskSchedulerThread, copy.getDefaultPriority());
        return Pair.of(copy, claims);
    }

    private ChunkManagerImpl baseCopy(Instance targetInstance) {
        var chunkManager = new ChunkManagerImpl(targetInstance, getChunkSupplier(), null);
        chunkManager.setGenerator(getGenerator());
        chunkManager.setAutosaveEnabled(isAutosaveEnabled());
        chunkManager.setDefaultPriority(getDefaultPriority());
        chunkManager.setPriorityDrop(getPriorityDrop());
        return chunkManager;
    }

    @Override
    public ChunkAndClaim addClaim(int chunkX, int chunkZ) {
        return addClaim(chunkX, chunkZ, 0);
    }

    @Override
    public ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius) {
        return addClaim(chunkX, chunkZ, radius, Shape.SQUARE);
    }

    @Override
    public ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, Shape shape) {
        return addClaim(chunkX, chunkZ, radius, this.getDefaultPriority(), shape);
    }

    @Override
    public ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority) {
        return addClaim(chunkX, chunkZ, radius, priority, Shape.SQUARE);
    }

    @Override
    public ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority, Shape shape) {
        return addClaim(chunkX, chunkZ, radius, priority, shape, null);
    }

    @Override
    public Instance getInstance() {
        return instance;
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
    public void setChunkSupplier(ChunkSupplier supplier) {
        this.taskSchedulerThread.setChunkSupplier(Objects.requireNonNull(supplier));
    }

    @Override
    public ChunkSupplier getChunkSupplier() {
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
    public PriorityDrop getPriorityDrop() {
        return this.taskSchedulerThread.getPriorityDrop();
    }

    @Override
    public void setPriorityDrop(PriorityDrop priorityDrop) {
        this.taskSchedulerThread.setPriorityDrop(priorityDrop);
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
