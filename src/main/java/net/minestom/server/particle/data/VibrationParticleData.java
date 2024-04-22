package net.minestom.server.particle.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record VibrationParticleData(@NotNull VibrationSource type, @NotNull Point source, int entityId, float entityEyeHeight, int ticks) implements ParticleData {
    public enum VibrationSource {
        BLOCK,
        ENTITY
    }

    VibrationParticleData(NetworkBuffer buffer) {
        this(read(buffer));
    }

    VibrationParticleData() {
        this(VibrationSource.BLOCK, Vec.ZERO, 0, 0, 0);
    }

    private VibrationParticleData(VibrationParticleData copy) {
        this(copy.type, copy.source, copy.entityId, copy.entityEyeHeight, copy.ticks);
    }

    private static VibrationParticleData read(NetworkBuffer buffer) {
        VibrationSource type = buffer.readEnum(VibrationSource.class);

        if (type == VibrationSource.BLOCK) {
            return new VibrationParticleData(type, buffer.read(NetworkBuffer.BLOCK_POSITION), 0, 0, buffer.read(NetworkBuffer.VAR_INT));
        } else {
            return new VibrationParticleData(type, Vec.ZERO, buffer.read(NetworkBuffer.VAR_INT), buffer.read(NetworkBuffer.FLOAT), buffer.read(NetworkBuffer.VAR_INT));
        }
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(VibrationSource.class, type);

        if (type == VibrationSource.BLOCK) {
            writer.write(NetworkBuffer.BLOCK_POSITION, source);
            writer.write(NetworkBuffer.VAR_INT, ticks);
        } else {
            writer.write(NetworkBuffer.VAR_INT, entityId);
            writer.write(NetworkBuffer.FLOAT, entityEyeHeight);
            writer.write(NetworkBuffer.VAR_INT, ticks);
        }
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.VIBRATION.id();
    }
}
