package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record SculkChargeParticleData(float roll) implements ParticleData {
    SculkChargeParticleData(NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.FLOAT));
    }

    SculkChargeParticleData() {
        this(0);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, roll);
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.SCULK_CHARGE.id();
    }
}
