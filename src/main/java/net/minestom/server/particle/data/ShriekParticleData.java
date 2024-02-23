package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record ShriekParticleData(int delay) implements ParticleData {
    ShriekParticleData(NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, delay);
    }
}
