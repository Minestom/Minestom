package net.minestom.server.world.generator;

import net.minestom.server.utils.noise.Noise2D;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.structures.StructurePool;

import java.util.Set;

public class BiomeGenerator {
    private final Biome biome;
    private final Set<StructurePool> structurePools;
    private final BlockProvider blockProvider;
    private final Noise2D heightNoise;

    public BiomeGenerator(Biome biome, Set<StructurePool> structurePools, BlockProvider blockProvider, Noise2D heightNoise) {
        this.biome = biome;
        this.structurePools = structurePools;
        this.blockProvider = blockProvider;
        this.heightNoise = heightNoise;
    }

    public int getId() {
        return biome.id();
    }
}
