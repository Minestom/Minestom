package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record DustParticleData(float red, float green, float blue, float scale) implements ParticleData {
    public DustParticleData {
        if (red < 0 || red > 1) {
            throw new IllegalArgumentException("Red must be between 0 and 1");
        }
        if (green < 0 || green > 1) {
            throw new IllegalArgumentException("Green must be between 0 and 1");
        }
        if (blue < 0 || blue > 1) {
            throw new IllegalArgumentException("Blue must be between 0 and 1");
        }
        if (scale < 0.01 || scale > 4) {
            throw new IllegalArgumentException("Scale must be positive");
        }
    }

    public DustParticleData(NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, red);
        writer.write(NetworkBuffer.FLOAT, green);
        writer.write(NetworkBuffer.FLOAT, blue);
        writer.write(NetworkBuffer.FLOAT, scale);
    }
}
