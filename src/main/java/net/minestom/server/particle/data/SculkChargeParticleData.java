package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record SculkChargeParticleData(float roll) implements ParticleData {
    SculkChargeParticleData(NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.FLOAT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, roll);
    }
}
