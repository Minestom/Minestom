package net.minestom.server.instance.generator.units;

import net.minestom.server.instance.Chunk;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record ChunkGenerationResponse(List<CompletableFuture<Chunk>> futures) implements GenerationResponse<Chunk> {
}
