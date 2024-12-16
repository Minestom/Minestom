package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Holder data structure containing a claim and a chunk.
 *
 * @param chunk      the chunk
 * @param chunkClaim the claim
 */
public record ChunkAndClaim(@NotNull Chunk chunk, @NotNull ChunkClaim chunkClaim) {
}
