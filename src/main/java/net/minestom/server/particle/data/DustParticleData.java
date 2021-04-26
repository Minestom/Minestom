package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DustParticleData extends ParticleData {
    private final Color color;
    private final float scale;

    public DustParticleData(@NotNull Color color, float scale) {
        this.color = color;
        this.scale = scale;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeFloat(color.getRed());
        writer.writeFloat(color.getGreen());
        writer.writeFloat(color.getBlue());
        writer.writeFloat(scale);
    }
}
