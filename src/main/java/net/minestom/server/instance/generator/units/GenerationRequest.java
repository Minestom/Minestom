package net.minestom.server.instance.generator.units;

sealed public interface GenerationRequest<T extends GenerationResponse> permits ChunkGenerationRequest, SectionGenerationRequest {
}
