package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.binary.BinaryWriter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ParticleData {
    private final Particle particle;

    public ParticleData(Particle particle) {
        this.particle = particle;
    }

    public abstract void write(BinaryWriter writer);

    public Particle getParticle() {
        return particle;
    }

    public static @NotNull ParticleData of(Particle particle) {
        return new ParticleData(particle) {
            @Override
            public void write(BinaryWriter writer) {}
        };
    }

    public static @NotNull BlockParticleData block(short blockstateID) {
        return new BlockParticleData(Particle.BLOCK, blockstateID);
    }

    public static @NotNull BlockParticleData fallingDust(short blockstateID) {
        return new BlockParticleData(Particle.FALLING_DUST, blockstateID);
    }

    public static @NotNull DustParticleData dust(@NotNull Color color, float scale) {
        return new DustParticleData(color, scale);
    }

    public static @NotNull ItemParticleData item(@NotNull ItemStack item) {
        return new ItemParticleData(item);
    }

    public static @Nullable ParticleData fromString(@Nullable Particle particle, @Nullable String data) {
        if (particle == null) {
            return null;
        } else if (data == null) {
            return ParticleData.of(particle);
        } else {
            //TODO temporary, should be inside the particle type
            //TODO particles should have a data linked to them
            if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST) {
                return new BlockParticleData(particle, Registries.getBlock(data).getBlockId());
            } else if (particle == Particle.DUST) {
                String[] numbers = data.split(StringUtils.SPACE);
                if (numbers.length != 4) {
                    return null;
                }

                try {
                    return ParticleData.dust(new Color((int) Float.parseFloat(numbers[0]) * 255,
                                    (int) Float.parseFloat(numbers[1]) * 255,
                                    (int) Float.parseFloat(numbers[2]) * 255),
                            Integer.parseInt(numbers[3]));
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (particle == Particle.ITEM) {
                try {
                    return ParticleData.item(ArgumentType.ItemStack("").parse(data));
                } catch (ArgumentSyntaxException e) {
                    return null;
                }
            } else {
                return ParticleData.of(particle);
            }
        }
    }
}
