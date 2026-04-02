package net.minestom.server.instance.chunksystem;

import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.instance.Chunk;

import java.util.concurrent.CompletableFuture;

/**
 * Holder data structure containing a claim and a chunk.
 *
 * @param chunkFuture the chunk future which will be completed when the main chunk has finished loading.
 *                    <b>This future is not completed on the tick thread!</b>
 *                    The reason behind this is to allow {@link CompletableFuture#join()} calls on the tick thread.
 *                    This also implies that {@link InstanceChunkLoadEvent}s will be called after the {@link CompletableFuture#join()}
 *                    call returns. Use this carefully, and consider the implications!
 * @param claim  the claim
 */
public record ChunkAndClaim(CompletableFuture<Chunk> chunkFuture, ChunkClaim claim) {
}
