package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class DustParticleEffect extends ParticleEffect {

    private final float red, green, blue;
    private final float scale;

    public DustParticleEffect(@NotNull Color color, float scale) {
        this.red = color.getRed() / 255F;
        this.green = color.getGreen() / 255F;
        this.blue = color.getBlue() / 255F;
        this.scale = scale;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(red);
        writer.writeFloat(green);
        writer.writeFloat(blue);
        writer.writeFloat(scale);
    }

    @Override
    public @Nullable DustParticleEffect read(@Nullable Scanner data) {
        if (data == null) return null;

        try {
            int red = (int) (data.nextFloat() * 255);
            int green = (int) (data.nextFloat() * 255);
            int blue = (int) (data.nextFloat() * 255);
            float scale = data.nextFloat();

            return new DustParticleEffect(new Color(red, green, blue), scale);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
