package net.minestom.server.particle.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record VibrationBlockParticleData (Point source, int ticks) implements ParticleData {
    VibrationBlockParticleData(NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.BLOCK_POSITION), buffer.read(NetworkBuffer.VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, 0);
        writer.write(NetworkBuffer.BLOCK_POSITION, source);
        writer.write(NetworkBuffer.VAR_INT, ticks);
    }
}
