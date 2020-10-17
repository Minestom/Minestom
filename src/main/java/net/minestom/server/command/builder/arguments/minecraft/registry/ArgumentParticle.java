package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registries;

/**
 * Represents an argument giving a {@link Particle}.
 */
public class ArgumentParticle extends ArgumentRegistry<Particle> {

    public ArgumentParticle(String id) {
        super(id);
    }

    @Override
    public Particle getRegistry(String value) {
        return Registries.getParticle(value);
    }
}
