package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record DustColorTransitionParticleData(float fromRed, float fromGreen, float fromBlue, float scale, float toRed, float toGreen, float toBlue) implements ParticleData {
    public DustColorTransitionParticleData {
        Check.argCondition(fromRed < 0 || fromRed > 1, "fromRed must be between 0 and 1");
        Check.argCondition(fromGreen < 0 || fromGreen > 1, "fromGreen must be between 0 and 1");
        Check.argCondition(fromBlue < 0 || fromBlue > 1, "fromBlue must be between 0 and 1");
        Check.argCondition(toRed < 0 || toRed > 1, "toRed must be between 0 and 1");
        Check.argCondition(toGreen < 0 || toGreen > 1, "toGreen must be between 0 and 1");
        Check.argCondition(toBlue < 0 || toBlue > 1, "toBlue must be between 0 and 1");
        Check.argCondition(scale < 0.01 || scale > 4, "scale must be positive");
    }

    DustColorTransitionParticleData(NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT),
            buffer.read(NetworkBuffer.FLOAT)
        );
    }

    DustColorTransitionParticleData() {
        this(1, 1, 1, 1, 1, 1, 1);
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

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.DUST_COLOR_TRANSITION.id();
    }
}
