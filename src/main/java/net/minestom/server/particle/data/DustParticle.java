package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.utils.binary.BinaryWriter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class DustParticle extends Particle {
    public static final BiFunction<ParticleType<DustParticle>, @Nullable String, DustParticle> READER = (particle, data) -> {
        if (data == null) return null;

        String[] numbers = data.split(StringUtils.SPACE);
        if (numbers.length != 4) {
            return null;
        }

        try {
            return new DustParticle(new Color(
                    (int) Float.parseFloat(numbers[0]) * 255,
                    (int) Float.parseFloat(numbers[1]) * 255,
                    (int) Float.parseFloat(numbers[2]) * 255),
                    Integer.parseInt(numbers[3]),
                    true, 0, 0, 0, 0, 1);
        } catch (NumberFormatException e) {
            return null;
        }
    };

    private final float red, green, blue;
    private final float scale;

    public DustParticle(@NotNull Color color, float scale, boolean longDistance,
                        float offsetX, float offsetY, float offsetZ, float speed, int count) {
        super(ParticleType.DUST, longDistance, offsetX, offsetY, offsetZ, speed, count);
        this.red = color.getRed() / 255F;
        this.green = color.getGreen() / 255F;
        this.blue = color.getBlue() / 255F;
        this.scale = scale;
    }

    public DustParticle(@NotNull Color color, float scale) {
        super(ParticleType.DUST);
        this.red = color.getRed() / 255F;
        this.green = color.getGreen() / 255F;
        this.blue = color.getBlue() / 255F;
        this.scale = scale;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeFloat(red);
        writer.writeFloat(green);
        writer.writeFloat(blue);
        writer.writeFloat(scale);
    }
}
