package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.binary.BinaryWriter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class DustParticleData extends ParticleData {
    public static final BiFunction<Particle<DustParticleData>, String,DustParticleData> READER = (particle, data) -> {
        //TODO better block state parsing, also required for ArgumentBlockState
        String[] numbers = data.split(StringUtils.SPACE);
        if (numbers.length != 4) {
            return null;
        }

        try {
            return new DustParticleData(new Color(
                    (int) Float.parseFloat(numbers[0]) * 255,
                    (int) Float.parseFloat(numbers[1]) * 255,
                    (int) Float.parseFloat(numbers[2]) * 255),
                    Integer.parseInt(numbers[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    };

    private final float red, green, blue;
    private final float scale;

    public DustParticleData(@NotNull Color color, float scale) {
        super(Particle.DUST);
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
