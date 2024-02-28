package net.minestom.server.particle.data;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record DustColorTransitionParticleData(@NotNull RGBLike from, float scale, @NotNull RGBLike to) implements ParticleData {
    public DustColorTransitionParticleData {
        Check.argCondition(scale < 0.01 || scale > 4, "scale must be between 0.01 and 4: was {0}", scale);
    }

    DustColorTransitionParticleData() {
        this(new Color(255, 255, 255), 1, new Color(255, 255, 255));
    }

    DustColorTransitionParticleData(NetworkBuffer buffer) {
        this(new Color(
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255),
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255),
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255)
        ), buffer.read(NetworkBuffer.FLOAT), new Color(
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255),
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255),
                (int) (buffer.read(NetworkBuffer.FLOAT) * 255)
        ));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, from.red() / 255f);
        writer.write(NetworkBuffer.FLOAT, from.green() / 255f);
        writer.write(NetworkBuffer.FLOAT, from.blue() / 255f);
        writer.write(NetworkBuffer.FLOAT, scale);
        writer.write(NetworkBuffer.FLOAT, to.red() / 255f);
        writer.write(NetworkBuffer.FLOAT, to.green() / 255f);
        writer.write(NetworkBuffer.FLOAT, to.blue() / 255f);
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.DUST_COLOR_TRANSITION.id();
    }
}
