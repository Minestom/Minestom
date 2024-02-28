package net.minestom.server.particle.data;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record DustParticleData(@NotNull RGBLike color, float scale) implements ParticleData {
    public DustParticleData {
        Check.argCondition(scale < 0.01 || scale > 4, "scale must be between 0.01 and 4");
    }

    DustParticleData(NetworkBuffer buffer) {
        this(new Color(
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255),
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255),
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255)
        ), buffer.read(NetworkBuffer.FLOAT));
    }

    DustParticleData() {
        this(new Color(255, 255, 255), 1);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, color.red() / 255f);
        writer.write(NetworkBuffer.FLOAT, color.green() / 255f);
        writer.write(NetworkBuffer.FLOAT, color.blue() / 255f);
        writer.write(NetworkBuffer.FLOAT, scale);
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.DUST.id();
    }
}
