package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record DustColorTransitionParticleData(float fromRed, float fromGreen, float fromBlue, float scale, float toRed, float toGreen, float toBlue) implements ParticleData {
    public DustColorTransitionParticleData {
        if (fromRed < 0 || fromRed > 1) {
            throw new IllegalArgumentException("fromRed must be between 0 and 1");
        }
        if (fromGreen < 0 || fromGreen > 1) {
            throw new IllegalArgumentException("fromGreen must be between 0 and 1");
        }
        if (fromBlue < 0 || fromBlue > 1) {
            throw new IllegalArgumentException("fromBlue must be between 0 and 1");
        }
        if (toRed < 0 || toRed > 1) {
            throw new IllegalArgumentException("toRed must be between 0 and 1");
        }
        if (toGreen < 0 || toGreen > 1) {
            throw new IllegalArgumentException("toGreen must be between 0 and 1");
        }
        if (toBlue < 0 || toBlue > 1) {
            throw new IllegalArgumentException("toBlue must be between 0 and 1");
        }
        if (scale < 0.01 || scale > 4) {
            throw new IllegalArgumentException("scale must be positive");
        }
    }

    public DustColorTransitionParticleData(NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT)
        );
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, fromRed);
        writer.write(NetworkBuffer.FLOAT, fromGreen);
        writer.write(NetworkBuffer.FLOAT, fromBlue);
        writer.write(NetworkBuffer.FLOAT, scale);
        writer.write(NetworkBuffer.FLOAT, toRed);
        writer.write(NetworkBuffer.FLOAT, toGreen);
        writer.write(NetworkBuffer.FLOAT, toBlue);
    }
}
