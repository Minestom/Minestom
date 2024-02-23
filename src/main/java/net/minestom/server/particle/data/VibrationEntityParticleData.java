package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record VibrationEntityParticleData (int entityId, float eyeHeight, int ticks) implements ParticleData {
    VibrationEntityParticleData(NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.VAR_INT), buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, 1);
        writer.write(NetworkBuffer.VAR_INT, entityId);
        writer.write(NetworkBuffer.FLOAT, eyeHeight);
        writer.write(NetworkBuffer.VAR_INT, ticks);
    }
}
