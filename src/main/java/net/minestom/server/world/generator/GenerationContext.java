package net.minestom.server.world.generator;

import net.minestom.server.instance.Instance;

public record GenerationContext(WorldGenerator worldGenerator, Instance instance) {
}
