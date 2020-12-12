package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link Particle}.
 */
public class ArgumentParticle extends ArgumentRegistry<Particle> {

    public ArgumentParticle(String id) {
        super(id);
    }

    @Override
    public Particle getRegistry(@NotNull String value) {
        return Registries.getParticle(value);
    }
}
