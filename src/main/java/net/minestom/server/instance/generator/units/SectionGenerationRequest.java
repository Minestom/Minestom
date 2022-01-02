package net.minestom.server.instance.generator.units;

import net.minestom.server.coordinate.Point;

import java.util.List;

public record SectionGenerationRequest(List<Point> sections) implements GenerationRequest<SectionGenerationResponse> {
}
