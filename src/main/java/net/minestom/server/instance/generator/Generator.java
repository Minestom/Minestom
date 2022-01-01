package net.minestom.server.instance.generator;

import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface Generator {
    List<CompletableFuture<SectionResult>> generate(Instance instance, GenerationRequest request);
}
