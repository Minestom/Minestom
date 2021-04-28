package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class ParticleData {
    public static final BiFunction<Particle<ParticleData>, String, ParticleData> READER =
            (particle, data) -> ParticleData.of(particle);

    private final Particle<?> particle;

    protected ParticleData(@NotNull Particle<?> particle) {
        this.particle = particle;
    }

    public void write(BinaryWriter writer) {}

    public Particle<?> getParticle() {
        return particle;
    }

    public static @NotNull ParticleData of(@NotNull Particle<ParticleData> particle) {
        return new ParticleData(particle);
    }

    public static @NotNull BlockParticleData block(BlockState state) {
        return new BlockParticleData(Particle.BLOCK, state);
    }

    public static @NotNull BlockParticleData fallingDust(BlockState state) {
        return new BlockParticleData(Particle.FALLING_DUST, state);
    }

    public static @NotNull DustParticleData dust(@NotNull Color color, float scale) {
        return new DustParticleData(color, scale);
    }

    public static @NotNull ItemParticleData item(@NotNull ItemStack item) {
        return new ItemParticleData(item);
    }

    public static @Nullable ParticleData fromString(@Nullable Particle<?> particle, @Nullable String data) {
        if (particle == null) {
            return null;
        } else if (data == null) {
            return ParticleData.of((Particle<ParticleData>) particle);
        } else {
            return particle.readData(data);
        }
    }
}
