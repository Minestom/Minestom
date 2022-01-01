package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.Point;
import java.util.List;

public record GenerationRequest(GenerationUnit unit, List<Point> locations) {
}
