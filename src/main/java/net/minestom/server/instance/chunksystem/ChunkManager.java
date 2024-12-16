package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.minestom.server.instance.chunksystem.ChunkClaim.Shape;

/**
 * Manager for a claim-based chunk system.
 * Every instance has a separate {@link ChunkManager}
 * {@code Instance#getChunkManager()}
 */
public interface ChunkManager {
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
     * Adds a claim to a chunk. The claim will have radius 0 (single-chunk)
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     * This method uses a default shape of {@link Shape#SQUARE}
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     */
    @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     * This method uses a default shape of {@link Shape#SQUARE}
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param radius the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     */
    @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param shape  the shape of the claim. Only matters with radius >= 1
     * @param radius the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     */
    @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius, @NotNull Shape shape);

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
     * @return a future for when the claim has been added successfully
     */
    @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius, int priority);

    /**
     * Adds a claim to a chunk.
     * Adding a claim can take an undefined period of time, chunk generation might have to happen first.
     * Claims added with this method will make the chunk fully generate.
     *
     * @param chunkX   the chunk X, in chunk coordinate space
     * @param chunkZ   the chunk Z, in chunk coordinate space
     * @param radius   the radius of this {@link ChunkClaim}. Use 0 to only load a single chunk.
     * @param priority the priority of the claim. Higher priorities get processed before lower priorities.
     * @param shape    the shape of the claim. Only matters with radius >= 1
     * @return a future for when the claim has been added successfully
     */
    @NotNull CompletableFuture<@NotNull ChunkAndClaim> addClaim(int chunkX, int chunkZ, int radius, int priority, @NotNull Shape shape);

    /**
     * Removes a claim from a chunk.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param claim  the {@link ChunkClaim} that should be removed
     * @return a future for when the claim was removed.
     * @implNote ideally the claim is removed as soon as the method returns (the future is already completed), this is not a requirement though.
     */
    @NotNull CompletableFuture<Void> removeClaim(int chunkX, int chunkZ, @NotNull ChunkClaim claim);
}
