package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record ShriekParticleData(int delay) implements ParticleData {
    ShriekParticleData(NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.VAR_INT));
    }

    ShriekParticleData() {
        this(0);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, delay);
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.SHRIEK.id();
    }
}
