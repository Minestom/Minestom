package net.minestom.server.utils.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.units.*;
import net.minestom.server.utils.async.AsyncUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneratorUtil {
    public static <T extends GenerationRequest<R>, R extends GenerationResponse<?>> CompletableFuture<Chunk> generateChunk(Instance instance, Generator<T, R> generator, Chunk chunk) {
        return generateChunks(instance, generator, List.of(chunk)).get(0);
    }

    public static <T extends GenerationRequest<R>, R extends GenerationResponse<?>> List<CompletableFuture<Chunk>> generateChunks(Instance instance, Generator<T, R> generator, List<Chunk> chunk) {
        final Class<? extends GenerationRequest<?>> requestType = generator.supportedRequestType();

        if (requestType == ChunkGenerationRequest.class) {
            final Generator<ChunkGenerationRequest, ChunkGenerationResponse> gen = (Generator<ChunkGenerationRequest, ChunkGenerationResponse>) generator;
            return gen.generate(instance, new ChunkGenerationRequest(chunk)).futures();
        } else if (requestType == SectionGenerationRequest.class) {
            final Generator<SectionGenerationRequest, SectionGenerationResponse> gen = (Generator<SectionGenerationRequest, SectionGenerationResponse>) generator;
            return chunk.stream().map(c -> AsyncUtils.allOf(gen.generate(instance, new SectionGenerationRequest(
                    IntStream.range(instance.getSectionMinY(), instance.getSectionMaxY())
                            .mapToObj(y -> new Vec(c.getChunkX(), y, c.getChunkZ()))
                            .collect(Collectors.toList()))).futures()).thenCompose(results -> {
                results.forEach(r -> c.setSection(r.sectionData(), (int) r.location().y()));
                return CompletableFuture.completedFuture(c);
            })).collect(Collectors.toList());
        } else {
            throw new IllegalStateException("Generator doesn't support any known generation request!");
        }
    }

    //TODO More utils
}
