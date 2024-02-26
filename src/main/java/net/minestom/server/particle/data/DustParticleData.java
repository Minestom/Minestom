package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record DustParticleData(float red, float green, float blue, float scale) implements ParticleData {
    public DustParticleData {
        Check.argCondition(red < 0 || red > 1, "red must be between 0 and 1");
        Check.argCondition(green < 0 || green > 1, "green must be between 0 and 1");
        Check.argCondition(blue < 0 || blue > 1, "blue must be between 0 and 1");
        Check.argCondition(scale < 0.01 || scale > 4, "scale must be positive");
    }

    DustParticleData(NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.FLOAT));
    }

    DustParticleData() {
        this(1, 1, 1, 1);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, red);
        writer.write(NetworkBuffer.FLOAT, green);
        writer.write(NetworkBuffer.FLOAT, blue);
        writer.write(NetworkBuffer.FLOAT, scale);
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.DUST.id();
    }
}
