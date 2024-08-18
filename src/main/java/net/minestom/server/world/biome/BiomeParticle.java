package net.minestom.server.world.biome;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.particle.Particle;

public record BiomeParticle(float probability, Particle particle) {
    public CompoundBinaryTag toNbt() {
        return CompoundBinaryTag.builder()
                .putFloat("probability", probability)
                .put("options", particle.toNbt())
                .build();
    }
}
