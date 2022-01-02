package net.minestom.server.instance;

import net.minestom.server.instance.batch.ChunkGenerationBatch;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.units.ChunkGenerationRequest;
import net.minestom.server.instance.generator.units.ChunkGenerationResponse;

import java.util.stream.Collectors;

/**
 * Provides full compatibility for the deprecated {@link ChunkGenerator}
 */
class ChunkGeneratorCompatibilityLayer implements Generator<ChunkGenerationRequest, ChunkGenerationResponse> {
    private final ChunkGenerator chunkGenerator;

    public ChunkGeneratorCompatibilityLayer(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @Override
    public ChunkGenerationResponse generate(Instance instance, ChunkGenerationRequest request) {
        return new ChunkGenerationResponse(request.chunks().stream()
                .map(chunk -> new ChunkGenerationBatch((InstanceContainer) instance, chunk)
                        .generate(chunkGenerator)).collect(Collectors.toList()));
    }

    @Override
    public Class<ChunkGenerationRequest> supportedRequestType() {
        return ChunkGenerationRequest.class;
    }

}
