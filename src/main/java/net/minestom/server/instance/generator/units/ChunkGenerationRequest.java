package net.minestom.server.instance.generator.units;

import net.minestom.server.instance.Chunk;

import java.util.List;

public record ChunkGenerationRequest(List<Chunk> chunks) implements GenerationRequest<ChunkGenerationResponse> {
}
