package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Holder data structure containing a claim and a chunk.
 *
 * @param chunkFuture the chunk future which will be completed when the main chunk has finished loading
 * @param chunkClaim  the claim
 */
public record ChunkAndClaim(@NotNull CompletableFuture<@NotNull Chunk> chunkFuture, @NotNull ChunkClaim chunkClaim) {
}
