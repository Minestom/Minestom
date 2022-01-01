package net.minestom.server.instance.generator.units;

import java.util.List;
import java.util.concurrent.CompletableFuture;

sealed public interface GenerationResponse<T> permits ChunkGenerationResponse, SectionGenerationResponse {
    List<CompletableFuture<T>> futures();
}
