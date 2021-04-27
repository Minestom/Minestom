package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DustParticleData extends ParticleData {
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
