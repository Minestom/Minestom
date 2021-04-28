package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class Particle {
    public static final BiFunction<ParticleType<Particle>, @Nullable String, Particle> READER =
            (particle, data) -> Particle.of(particle);

    private final ParticleType<?> particleType;

    protected Particle(@NotNull ParticleType<?> particleType) {
        this.particleType = particleType;
    }

    public void write(BinaryWriter writer) {}

    public ParticleType<?> getParticle() {
        return particleType;
    }

    public static @NotNull Particle of(@NotNull ParticleType<Particle> particleType) {
        return new Particle(particleType);
    }

    public static @NotNull BlockParticle block(BlockState state) {
        return new BlockParticle(ParticleType.BLOCK, state);
    }

    public static @NotNull BlockParticle fallingDust(BlockState state) {
        return new BlockParticle(ParticleType.FALLING_DUST, state);
    }

    public static @NotNull DustParticle dust(@NotNull Color color, float scale) {
        return new DustParticle(color, scale);
    }

    public static @NotNull ItemParticle item(@NotNull ItemStack item) {
        return new ItemParticle(item);
    }
}
