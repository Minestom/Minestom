package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.*;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static net.minestom.server.instance.chunksystem.ChunkClaim.Shape;

/**
 * Manager for a claim-based chunk system.
 * Every instance has a separate {@link ChunkManager}
 * {@code Instance#getChunkManager()}
 */
public interface ChunkManager {
    @NotNull Instance getInstance();

    /**
     * The default priority for all chunk loads.
     *
     * @return the default priority
     */
    int getDefaultPriority();

    /**
     * Changes the default priority. This priority is used for loads if no priority is otherwise specified.
     *
     * @param priority the new default priority
     */
    void setDefaultPriority(int priority);

    /**
     * Changes which type of {@link Chunk} implementation to use once one needs to be loaded.
     * <p>
     * Uses {@link DynamicChunk} by default.
     * <p>
     * WARNING: if you need to save this instance's chunks later,
     * the code needs to be predictable for {@link IChunkLoader#loadChunk(Instance, int, int)}
     * to create the correct type of {@link Chunk}. tl;dr: Need chunk save = no random type.
     *
     * @param supplier the new {@link ChunkSupplier} of this instance, chunks need to be non-null
     * @throws NullPointerException if {@code chunkSupplier} is null
     */
    void setChunkSupplier(@NotNull ChunkSupplier supplier);

    /**
     * Get the chunk supplier.
     * <p>
     * Uses {@link DynamicChunk} by default.
     *
     * @return the chunk supplier
     */
    @NotNull ChunkSupplier getChunkSupplier();

    /**
     * Changes the generator used for newly generated chunks.
     *
     * @param generator the new generator to use
     */
    void setGenerator(@Nullable Generator generator);

    /**
     * Gets the generator used for newly generated chunks
     *
     * @return the active generator, null if no generator is set
     */
    @Nullable Generator getGenerator();

    /**
     * Gets the current {@link PriorityDrop} function for this {@link ChunkManager}
     *
     * @return the priority drop function
     */
    @ApiStatus.Experimental
    @NotNull PriorityDrop getPriorityDrop();

    /**
     * Change the current {@link PriorityDrop} function for this {@link ChunkManager}.
     * The priority drop is used to determine the order in which chunks are loaded.
     *
     * @param priorityDrop the new priority drop function
     * @see PriorityDrop
     */
    @ApiStatus.Experimental
    void setPriorityDrop(@NotNull PriorityDrop priorityDrop);

    /**
     * @return whether autosave is enabled
     * @see #setAutosaveEnabled(boolean)
     */
    boolean isAutosaveEnabled();

    /**
     * Change whether the {@link ChunkManager} should auto-save unloaded chunks and instance data to storage.
     * This removes the necessity of calling {@link #saveInstanceDataAndChunks()}.
     * This setting should always be preferred over {@link #saveChunk(Chunk)} and similar methods.
     *
     * @param autosaveEnabled whether autosave is enabled
     */
    void setAutosaveEnabled(boolean autosaveEnabled);

