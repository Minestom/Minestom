package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registries;

/**
 * Represent an argument giving a particle type
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
