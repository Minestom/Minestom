package net.minestom.server.instance.generator.units;

import net.minestom.server.instance.generator.SectionResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record SectionGenerationResponse(List<CompletableFuture<SectionResult>> futures) implements GenerationResponse<SectionResult> {
}