    /**
     * Gets the loaded {@link Chunk} at a position.
     * <p>
     * WARNING: this should only return already-loaded chunk, use {@link #addClaim(int, int)} or similar to load one instead.
     * <p>
     * WARNING: the returned chunk can be unloaded as soon as this call returns. The better approach is to use {@link #addClaim(int, int)} to get a chunk.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk at the specified position, null if not loaded
     */
    @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ);

    /**
     * Get the currently loaded chunks. This is only ever updated in the instance's tick thread.
     *
     * @return the currently loaded chunks
     */
    @UnmodifiableView
    @NotNull Collection<@NotNull Chunk> getLoadedChunks();

    /**
     * @see #addClaim(int, int)
     */
    default @NotNull ChunkAndClaim addClaim(@NotNull Point point) {
        return addClaim(point.chunkX(), point.chunkZ());
    }

    /**
     * @see #addClaim(int, int, int)
     */
    default @NotNull ChunkAndClaim addClaim(@NotNull Point point, int radius) {
        return addClaim(point.chunkX(), point.chunkZ(), radius);
    }

    /**
     * @see #addClaim(int, int, int, Shape)
     */
    default @NotNull ChunkAndClaim addClaim(@NotNull Point point, int radius, @NotNull Shape shape) {
        return addClaim(point.chunkX(), point.chunkZ(), radius, shape);
    }

    /**
     * @see #addClaim(int, int, int, int)
     */
    default @NotNull ChunkAndClaim addClaim(@NotNull Point point, int radius, int priority) {
        return addClaim(point.chunkX(), point.chunkZ(), radius, priority);
    }

    /**
     * @see #addClaim(int, int, int, int, Shape)
     */
    default @NotNull ChunkAndClaim addClaim(@NotNull Point point, int radius, int priority, @NotNull Shape shape) {
        return addClaim(point.chunkX(), point.chunkZ(), radius, priority, shape);
    }

    /**
     * @see #addClaim(int, int, int, int, Shape, ClaimCallbacks)
     */
    default @NotNull ChunkAndClaim addClaim(@NotNull Point point, int radius, int priority, @NotNull Shape shape, @Nullable ClaimCallbacks callbacks) {
        return addClaim(point.chunkX(), point.chunkZ(), radius, priority, shape, callbacks);
    }

    /**
     * Adds a claim to a chunk. The claim will have radius 0 (single-chunk)
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     * This method uses a default shape of {@link Shape#SQUARE}
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @return the {@link ChunkAndClaim} used to remove the claim.
     */
    @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     * This method uses a default shape of {@link Shape#SQUARE}
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param radius the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     * @return the {@link ChunkAndClaim} used to remove the claim.
     */
    @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param shape  the shape of the claim. Only matters with radius >= 1
     * @param radius the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     * @return the {@link ChunkAndClaim} used to remove the claim.
     */
    @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, @NotNull Shape shape);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     * This method uses a default shape of {@link Shape#SQUARE}
     *
     * @param chunkX   the chunk X, in chunk coordinate space
     * @param chunkZ   the chunk Z, in chunk coordinate space
     * @param radius   the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     * @param priority the priority of the claim. Higher priorities get processed before lower priorities.
     * @return the {@link ChunkAndClaim} used to remove the claim.
     */
    @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     * <p>
     * Same as {@link #addClaim(int, int, int, int, Shape, ClaimCallbacks) addClaim(chunkX, chunkZ, radius, priority, shape, null)}
     *
     * @param chunkX   the chunk X, in chunk coordinate space
     * @param chunkZ   the chunk Z, in chunk coordinate space
     * @param radius   the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     * @param priority the priority of the claim. Higher priorities get processed before lower priorities.
     * @param shape    the shape of the claim. Only matters with radius >= 1
     * @return the {@link ChunkAndClaim} used to remove the claim.
     */
    @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority, @NotNull Shape shape);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     *
     * @param chunkX    the chunk X, in chunk coordinate space
     * @param chunkZ    the chunk Z, in chunk coordinate space
     * @param radius    the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     * @param priority  the priority of the claim. Higher priorities get processed before lower priorities.
     * @param shape     the shape of the claim. Only matters with radius >= 1
     * @param callbacks the callbacks to use for this claim
     * @return the {@link ChunkAndClaim} used to remove the claim.
     */
    @NotNull ChunkAndClaim addClaim(int chunkX, int chunkZ, int radius, int priority, @NotNull Shape shape, @Nullable ClaimCallbacks callbacks);

    /**
     * Removes a claim from a chunk.
     * <p>
     * The passed {@link ChunkClaim} must be the exact same object instance returned when the claim was created.
     * Even though {@link ChunkClaim} is a record, the object instance matters to identify the exact claim.
     *
     * @param claim the {@link ChunkClaim} that should be removed
     * @return a future for when the claim was removed.
     */
    @NotNull CompletableFuture<Void> removeClaim(@NotNull ChunkClaim claim);

    /**
     * Saves the current instance tags
     * <p>
     * Warning: only the global instance data will be saved, not chunks.
     * You would need to call {@link #saveChunks()} too, or instead opt for {@link #saveInstanceDataAndChunks()}
     *
     * @return the future called once the instance data has been saved
     * @deprecated try to use {@link #setAutosaveEnabled(boolean)} instead, if possible.
     * If not, please state your use case and open an issue on GitHub
     */
    @Deprecated
    @NotNull CompletableFuture<Void> saveInstanceData();

    /**
     * Saves a {@link Chunk} to storage.
     *
     * @param chunk the {@link Chunk} to save
     * @return future called when chunk is done saving
     * @deprecated try to use {@link #setAutosaveEnabled(boolean)} instead, if possible.
     * If not, please state your use case and open an issue on GitHub
     */
    @Deprecated
    @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk);

    /**
     * Saves all loaded chunks to storage.
     *
     * @return future called once all chunks have been saved
     * @see #saveInstanceDataAndChunks()
     * @deprecated try to use {@link #setAutosaveEnabled(boolean)} instead, if possible.
     * If not, please state your use case and open an issue on GitHub
     */
    @Deprecated
    @NotNull CompletableFuture<Void> saveChunks();

    /**
     * Saves the instance data and all chunks to storage
     *
     * @return future called when instance data and chunks have been saved
     * @see #saveInstanceData()
     * @see #saveChunks()
     * @deprecated try to use {@link #setAutosaveEnabled(boolean)} instead, if possible.
     * If not, please state your use case and open an issue on GitHub
     */
    @Deprecated
    @NotNull CompletableFuture<Void> saveInstanceDataAndChunks();


    /**
     * Changes the {@link IChunkLoader} of this chunk manager (to change how chunks are retrieved when not already loaded).
     *
     * <p>{@link IChunkLoader#noop()} can be used to do nothing.</p>
     *
     * @param chunkLoader the new {@link IChunkLoader}
     */
    void setChunkLoader(@NotNull IChunkLoader chunkLoader);

    /**
     * Gets the {@link IChunkLoader} of this chunk manager.
     *
     * @return the {@link IChunkLoader} of this chunk manager
     */
    @NotNull IChunkLoader getChunkLoader();

    /**
     * Copies this {@link ChunkManager} with a claim per loaded chunk.
     * Every claim will have radius 0 and shape {@link ChunkClaim.Shape#SQUARE SQUARE}.
     * <p>
     * The copy will preserve the {@link Generator}, {@link PriorityDrop}, {@link #getDefaultPriority()},
     * {@link #getChunkSupplier()} and {@link #isAutosaveEnabled()}, but not the {@link IChunkLoader}.
     * A new {@link IChunkLoader} should be set, if that is required.
     *
     * @param targetInstance the instance for the newly copied chunk manager
     * @return a pair of the copy and claims
     */
    @ApiStatus.Experimental
    @NotNull Pair<ChunkManager, Collection<ChunkAndClaim>> singleClaimCopy(@NotNull Instance targetInstance);

    /**
     * Allows creating a {@link ChunkManager} for any generic instance.
     * <p>
     * Only one {@link ChunkManager} may ever be created for an {@link Instance}.
     * Using multiple for the same {@link Instance}, will result in undefined behaviour.
     *
     * @param instance      the instance to create the ChunkManager for
     * @param chunkSupplier a {@link ChunkSupplier} to use, null to use {@link DynamicChunk#DynamicChunk(Instance, int, int) DynamicChunk::new}
     * @param chunkLoader   a {@link IChunkLoader} to use, null to use {@link IChunkLoader#noop()}
     * @return a {@link ChunkManager} for that instance
     */
    static @NotNull ChunkManager createFor(@NotNull Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable IChunkLoader chunkLoader) {
        return new ChunkManagerImpl(instance, chunkSupplier, chunkLoader, ChunkSystemChunkAccessImpl.INSTANCE);
    }
}
